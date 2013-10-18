package com.attestator.admin.client.ui.widgets;


import java.util.Date;

import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.admin.client.ui.event.DeleteEvent.HasDeleteEventHandlers;
import com.attestator.admin.client.ui.event.RearrangeEvent;
import com.attestator.admin.client.ui.event.RearrangeEvent.HasRearrangeEventHandlers;
import com.attestator.admin.client.ui.event.RearrangeEvent.RearrangeHandler;
import com.attestator.common.shared.helper.DateHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.TimeField;

public class DateTimeSelector extends Composite implements LeafValueEditor<Date>, HasDeleteEventHandlers, HasRearrangeEventHandlers {
    interface UiBinderImpl extends UiBinder<Widget, DateTimeSelector> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField(provided=true)
    DateTimePropertyEditor datePropertyEditor = new DateTimePropertyEditor(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM));
    
    @UiField    
    protected DateField dateField;

    @UiField
    protected TimeField timeField;
    
    @UiConstructor
    public DateTimeSelector() {
        initWidget(uiBinder.createAndBindUi(this));                
    }
    
    @Override
    public final HandlerRegistration addDeleteHandler(DeleteHandler handler) {
        return addHandler(handler, DeleteEvent.getType());
    }
    
    @Override
    public HandlerRegistration addRearrangeHandler(RearrangeHandler handler) {
        return addHandler(handler, RearrangeEvent.getType());
    }

    @UiHandler("clearButton")
    public void clearButtonClick(SelectEvent event) {        
        dateField.focus();
        timeField.clear();
        timeField.focus();
        dateField.clear();
        dateField.focus();
    }    
    
    @Override
    public void setValue(Date value) {
        if (value != null) {
            dateField.setValue(value);
            timeField.setValue(value);
        }
        else {
            dateField.setValue(null);
            timeField.setValue(null);
        }
    }

    @Override
    public Date getValue() {
        Date result = null;
        if (dateField.getValue() != null) {
            if (timeField.getValue() != null) {
                long resultTime = DateHelper.getDatePart(dateField.getValue()) + DateHelper.getTimePart(timeField.getValue()); 
                if (resultTime < dateField.getValue().getTime()) {
                    resultTime += 1000 * 60 * 60 * 24;
                }
                result = new Date(resultTime);
            }
            else {
                result = dateField.getValue();
            }
        }
        return result;
    } 
    
}