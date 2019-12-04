// Debugger.java
package com.alexii.j2v8debuggerJ;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.alexii.j2v8debuggerJ.bean.GetScriptSourceRequest;
import com.alexii.j2v8debuggerJ.bean.GetScriptSourceResponse;
import com.alexii.j2v8debuggerJ.bean.Location;
import com.alexii.j2v8debuggerJ.bean.RemoveBreakpointRequest;
import com.alexii.j2v8debuggerJ.bean.ScriptParsedEvent;
import com.alexii.j2v8debuggerJ.bean.SetBreakpointByUrlRequest;
import com.alexii.j2v8debuggerJ.bean.SetBreakpointByUrlResponse;
import com.alexii.j2v8debuggerJ.utils.LogUtils;
import com.alexii.j2v8debuggerJ.utils.LoggerWrapper;
import com.eclipsesource.v8.debug.DebugHandler;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsMethod;
import com.facebook.stetho.json.ObjectMapper;
import com.facebook.stetho.websocket.CloseCodes;
import com.facebook.stetho.websocket.SimpleSession;

import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class Debugger extends com.facebook.stetho.inspector.protocol.module.Debugger {
    @NonNull
    private ObjectMapper dtoMapper;
    @Nullable
    private DebugHandler v8Debugger;
    private final V8ToChromeDevToolsBreakHandler v8ToChromeBreakHandler;
    private ExecutorService v8Executor;
    private JsonRpcPeer connectedPeer;
    private final ScriptSourceProvider scriptSourceProvider;
    public static final String TAG = "j2v8-debugger";

    @VisibleForTesting
    @NonNull
    public final ObjectMapper getDtoMapper() {
        return this.dtoMapper;
    }

    @VisibleForTesting
    public final void setDtoMapper(
            @NonNull ObjectMapper dtoMapper
    ) {
        this.dtoMapper = dtoMapper;
    }

    @VisibleForTesting
    @Nullable
    public final DebugHandler getV8Debugger() {
        return this.v8Debugger;
    }

    public final void initialize(
            @NonNull DebugHandler v8Debugger,
            @NonNull ExecutorService v8Executor
    ) {
        this.v8Debugger = v8Debugger;
        this.v8Executor = v8Executor;
        v8Debugger.addBreakHandler(this.v8ToChromeBreakHandler);
    }

    private void validateV8Initialized() {
        if (this.v8Executor == null || this.v8Debugger == null) {
            throw new IllegalStateException("Unable to set breakpoint when v8 was not initialized yet");
        }
    }

    @ChromeDevtoolsMethod
    public void enable(
            @NonNull final JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        this.runStethoSafely(() -> {
            this.connectedPeer = peer;

            Iterable iterable = this.scriptSourceProvider.getAllScriptIds();
            for (Object o : iterable) {
                ScriptParsedEvent scriptParsedEvent = new ScriptParsedEvent((String) o, DebuggerExtensions.getScriptIdToUrlResult((String) o));
                peer.invokeMethod("Debugger.scriptParsed", scriptParsedEvent, null);
            }
            peer.registerDisconnectReceiver(() ->
                    this.runStethoSafely(() -> {
                        this.connectedPeer = null;
                        this.v8ToChromeBreakHandler.resume();
                        this.v8Executor.execute(() -> {
                            if (this.getV8Debugger() != null) {
                                this.getV8Debugger().disableAllBreakPoints();
                            }
                        });

                        return null;
                    }));

            return null;
        });
    }

    public final void onScriptsChanged() {
        if (this.connectedPeer != null) {
            SimpleSession session = this.connectedPeer.getWebSocket();
            if (session != null) {
                session.close(CloseCodes.NORMAL_CLOSURE, "on scripts changed");
            }
        }
    }

    @ChromeDevtoolsMethod
    public void disable(
            @NonNull JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        //xxx: figure-out why and when this method could be called
    }

    @ChromeDevtoolsMethod
    @Nullable
    public final JsonRpcResult getScriptSource(
            @NonNull JsonRpcPeer peer,
            @NonNull final JSONObject params
    ) {
        return (JsonRpcResult) this.runStethoSafely(() -> {
            GetScriptSourceResponse getScriptSourceResponse;
            try {
                GetScriptSourceRequest request = this.getDtoMapper().convertValue(params, GetScriptSourceRequest.class);
                String scriptSource = this.scriptSourceProvider.getSource(request.scriptId);
                getScriptSourceResponse = new GetScriptSourceResponse(scriptSource);
            } catch (Exception e) {
                getScriptSourceResponse = new GetScriptSourceResponse(LoggerWrapper.getLogger().getStackTraceString(e));
            }

            return getScriptSourceResponse;
        });
    }

    @ChromeDevtoolsMethod
    public final void resume(
            @NonNull JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        this.runStethoSafely(() -> {
            this.v8ToChromeBreakHandler.resume();
            return null;
        });
    }

    @ChromeDevtoolsMethod
    public final void pause(
            @NonNull JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        LogUtils.INSTANCE.logChromeDevToolsCalled();
    }

    @ChromeDevtoolsMethod
    public final void stepOver(
            @NonNull JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        this.runStethoSafely(() -> {
            this.v8ToChromeBreakHandler.stepOver();
            return null;
        });
    }

    @ChromeDevtoolsMethod
    public final void stepInto(
            @NonNull JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        this.runStethoSafely(() -> {
            this.v8ToChromeBreakHandler.stepInto();
            return null;
        });
    }

    @ChromeDevtoolsMethod
    public final void stepOut(
            @NonNull JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        this.runStethoSafely(() -> {
            this.v8ToChromeBreakHandler.stepOut();
            return null;
        });
    }

    @ChromeDevtoolsMethod
    @Nullable
    public final JsonRpcResult setBreakpoint(
            @Nullable JsonRpcPeer peer,
            @Nullable final JSONObject params
    ) {
        return (JsonRpcResult) this.runStethoSafely(() -> {
            return (new IllegalArgumentException("Unexpected Debugger.setBreakpoint() is called by Chrome DevTools: " + params));
        });
    }

    @ChromeDevtoolsMethod
    @Nullable
    @SuppressWarnings("unchecked")
    public final JsonRpcResult setBreakpointByUrl(
            @NonNull JsonRpcPeer peer,
            @NonNull final JSONObject params
    ) {
        return (JsonRpcResult)
                this.runStethoAndV8Safely(() -> {
                    Future responseFuture = this.v8Executor.submit((Callable) () -> {
                        SetBreakpointByUrlRequest request = this.getDtoMapper().convertValue(params, SetBreakpointByUrlRequest.class);
                        String scriptId = request.getScriptId();
                        Integer lineNumber = request.lineNumber;

                        int breakpointId = this.getV8Debugger().setScriptBreakpoint(scriptId, lineNumber);
                        Location location = new Location(request.getScriptId(), request.lineNumber, request.columnNumber);
                        SetBreakpointByUrlResponse setBreakpointByUrlResponse = new SetBreakpointByUrlResponse(String.valueOf(breakpointId), location);
                        return setBreakpointByUrlResponse;
                    });

                    return responseFuture.get();
                });
    }

    @ChromeDevtoolsMethod
    public final void removeBreakpoint(
            @NonNull JsonRpcPeer peer,
            @NonNull final JSONObject params
    ) {
        this.runStethoSafely(() -> {
                    final RemoveBreakpointRequest request = this.getDtoMapper().convertValue(params, RemoveBreakpointRequest.class);
                    this.v8Executor.execute(() -> this.getV8Debugger().clearBreakPoint(Integer.parseInt(request.breakpointId)));
                    return null;
                }
        );
    }

    private Object runStethoSafely(
            ActionFunction action
    ) {
        LogUtils.INSTANCE.logChromeDevToolsCalled();

        try {
            return action.onAction();
        } catch (Throwable e) {
            LoggerWrapper.getLogger().w(TAG, "Unable to perform " + LogUtils.INSTANCE.getChromeDevToolsMethodName(), e);
            return null;
        }
    }

    private Object runStethoAndV8Safely(
            final ActionFunction action
    ) {
        return this.runStethoSafely(() -> {
            this.validateV8Initialized();
            this.valideV8NotSuspended();
            return action.onAction();
        });
    }

    @FunctionalInterface
    public interface ActionFunction {
        Object onAction() throws ExecutionException, InterruptedException;
    }

    @FunctionalInterface
    public interface GetFunction {
        Object onGet() throws ExecutionException, InterruptedException;
    }

    private void valideV8NotSuspended() {
        if (this.v8ToChromeBreakHandler.getSuspended()) {
            throw new IllegalStateException("Can't peform " + LogUtils.INSTANCE.getChromeDevToolsMethodName() + " while paused in debugger.");
        }
    }

    public Debugger(@NonNull ScriptSourceProvider scriptSourceProvider) {
        super();
        this.scriptSourceProvider = scriptSourceProvider;
        this.dtoMapper = new ObjectMapper();
        this.v8ToChromeBreakHandler = new V8ToChromeDevToolsBreakHandler(() -> connectedPeer);
    }

    public static JsonRpcPeer getConnectedPeer(Debugger debugger) {
        return debugger.connectedPeer;
    }

    public static void setV8Executor(Debugger debugger, ExecutorService executorService) {
        debugger.v8Executor = executorService;
    }

    public static DebugHandler getV8Debugger(Debugger debugger) {
        return debugger.v8Debugger;
    }

    public static void setV8Debugger(Debugger debugger, DebugHandler debugHandler) {
        debugger.v8Debugger = debugHandler;
    }
}