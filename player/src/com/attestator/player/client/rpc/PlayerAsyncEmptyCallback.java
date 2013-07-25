package com.attestator.player.client.rpc;


public class PlayerAsyncEmptyCallback<T> extends PlayerAsyncCallback<T> {
    @Override
    public void onSuccess(T result) {
    }
}
