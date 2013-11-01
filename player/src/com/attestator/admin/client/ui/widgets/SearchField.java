package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.ui.event.FilterEvent;
import com.attestator.admin.client.ui.event.FilterEvent.FilterHandler;
import com.attestator.admin.client.ui.event.FilterEvent.HasFilterEventHandlers;
import com.attestator.common.shared.helper.NullHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.theme.base.client.field.StoreFilterFieldDefaultAppearance;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent.TriggerClickHandler;
import com.sencha.gxt.widget.core.client.form.TriggerField;

public class SearchField extends TriggerField<String> implements HasFilterEventHandlers<String> {

    static class SearchFieldCell extends TriggerFieldCell<String> {
        public SearchFieldCell() {
            super(GWT.<StoreFilterFieldDefaultAppearance> create(StoreFilterFieldDefaultAppearance.class));
        }
    }

    private String oldFilterValue = null;
    private String newFilterValue = null;
    
    /**
     * Creates a store filter field. Use {@link #bind(Store)} to bind the filter
     * to a store.
     */
    @UiConstructor
    public SearchField() {
        super(new SearchFieldCell());
        setAutoValidate(true);
        setValidateOnBlur(false);
        sinkEvents(Event.ONPASTE);

        redraw();
        
        addTriggerClickHandler(new TriggerClickHandler() {
            @Override
            public void onTriggerClick(TriggerClickEvent event) {
                SearchField.this.onTriggerClick(event);
            }
        });
    }
    
    @Override
    public void onBrowserEvent(Event event) {        
        super.onBrowserEvent(event);
        switch (event.getTypeInt()) {
        case Event.ONPASTE:
            // Force filtering 
            onKeyUp(null);
            break;
        }
    }

    
    protected void onFilter(String value) {
        if (!NullHelper.nullSafeEquals(oldFilterValue, value)) {
            newFilterValue = value;
            fireEvent(new FilterEvent<String>(value));
            oldFilterValue = value;
        }
        focus();
    }

    protected void onTriggerClick(TriggerClickEvent event) {
        setValue(null);
        // value may not have been updated if no blur so force text change
        // as filters work against current text, not the actual value
        setText("");
        onFilter(null);
    }

    protected boolean validateValue(String value) {
        boolean ret = super.validateValue(value);
        onFilter(value);
        return ret;
    }

    @Override
    public HandlerRegistration addFilterChangeHandler(
            FilterHandler<String> handler) {
        return addHandler(handler, FilterEvent.getType());
    }

    public String getNewFilterValue() {
        return newFilterValue;
    }
}
