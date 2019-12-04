package com.alexii.j2v8debuggerJ;

import android.support.annotation.NonNull;

import com.eclipsesource.v8.V8;

public final class V8HelperExtensions {

    public static void releaseDebuggable(
            @NonNull V8 v8,
            boolean reportMemoryLeaks
    ) {
        V8Helper.releaseV8Debugger();
        v8.release(reportMemoryLeaks);
    }

    public static void releaseDebuggable(
            @NonNull V8 v8
    ) {
        releaseDebuggable(v8, true);
    }
}