package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.NonNull;
import com.facebook.stetho.json.annotation.JsonProperty;

import java.util.List;

public final class PausedEvent {
    @JsonProperty
    @NonNull
    public final List callFrames;
    @JsonProperty
    @NonNull
    public final String reason;

    public PausedEvent(
            @NonNull List callFrames,
            @NonNull String reason
    ) {
        super();
        this.callFrames = callFrames;
        this.reason = reason;
    }


    public PausedEvent(
            @NonNull List callFrames
    ) {
        this(callFrames, "other");
    }
}