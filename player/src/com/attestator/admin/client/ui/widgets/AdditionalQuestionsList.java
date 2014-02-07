package com.attestator.admin.client.ui.widgets;

import java.util.Iterator;

import com.attestator.admin.client.helper.WidgetHelper;
import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.admin.client.ui.event.RearrangeEvent;
import com.attestator.admin.client.ui.event.RearrangeEvent.RearrangeHandler;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
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
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

public class AdditionalQuestionsList extends Composite implements IsEditor<ListEditor<AdditionalQuestionVO, AdditionalQuestionItem>>{
    private class AdditonalQuestionEditorSource extends EditorSource<AdditionalQuestionItem> {
        @Override
        public AdditionalQuestionItem create(final int index) {
            AdditionalQuestionItem item = new AdditionalQuestionItem();
            aqContainer.insert(item, index, maxWidthMinHeightVLData);
                        
            item.addDeleteHandler(new DeleteHandler() {                
                @Override
                public void onDelete(DeleteEvent event) {
                    removeItem((AdditionalQuestionItem)event.getSource());
                }
            });
            
            item.addRearrangeHandler(new RearrangeHandler() {                
                @Override
                public void onRearrange(RearrangeEvent event) {
                    moveItem((AdditionalQuestionItem)event.getSource(), event.isUp());
                }
            });
            
            updateOrders();            

            return item;
        }
        
        @Override
        public void dispose(AdditionalQuestionItem item) {
            item.removeFromParent();
            updateOrders();
        }
        
        @Override 
        public void setIndex(AdditionalQuestionItem item, int index) {
            aqContainer.insert(item, index, maxWidthMinHeightVLData);
            updateOrders();
        }
    }
    
    interface UiBinderImpl extends UiBinder<Widget, AdditionalQuestionsList> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    protected VerticalLayoutContainer topContainer;
    
    @UiField
    protected VerticalLayoutContainer aqContainer;
    
    @UiField
    protected VerticalLayoutData maxWidthMinHeightVLData;
    
    // This source backs the ListEditor, which adds,inserts and removes child widgets from the list.
    private ListEditor<AdditionalQuestionVO, AdditionalQuestionItem> listEditor = ListEditor.of(new AdditonalQuestionEditorSource());
    
    @UiConstructor
    public AdditionalQuestionsList() {
        initWidget(uiBinder.createAndBindUi(this));
        updateOrders();
    }
    
    private void updateOrders() {
        for (int i = 0; i < aqContainer.getWidgetCount(); i++) {
            AdditionalQuestionItem item = (AdditionalQuestionItem)aqContainer.getWidget(i);
            item.order.setValue(i);
        }
    }
    
    private void moveItem(AdditionalQuestionItem item, boolean up) {
        int itemIndex = WidgetHelper.widgetIndex(aqContainer, item);
        
        if (up) {
            if (itemIndex > 0) {
                aqContainer.insert(item, itemIndex - 1);
            }
        }
        else {
            if (itemIndex < (aqContainer.getWidgetCount() - 1)) {
                aqContainer.insert(item, itemIndex + 2);
            }
        }
        updateOrders();
    }

    private void removeItem(AdditionalQuestionItem item) {
        int index = listEditor.getEditors().indexOf(item);
        listEditor.getList().remove(index);
        updateOrders();
    }
    
    @Override
    public ListEditor<AdditionalQuestionVO, AdditionalQuestionItem> asEditor() {
        return listEditor;
    }
    
    @UiHandler("addButton")
    public void addButtonClick(SelectEvent event) {
        listEditor.getList().add(new AdditionalQuestionVO());
        updateOrders();
    }
    
    @Ignore
    public AdditionalQuestionItem getAdditonalQuestionItem(int order) {
        Iterator<Widget> iterator = aqContainer.iterator();
        while (iterator.hasNext()) {
            AdditionalQuestionItem item = (AdditionalQuestionItem)iterator.next();
            if (item.order.getValue() == order) {
                return item;
            }
        }
        return null;
    }
}
