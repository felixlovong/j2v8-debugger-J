package com.alexii.j2v8debuggerJ;

import android.support.annotation.NonNull;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.debug.DebugHandler;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.eclipsesource.v8.debug.V8DebugServer.DEBUG_OBJECT_NAME;

public final class V8Helper {

    private static DebugHandler v8Debugger;

    private static boolean isDebuggingEnabled() throws NoSuchFieldException, IllegalAccessException {
        Field v8FlagsField = V8.class.getDeclaredField("v8Flags");
        v8FlagsField.setAccessible(true);
        String v8FlagsValue = (String) v8FlagsField.get(null);

        return v8FlagsValue != null && v8FlagsValue.contains(DebugHandler.DEBUG_OBJECT_NAME);
    }

    public static void releaseV8Debugger() {
        if (v8Debugger != null) {
            v8Debugger.release();
        }

        v8Debugger = null;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static Future<V8> createDebuggableV8Runtime(
            @NonNull final ExecutorService v8Executor
    ) {
        V8.setFlags("-expose-debug-as=" + DEBUG_OBJECT_NAME);
        Future<V8> v8Future = v8Executor.submit((Callable) () -> {
            V8 v8 = V8.createV8Runtime();
            DebugHandler v8Debugger = null;
            try {
                v8Debugger = getOrCreateV8Debugger(v8);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            StethoHelper.INSTANCE.initializeWithV8Debugger(v8Debugger, v8Executor);
            return v8;
        });
        return v8Future;
    }

    @NonNull
    public static DebugHandler getOrCreateV8Debugger(
            @NonNull V8 v8
    ) throws Throwable {
        if (v8Debugger == null) {
            if (!isDebuggingEnabled()) {
                throw new IllegalStateException("V8 Debugging is not enabled. Call V8Helper.enableV8Debugging() before creation of V8 runtime!");
            }

            v8Debugger = new DebugHandler(v8);
        }

        return v8Debugger;
    }
}
