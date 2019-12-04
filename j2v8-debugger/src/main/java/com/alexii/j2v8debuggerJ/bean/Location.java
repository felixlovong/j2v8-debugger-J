package com.alexii.j2v8debuggerJ.bean;

import android.support.annotation.NonNull;
import com.facebook.stetho.json.annotation.JsonProperty;

public final class Location {
    @JsonProperty
    @NonNull
    public final String scriptId;
    @JsonProperty
    public final int lineNumber;
    @JsonProperty
    public final int columnNumber;

    public Location(
            @NonNull String scriptId,
            int lineNumber,
            int columnNumber
    ) {
        super();
        this.scriptId = scriptId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
}