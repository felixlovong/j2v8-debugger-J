package com.alexii.j2v8debuggerJ;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.stetho.inspector.console.RuntimeReplFactory;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcException;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsMethod;

import org.json.JSONException;
import org.json.JSONObject;

public final class Runtime implements ChromeDevtoolsDomain {
    @NonNull
    private com.facebook.stetho.inspector.protocol.module.Runtime adaptee;

    public Runtime(
            @Nullable RuntimeReplFactory replFactory
    ) {
        this.adaptee = new com.facebook.stetho.inspector.protocol.module.Runtime(replFactory);
    }

    @NonNull
    public final com.facebook.stetho.inspector.protocol.module.Runtime getAdaptee() {
        return this.adaptee;
    }

    public final void setAdaptee(
            @NonNull com.facebook.stetho.inspector.protocol.module.Runtime var1
    ) {
        this.adaptee = var1;
    }

    @ChromeDevtoolsMethod
    @NonNull
    public final JsonRpcResult getProperties(
            @Nullable JsonRpcPeer peer,
            @Nullable JSONObject params
    ) throws JsonRpcException, JSONException {
        if (params != null) {
            params.put("ownProperties", true);
        }

        return this.adaptee.getProperties(peer, params);
    }

    @ChromeDevtoolsMethod
    public final void releaseObject(
            @Nullable JsonRpcPeer peer,
            @Nullable JSONObject params
    ) throws JSONException {
        this.adaptee.releaseObject(peer, params);
    }

    @ChromeDevtoolsMethod
    public final void releaseObjectGroup(
            @Nullable JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        this.adaptee.releaseObjectGroup(peer, params);
    }

    @ChromeDevtoolsMethod
    @Nullable
    public final JsonRpcResult callFunctionOn(
            @Nullable JsonRpcPeer peer,
            @Nullable JSONObject params
    ) throws JsonRpcException {
        return this.adaptee.callFunctionOn(peer, params);
    }

    @ChromeDevtoolsMethod
    @NonNull
    public final JsonRpcResult evaluate(
            @Nullable JsonRpcPeer peer,
            @Nullable JSONObject params
    ) {
        return this.adaptee.evaluate(peer, params);
    }
}