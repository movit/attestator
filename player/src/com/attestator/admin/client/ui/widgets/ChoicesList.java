package com.attestator.admin.client.ui.widgets;

import java.util.Iterator;

import com.attestator.admin.client.helper.WidgetHelper;
import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.admin.client.ui.event.RearrangeEvent;
import com.attestator.admin.client.ui.event.RearrangeEvent.RearrangeHandler;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChoiceVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DisableEvent;
import com.sencha.gxt.widget.core.client.event.EnableEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

public class ChoicesList extends Composite implements IsEditor<ListEditor<ChoiceVO, ChoicesListItem>>{
    private class ChoiceEditorSource extends EditorSource<ChoicesListItem> {
        @Override
        public ChoicesListItem create(final int index) {
            ChoicesListItem item = new ChoicesListItem();
            
            choicesContainer.insert(item, index, maxWidthMinHeightVLData);
                        
            tg.add(item.getRight());
            
            item.addDeleteHandler(new DeleteHandler() {                
                @Override
                public void onDelete(DeleteEvent event) {
                    if (listEditor.getList().size() > 1) {
                        removeItem((ChoicesListItem)event.getSource());
                    }
                }
            });
            
            item.addRearrangeHandler(new RearrangeHandler() {                
                @Override
                public void onRearrange(RearrangeEvent event) {
                    moveItem((ChoicesListItem)event.getSource(), event.isUp());
                }
            });
            
            updateOrders();

            return item;
        }
        
        private ToggleGroup tg = new ToggleGroup();        
        
        @Override
        public void dispose(ChoicesListItem item) {
            tg.remove(item.getRight());
            item.removeFromParent();
            updateOrders();
        }
        
        @Override 
        public void setIndex(ChoicesListItem item, int index) {
            choicesContainer.insert(item, index, maxWidthMinHeightVLData);
            updateOrders();
            tg.add(item.getRight());
        }
    }
    
    interface UiBinderImpl extends UiBinder<Widget, ChoicesList> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    protected VerticalLayoutContainer topContainer;
    
    @UiField
    protected VerticalLayoutContainer choicesContainer;
    
    @UiField
    protected VerticalLayoutData maxWidthMinHeightVLData;
    
    @UiField
    protected TextButton addButton;
    
    // This source backs the ListEditor, which adds,inserts and removes child widgets from the list.
    private ListEditor<ChoiceVO, ChoicesListItem> listEditor = ListEditor.of(new ChoiceEditorSource());
    
    @UiConstructor
    public ChoicesList() {
        initWidget(uiBinder.createAndBindUi(this));
        updateOrders();
    }
    
    private void updateOrders() {
        for (int i = 0; i < choicesContainer.getWidgetCount(); i++) {
            ChoicesListItem item = (ChoicesListItem)choicesContainer.getWidget(i);
            item.order.setValue(i);
            item.deleteButton.setEnabled(choicesContainer.getWidgetCount() > 1);
        }
    }
    
    private void moveItem(ChoicesListItem item, boolean up) {
        int itemIndex = WidgetHelper.widgetIndex(choicesContainer, item);
        
        if (up) {
            if (itemIndex > 0) {
                choicesContainer.insert(item, itemIndex - 1);
            }
        }
        else {
            if (itemIndex < (choicesContainer.getWidgetCount() - 1)) {
                choicesContainer.insert(item, itemIndex + 2);
            }
        }
        updateOrders();
    }

    private void removeItem(ChoicesListItem item) {
        int index = listEditor.getEditors().indexOf(item);
        listEditor.getList().remove(index);
        updateOrders();
    }
    
    @Override
    public ListEditor<ChoiceVO, ChoicesListItem> asEditor() {
        return listEditor;
    }
    
    @UiHandler("addButton")
    public void addButtonClick(SelectEvent event) {
        listEditor.getList().add(new ChoiceVO());
        updateOrders();
    }
    
    @Ignore
    public ChoicesListItem getEmptyChoiceItem() {
        Iterator<Widget> iterator = choicesContainer.iterator();
        while (iterator.hasNext()) {
            ChoicesListItem item = (ChoicesListItem)iterator.next();
            if (StringHelper.isEmptyOrNull(item.text.getText())) {
                return item;
            }
        }
        return null;
    }
    
    @Override
    public void disable() {
        disabled = true;
        fireEvent(new DisableEvent());
        addButton.disable();
        for (int i = 0; i < choicesContainer.getWidgetCount(); i++) {
            ChoicesListItem item = (ChoicesListItem)choicesContainer.getWidget(i);
            item.disable();
        }
    }
    
    @Override
    public void enable() {
        disabled = false;
        fireEvent(new EnableEvent());
        addButton.enable();
        for (int i = 0; i < choicesContainer.getWidgetCount(); i++) {
            ChoicesListItem item = (ChoicesListItem)choicesContainer.getWidget(i);
            item.enable();
        }
    }
}
