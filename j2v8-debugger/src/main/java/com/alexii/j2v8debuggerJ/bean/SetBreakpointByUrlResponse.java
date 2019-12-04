package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.NonNull;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.json.annotation.JsonProperty;

import java.util.List;

import static java.util.Collections.singletonList;

public final class SetBreakpointByUrlResponse implements JsonRpcResult {
    @JsonProperty
    @NonNull
    public final List locations;
    @JsonProperty
    @NonNull
    public final String breakpointId;

    public SetBreakpointByUrlResponse(
            @NonNull String breakpointId,
            @NonNull Location location
    ) {
        super();
        this.breakpointId = breakpointId;
        this.locations = singletonList(location);
    }
}