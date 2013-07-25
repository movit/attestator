package com.attestator.admin.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;

public abstract class AdminAsyncCallback<T> implements AsyncCallback<T> {

    @Override
    public void onFailure(Throwable caught) {
        (new AlertMessageBox("Ошибка", caught.getMessage())).show();
    }
    
}
