package com.alexii.j2v8debuggerJ.utils;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public final class LoggerWrapper {
    @NonNull
    private static Logger logger = new Logger();

    @VisibleForTesting
    @NonNull
    public static Logger getLogger() {
        return logger;
    }

    @VisibleForTesting
    public static void setLogger(
            @NonNull Logger logger
    ) {
        LoggerWrapper.logger = logger;
    }
}