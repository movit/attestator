package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.ui.widgets.tabs.Tab;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.PlainTabPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class AdminScreen implements IsWidget {

    interface UiBinderImpl extends UiBinder<Widget, AdminScreen> {
    }

    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    VerticalLayoutContainer top;
    
    @UiField
    PlainTabPanel tabs;
    
    public AdminScreen() {
        uiBinder.createAndBindUi(this);
        tabs.addSelectionHandler(new SelectionHandler<Widget>() {            
            @SuppressWarnings("unchecked")
            @Override
            public void onSelection(SelectionEvent<Widget> event) {                
                SelectionEvent.fire((HasSelectionHandlers<Tab>)event.getSelectedItem(), (Tab)event.getSelectedItem());                
            }
        });
    }

    @UiHandler("logout")
    public void logoutClick(ClickEvent event) {
        Admin.logout();
    }

    @UiHandler("settings")
    public void settingsClick(ClickEvent event) {
        UserProfileWindow.showWindow();
    }

    public Widget asWidget() {
        return top;
    }    
}
