package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.Nullable;
import com.facebook.stetho.json.annotation.JsonProperty;

public final class ScriptParsedEvent {
    @JsonProperty
    @Nullable
    public final String scriptId;
    @JsonProperty
    @Nullable
    public final String url;

    public ScriptParsedEvent(
            @Nullable String scriptId,
            @Nullable String url
    ) {
        this.scriptId = scriptId;
        this.url = url;
    }
}