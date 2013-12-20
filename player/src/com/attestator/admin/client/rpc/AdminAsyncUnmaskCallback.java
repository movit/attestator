package com.attestator.admin.client.rpc;

import com.attestator.common.client.helper.WindowHelper;

public abstract class AdminAsyncUnmaskCallback<T> extends AdminAsyncCallback<T> {
    
    @Override
    public void onSuccess(T result) {
        WindowHelper.unmask();
    };
    
    @Override
    public void onFailure(Throwable caught) {
        WindowHelper.unmask();
        super.onFailure(caught);
    }
    
}
