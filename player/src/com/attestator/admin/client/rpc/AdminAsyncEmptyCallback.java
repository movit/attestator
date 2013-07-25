package com.attestator.admin.client.rpc;


public class AdminAsyncEmptyCallback<T> extends AdminAsyncCallback<T> {
    @Override
    public void onSuccess(T result) {
    }
}
