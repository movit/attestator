package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.RearrangeEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.admin.client.ui.event.DeleteEvent.HasDeleteEventHandlers;
import com.attestator.admin.client.ui.event.RearrangeEvent.HasRearrangeEventHandlers;
import com.attestator.admin.client.ui.event.RearrangeEvent.RearrangeHandler;
import com.attestator.common.shared.vo.ChoiceVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextField;

public class ChoicesListItem extends Composite implements Editor<ChoiceVO>, HasDeleteEventHandlers, HasRearrangeEventHandlers {
    interface UiBinderImpl extends UiBinder<Widget, ChoicesListItem> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    @Ignore
    protected TextButton deleteButton;
    
    @UiField
    protected TextField text;

    @UiField
    protected Radio right;
    
    protected SimpleEditor<Integer> order = SimpleEditor.of();
    
    @UiConstructor
    public ChoicesListItem() {
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
    
    @UiHandler("deleteButton")
    public void deleteButtonClick(SelectEvent event) {
        fireEvent(new DeleteEvent());
    }

    @UiHandler("upButton")
    public void upButtonClick(SelectEvent event) {
        fireEvent(new RearrangeEvent(true));
    }

    @UiHandler("downButton")
    public void downButtonClick(SelectEvent event) {
        fireEvent(new RearrangeEvent(false));
    }
    
    @Ignore
    public Radio getRight() {
        return right;
    }
    
    @Ignore
    public TextButton getDeleteButton() {
        return deleteButton;
    }
    
    @Ignore 
    public TextField getTextField() {
        return text;
    }
}
