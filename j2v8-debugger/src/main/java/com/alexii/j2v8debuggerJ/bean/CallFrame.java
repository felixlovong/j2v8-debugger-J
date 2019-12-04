package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.facebook.stetho.inspector.protocol.module.Runtime;
import com.facebook.stetho.json.annotation.JsonProperty;

import java.util.List;

public final class CallFrame {
    @JsonProperty
    @NonNull
    public final String callFrameId;
    @JsonProperty
    @NonNull
    public final String functionName;
    @JsonProperty
    @NonNull
    public final Location location;
    @JsonProperty
    @NonNull
    public final String url;
    @JsonProperty
    @NonNull
    public final List scopeChain;
    @JsonProperty
    @Nullable
    public final Runtime.RemoteObject remoteObject;

    public CallFrame(
            @NonNull String callFrameId,
            @NonNull String functionName,
            @NonNull Location location,
            @NonNull String url,
            @NonNull List scopeChain,
            @Nullable Runtime.RemoteObject remoteObject
    ) {
        super();
        this.callFrameId = callFrameId;
        this.functionName = functionName;
        this.location = location;
        this.url = url;
        this.scopeChain = scopeChain;
        this.remoteObject = remoteObject;
    }

}