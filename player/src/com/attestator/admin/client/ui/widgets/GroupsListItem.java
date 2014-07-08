package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.admin.client.ui.event.DeleteEvent.HasDeleteEventHandlers;
import com.attestator.common.shared.vo.GroupVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class GroupsListItem extends Composite implements ValueAwareEditor<GroupVO>, HasDeleteEventHandlers {
    interface UiBinderImpl extends UiBinder<Widget, GroupsListItem> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    ToolBar container;
    
    @UiField
    @Ignore
    protected TextButton deleteButton;
    
    @UiField
    protected TextField name;

    @UiConstructor
    public GroupsListItem() {
        initWidget(uiBinder.createAndBindUi(this));                
    }
    
    @Override
    public final HandlerRegistration addDeleteHandler(DeleteHandler handler) {
        return addHandler(handler, DeleteEvent.getType());
    }    
    
    @UiHandler("deleteButton")
    public void deleteButtonClick(SelectEvent event) {
        fireEvent(new DeleteEvent());
    }

    @Ignore
    public TextButton getDeleteButton() {
        return deleteButton;
    }
    
    @Ignore 
    public TextField getNameField() {
        return name;
    }

    @Override
    public void setDelegate(EditorDelegate<GroupVO> delegate) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void onPropertyChange(String... paths) {
    }

    @Override
    public void setValue(GroupVO value) {
        if (Admin.getLoggedUser().getDefaultGroupId().equals(value.getId())) {
            deleteButton.disable();
        }
    }
    
    public ToolBar getContainter() {
        return container;
    }
}
