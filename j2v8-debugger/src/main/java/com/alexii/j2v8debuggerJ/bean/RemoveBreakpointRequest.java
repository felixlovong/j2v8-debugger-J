package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.Nullable;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.json.annotation.JsonProperty;

public final class RemoveBreakpointRequest implements JsonRpcResult {
    @JsonProperty
    @Nullable
    public String breakpointId;
}