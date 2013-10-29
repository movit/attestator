package com.attestator.admin.client.ui.widgets.tabs;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.Composite;

public class Tab extends Composite implements HasSelectionHandlers<Tab> {
    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Tab> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }
}
