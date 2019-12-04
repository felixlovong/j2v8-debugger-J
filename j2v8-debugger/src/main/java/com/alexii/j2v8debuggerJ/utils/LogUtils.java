package com.alexii.j2v8debuggerJ.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsMethod;

import java.lang.reflect.Method;

public final class LogUtils {

    private static boolean enabled;
    public static final LogUtils INSTANCE;

    public static final String TAG = "j2v8-debugger";

    private LogUtils() {
    }

    static {
        INSTANCE = new LogUtils();
    }

    public static boolean getEnabled() {
        return enabled;
    }

    public static void setEnabled(
            boolean pEnabled
    ) {
        enabled = pEnabled;
    }

    @NonNull
    public static String getChromeDevToolsMethodName() {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stackTraceElements = currentThread.getStackTrace();
        int length = stackTraceElements.length;
        int index = 0;

        StackTraceElement chromeDevtoolsStackTraceElement;
        while (true) {
            if (index >= length) {
                chromeDevtoolsStackTraceElement = null;
                break;
            }

            StackTraceElement stackTraceElement;
            boolean hasMatched;
            innerLoop:
            {
                stackTraceElement = stackTraceElements[index];
                Class clazz;
                try {
                    clazz = Class.forName(stackTraceElement.getClassName());
                    if (INSTANCE.isChromeDevToolsClass(clazz) && INSTANCE.isChromeDevToolsMethod(clazz, stackTraceElement.getMethodName())) {
                        hasMatched = true;
                        break innerLoop;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                hasMatched = false;
            }

            if (hasMatched) {
                chromeDevtoolsStackTraceElement = stackTraceElement;
                break;
            }

            ++index;
        }

        return chromeDevtoolsStackTraceElement != null ? chromeDevtoolsStackTraceElement.getMethodName() : "";
    }

    private boolean isChromeDevToolsClass(
            Class clazz
    ) {
        return ChromeDevtoolsDomain.class.isAssignableFrom(clazz);
    }

    private boolean isChromeDevToolsMethod(
            Class clazz,
            String methodName
    ) {
        Method[] clazzMethods = clazz.getMethods();
        int length = clazzMethods.length;
        int index = 0;

        Method chromeDevToolsMethod;
        while (true) {
            if (index >= length) {
                chromeDevToolsMethod = null;
                break;
            }

            Method method = clazzMethods[index];
            if (methodName.equals(method.getName())) {
                chromeDevToolsMethod = method;
                break;
            }

            ++index;
        }

        return chromeDevToolsMethod != null && chromeDevToolsMethod.isAnnotationPresent(ChromeDevtoolsMethod.class);
    }

    @SuppressLint("VisibleForTests")
    public final void logChromeDevToolsCalled() {
        if (enabled) {
            try {
                LoggerWrapper.getLogger().i(TAG, "Calling " + this.getChromeDevToolsMethodName());
            } catch (Exception e) {
                LoggerWrapper.getLogger().e(TAG, "Unable to log called method", e);
            }

        }
    }
}
