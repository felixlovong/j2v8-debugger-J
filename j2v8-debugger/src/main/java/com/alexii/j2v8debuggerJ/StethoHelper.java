package com.alexii.j2v8debuggerJ;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.eclipsesource.v8.debug.DebugHandler;
import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho.DefaultInspectorModulesBuilder;
import com.facebook.stetho.inspector.console.RuntimeReplFactory;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public final class StethoHelper {

    @Nullable
    private static Debugger debugger;
    @Nullable
    private static WeakReference v8DebuggerRef;
    @Nullable
    private static WeakReference v8ExecutorRef;
    @NonNull
    public static String scriptsPathPrefix;
    public static final StethoHelper INSTANCE;

    private StethoHelper() {
    }

    static {
        INSTANCE = new StethoHelper();
        scriptsPathPrefix = "";
    }

    @VisibleForTesting
    @Nullable
    public final Debugger getDebugger() {
        return debugger;
    }

    @NonNull
    public final String getScriptsPathPrefix() {
        return scriptsPathPrefix;
    }

    public final void setScriptsPathPrefix(@NonNull String value) {
        scriptsPathPrefix = "/" + value + "/";
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static InspectorModulesProvider defaultInspectorModulesProvider(
            @NonNull final Context context,
            @NonNull final ScriptSourceProvider scriptSourceProvider
    ) {
        return () -> StethoHelper.getInspectorModules(StethoHelper.INSTANCE, context, scriptSourceProvider);
    }

    public static Iterable getInspectorModules(
            StethoHelper stethoHelper,
            Context context,
            ScriptSourceProvider scriptSourceProvider
    ) {
        return stethoHelper.getInspectorModules(context, scriptSourceProvider, null);
    }

    @NonNull
    public final Iterable getInspectorModules(
            @NonNull Context context,
            @NonNull ScriptSourceProvider scriptSourceProvider,
            @Nullable RuntimeReplFactory factory
    ) {
        Iterable iterable;
        try {
            iterable = this.getDefaultInspectorModulesWithDebugger(context, scriptSourceProvider, factory);
        } catch (Throwable e) {
            Log.e(Debugger.TAG, "Unable to init Stetho with V8 Debugger. Default set-up will be used", e);
            iterable = this.getDefaultInspectorModules(context, factory);
        }

        return iterable;
    }

    @VisibleForTesting
    @NonNull
    @SuppressWarnings("unchecked")
    public final Iterable getDefaultInspectorModulesWithDebugger(
            @NonNull Context context,
            @NonNull ScriptSourceProvider scriptSourceProvider,
            @Nullable RuntimeReplFactory factory
    ) {
        Iterable defaultInspectorModules = this.getDefaultInspectorModules(context, factory);
        ArrayList inspectorModules = new ArrayList();

        for (Object defaultInspectorModule : defaultInspectorModules) {
            ChromeDevtoolsDomain defaultModule = (ChromeDevtoolsDomain) defaultInspectorModule;
            if (com.facebook.stetho.inspector.protocol.module.Debugger.class != defaultModule.getClass() &&
                    com.facebook.stetho.inspector.protocol.module.Runtime.class != defaultModule.getClass()) {
                inspectorModules.add(defaultModule);
            }
        }

        debugger = new Debugger(scriptSourceProvider);
        inspectorModules.add(debugger);
        inspectorModules.add(new com.alexii.j2v8debuggerJ.Runtime(factory));
        this.bindV8ToChromeDebuggerIfReady();
        return inspectorModules;
    }

    private Iterable getDefaultInspectorModules(
            Context context,
            RuntimeReplFactory factory
    ) {
        return (new DefaultInspectorModulesBuilder(context)).runtimeRepl(factory).finish();
    }

    @SuppressWarnings("unchecked")
    public final void initializeWithV8Debugger(
            @NonNull DebugHandler v8Debugger,
            @NonNull ExecutorService v8Executor
    ) {
        v8DebuggerRef = new WeakReference(v8Debugger);
        v8ExecutorRef = new WeakReference(v8Executor);
        this.bindV8ToChromeDebuggerIfReady();
    }

    public static void notifyScriptsChanged() {
        if (debugger != null) {
            debugger.onScriptsChanged();
        }
    }

    private void bindV8ToChromeDebuggerIfReady() {
        boolean chromeDebuggerAttached = debugger != null;
        final DebugHandler v8Debugger = v8DebuggerRef != null ? (DebugHandler) v8DebuggerRef.get() : null;
        final ExecutorService v8Executor = v8ExecutorRef != null ? (ExecutorService) v8ExecutorRef.get() : null;
        boolean v8DebuggerInitialized = v8Debugger != null && v8Executor != null;
        if (v8DebuggerInitialized && chromeDebuggerAttached) {
            v8Executor.execute(() -> StethoHelper.INSTANCE.bindV8DebuggerToChromeDebugger(StethoHelper.INSTANCE.getDebugger(), v8Debugger, v8Executor));
        }
    }

    private void bindV8DebuggerToChromeDebugger(
            Debugger chromeDebugger,
            DebugHandler v8Debugger,
            ExecutorService v8Executor
    ) {
        chromeDebugger.initialize(v8Debugger, v8Executor);
    }
}
