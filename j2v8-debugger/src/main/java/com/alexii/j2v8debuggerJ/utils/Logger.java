package com.alexii.j2v8debuggerJ.utils;

import android.support.annotation.NonNull;
import android.util.Log;

public final class Logger {
    public final int i(
            @NonNull String tag,
            @NonNull String msg
    ) {
        return Log.i(tag, msg);
    }

    public final int w(
            @NonNull String tag,
            @NonNull String msg
    ) {
        return Log.w(tag, msg);
    }

    public final int w(
            @NonNull String tag,
            @NonNull String msg,
            @NonNull Throwable tr
    ) {
        return Log.w(tag, msg, tr);
    }

    public final int e(
            @NonNull String tag,
            @NonNull String msg,
            @NonNull Throwable tr
    ) {
        return Log.e(tag, msg, tr);
    }

    public final String getStackTraceString(
            @NonNull Throwable tr
    ) {
        return Log.getStackTraceString(tr);
    }
}