package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class AdminScreen implements IsWidget {

    interface UiBinderImpl extends UiBinder<Widget, AdminScreen> {
    }

    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiHandler("logout")
    public void logoutClick(ClickEvent event) {
        Admin.logout();
    }
    
    public Widget asWidget() {
        return uiBinder.createAndBindUi(this);
    }    
}
