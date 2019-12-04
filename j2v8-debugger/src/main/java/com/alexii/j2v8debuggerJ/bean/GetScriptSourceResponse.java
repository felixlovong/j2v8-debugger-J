package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.NonNull;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.json.annotation.JsonProperty;

public final class GetScriptSourceResponse implements JsonRpcResult {
    @JsonProperty
    @NonNull
    public final String scriptSource;

    public GetScriptSourceResponse(
            @NonNull String scriptSource
    ) {
        super();
        this.scriptSource = scriptSource;
    }
}