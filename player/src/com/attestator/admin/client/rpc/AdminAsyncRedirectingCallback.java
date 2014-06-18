package com.attestator.admin.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AdminAsyncRedirectingCallback<T> implements AsyncCallback<T> {
    private AsyncCallback<T> callback;
    
    public void onBeforeSuccess(T result){};
    public void onAfterSuccess(T result){};
    
    public void onBeforeFailure(Throwable caught){};
    public void onAfterFailure(Throwable caught){};    
    
    public AdminAsyncRedirectingCallback(AsyncCallback<T> callback) {
        super();
        this.callback = callback;
    }
    
    public void onSuccess(T result) {
        onBeforeSuccess(result);
        if (callback != null) {
            callback.onSuccess(result);
        }
        onAfterSuccess(result);
    };
    
    @Override
    public void onFailure(Throwable caught) {
        onBeforeFailure(caught);
        if (callback != null) {
            callback.onFailure(caught);
        }
        onAfterFailure(caught);
    }
    
}
