// V8ToChromeDevToolsBreakHandler.java
package com.alexii.j2v8debuggerJ;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alexii.j2v8debuggerJ.bean.CallFrame;
import com.alexii.j2v8debuggerJ.bean.Location;
import com.alexii.j2v8debuggerJ.bean.PausedEvent;
import com.alexii.j2v8debuggerJ.bean.Scope;
import com.alexii.j2v8debuggerJ.structure.Range;
import com.alexii.j2v8debuggerJ.utils.LoggerWrapper;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.debug.BreakEvent;
import com.eclipsesource.v8.debug.BreakHandler;
import com.eclipsesource.v8.debug.DebugHandler;
import com.eclipsesource.v8.debug.EventData;
import com.eclipsesource.v8.debug.ExecutionState;
import com.eclipsesource.v8.debug.StepAction;
import com.eclipsesource.v8.debug.mirror.Frame;
import com.eclipsesource.v8.debug.mirror.ValueMirror;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.network.NetworkPeerManager;
import com.facebook.stetho.inspector.protocol.module.Runtime;
import com.facebook.stetho.inspector.protocol.module.Runtime.ObjectType;
import com.facebook.stetho.inspector.protocol.module.Runtime.RemoteObject;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static java.util.Collections.singletonList;

final class V8ToChromeDevToolsBreakHandler implements BreakHandler {
    private CountDownLatch debuggingLatch;
    private boolean suspended;
    private StepAction nextDebugAction;
    private final Debugger.GetFunction currentPeerProvider;

    public static final String TAG = "j2v8-debugger";

    public final boolean getSuspended() {
        return this.suspended;
    }

    public void onBreak(
            @Nullable DebugHandler.DebugEvent event,
            @Nullable ExecutionState state,
            @Nullable EventData eventData,
            @Nullable V8Object data
    ) {
        if (event == DebugHandler.DebugEvent.Break) {
            if (eventData != null) {
                if (eventData instanceof BreakEvent) {
                    NetworkPeerManager networkPeerManager = NetworkPeerManager.getInstanceOrNull();
                    if (networkPeerManager != null) {
                        if (state != null) {
                            try {
                                List<CallFrame> callFrames = new ArrayList<>();
                                Iterable iterable = new Range(0, state.getFrameCount());
                                Iterator iterator = iterable.iterator();

                                while (iterator.hasNext()) {
                                    int index = ((Iterator<Integer>) iterator).next();
                                    Frame frame = state.getFrame(index);
                                    String scriptId = frame.getSourceLocation().getScriptName();
                                    Location location = new Location(scriptId, ((BreakEvent) eventData).getSourceLine(), ((BreakEvent) eventData).getSourceColumn());
                                    Map knowVariables = this.getKnownVariables(frame);
                                    int storedVariablesId = Runtime.mapObject((JsonRpcPeer) this.currentPeerProvider.onGet(), knowVariables);
                                    RemoteObject remoteObject = new RemoteObject();
                                    remoteObject.objectId = String.valueOf(storedVariablesId);
                                    remoteObject.type = ObjectType.OBJECT;
                                    remoteObject.className = "Object";
                                    remoteObject.description = "Object";

                                    String scopeName = com.eclipsesource.v8.debug.mirror.Scope.ScopeType.Local.name().toLowerCase(Locale.ENGLISH);
                                    Scope syntheticScope = new Scope(scopeName, remoteObject);
                                    CallFrame callFrame = new CallFrame(
                                            String.valueOf(index),
                                            frame.getFunction().getName(),
                                            location,
                                            DebuggerExtensions.getScriptIdToUrlResult(scriptId),
                                            singletonList(syntheticScope),
                                            null);
                                    frame.release();
                                    callFrames.add(callFrame);
                                }

                                PausedEvent pausedEvent = new PausedEvent(callFrames);
                                LoggerWrapper.getLogger().w(TAG, "Sending Debugger.paused: " + pausedEvent);
                                networkPeerManager.sendNotificationToPeers("Debugger.paused", pausedEvent);
                                this.pause();
                                if (this.nextDebugAction != null) {
                                    state.prepareStep(this.nextDebugAction);
                                }
                            } catch (Throwable e) {
                                LoggerWrapper.getLogger().w(
                                        TAG,
                                        "Unable to forward break event to Chrome DevTools at " +
                                                ((BreakEvent) eventData).getSourceLine() + ", source: " +
                                                ((BreakEvent) eventData).getSourceLineText(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    private void pause() throws InterruptedException {
        this.suspended = true;
        this.debuggingLatch.await();
        this.suspended = false;
    }

    public final void resume() {
        this.resumeWith(null);
    }

    public final void stepOver() {
        this.resumeWith(StepAction.STEP_NEXT);
    }

    public final void stepInto() {
        this.resumeWith(StepAction.STEP_IN);
    }

    public final void stepOut() {
        this.resumeWith(StepAction.STEP_OUT);
    }

    private void resumeWith(
            StepAction nextDebugAction
    ) {
        CountDownLatch currentLatch = this.debuggingLatch;
        this.debuggingLatch = new CountDownLatch(1);
        currentLatch.countDown();
        this.nextDebugAction = nextDebugAction;
    }

    private Map getKnownVariables(
            @NonNull Frame frame
    ) {
        Map map1 = new LinkedHashMap();

        Iterable iterable1 = new Range(0, frame.getArgumentCount());
        Iterator iterator1 = iterable1.iterator();
        while (iterator1.hasNext()) {
            int index = ((Iterator<Integer>) iterator1).next();
            map1.put(frame.getArgumentName(index), this.toJavaObject(frame.getArgumentValue(index)));
        }

        Map map2 = new LinkedHashMap();

        Iterable iterable2 = new Range(0, frame.getLocalCount());
        Iterator iterator2 = iterable2.iterator();
        while (iterator2.hasNext()) {
            int index = ((Iterator<Integer>) iterator2).next();
            map2.put(frame.getLocalName(index), this.toJavaObject(frame.getLocalValue(index)));
        }

        map1.putAll(map2);
        return map1;
    }

    private Object toJavaObject(
            @NonNull ValueMirror valueMirror
    ) {
        Object v8Object = valueMirror.getValue();

        Object javaObject;
        try {
            javaObject = V8ObjectUtils.getValue(v8Object);
        } catch (IllegalStateException e2) {
            javaObject = "{unknown value}: " + v8Object;
        }

        if (v8Object instanceof Releasable) {
            ((Releasable) v8Object).release();
        }

        valueMirror.release();
        return javaObject;
    }

    public V8ToChromeDevToolsBreakHandler(
            @NonNull Debugger.GetFunction currentPeerProvider
    ) {
        super();
        this.currentPeerProvider = currentPeerProvider;
        this.debuggingLatch = new CountDownLatch(1);
    }
}