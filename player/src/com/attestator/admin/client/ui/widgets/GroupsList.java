package com.attestator.admin.client.ui.widgets;

import java.util.Iterator;

import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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

public class GroupsList extends Composite implements IsEditor<ListEditor<GroupVO, GroupsListItem>>{
    private class GroupEditorSource extends EditorSource<GroupsListItem> {
        @Override
        public GroupsListItem create(final int index) {
            final GroupsListItem item = new GroupsListItem();
            itemsContainer.insert(item, index, maxWidthMinHeightVLData);            
            itemsContainer.forceLayout();
            
            item.addDeleteHandler(new DeleteHandler() {                
                @Override
                public void onDelete(DeleteEvent event) {
                    if (listEditor.getList().size() > 1) {
                        removeItem((GroupsListItem)event.getSource());
                    }
                }
            });
            
            return item;
        }
        
        @Override
        public void dispose(GroupsListItem item) {
            item.removeFromParent();
            itemsContainer.forceLayout();
        }
        
        @Override 
        public void setIndex(GroupsListItem item, int index) {
            itemsContainer.insert(item, index, maxWidthMinHeightVLData);
            itemsContainer.forceLayout();
        }
    }
    
    interface UiBinderImpl extends UiBinder<Widget, GroupsList> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    protected VerticalLayoutContainer topContainer;
    
    @UiField
    protected VerticalLayoutContainer itemsContainer;
    
    @UiField
    protected VerticalLayoutData maxWidthMinHeightVLData;
    
    // This source backs the ListEditor, which adds,inserts and removes child widgets from the list.
    private ListEditor<GroupVO, GroupsListItem> listEditor = ListEditor.of(new GroupEditorSource());
    
    @UiConstructor
    public GroupsList() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    private void removeItem(GroupsListItem item) {
        int index = listEditor.getEditors().indexOf(item);
        listEditor.getList().remove(index);
    }
    
    @Override
    public ListEditor<GroupVO, GroupsListItem> asEditor() {
        return listEditor;
    }
    
    @UiHandler("addButton")
    public void addButtonClick(SelectEvent event) {
        listEditor.getList().add(new GroupVO());
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                itemsContainer.getScrollSupport().scrollToBottom();
            }
        });
    }
    
    @Ignore
    public GroupsListItem getEmptyGroupItem() {
        Iterator<Widget> iterator = itemsContainer.iterator();
        while (iterator.hasNext()) {
            GroupsListItem item = (GroupsListItem)iterator.next();
            if (StringHelper.isEmptyOrNull(item.getNameField().getText())) {
                return item;
            }
        }
        return null;
    }
    
    @Ignore
    public VerticalLayoutContainer getItemsContainer() {
        return itemsContainer;
    }
}
