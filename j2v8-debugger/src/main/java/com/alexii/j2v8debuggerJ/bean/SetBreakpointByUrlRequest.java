package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.Nullable;
import com.alexii.j2v8debuggerJ.DebuggerExtensions;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.json.annotation.JsonProperty;

public final class SetBreakpointByUrlRequest implements JsonRpcResult {
    @JsonProperty
    @Nullable
    public String url;
    @JsonProperty
    @Nullable
    public Integer lineNumber;
    @JsonProperty
    @Nullable
    public Integer columnNumber;
    @JsonProperty
    @Nullable
    public String condition;

    @Nullable
    public final String getScriptId() {
        return DebuggerExtensions.getUrlToScriptIdResult(this.url);
    }
}