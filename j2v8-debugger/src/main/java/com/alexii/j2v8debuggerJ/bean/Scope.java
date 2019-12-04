package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.NonNull;
import com.facebook.stetho.inspector.protocol.module.Runtime;
import com.facebook.stetho.json.annotation.JsonProperty;

public final class Scope {
    @JsonProperty
    @NonNull
    public final String type;
    @JsonProperty
    @NonNull
    public final Runtime.RemoteObject object;

    public Scope(
            @NonNull String type,
            @NonNull Runtime.RemoteObject object
    ) {
        super();
        this.type = type;
        this.object = object;
    }
}