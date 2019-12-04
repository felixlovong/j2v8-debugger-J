package com.alexii.j2v8debuggerJ;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface ScriptSourceProvider {

    @NonNull
    Collection<String> getAllScriptIds();

    @NonNull
    String getSource(
            @NonNull String scriptId
    );
}
