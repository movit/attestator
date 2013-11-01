package com.attestator.admin.client.ui;

import com.attestator.common.shared.vo.ReportVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

public class PrintWindow implements IsWidget {

    interface UiBinderImpl extends UiBinder<Window, PrintWindow> {
    }

    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    Window window;
    
    @UiField
    VerticalLayoutContainer top;
    
    public PrintWindow(ReportVO value) {
        uiBinder.createAndBindUi(this);
    }
    
    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }
    
    @Override
    public Window asWidget() {
        return window;
    }
}
