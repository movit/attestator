package com.attestator.admin.client.ui;

import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.HasSaveEventHandlers;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.GroupsList;
import com.attestator.admin.client.ui.widgets.GroupsListItem;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

public class GroupsWindow implements IsWidget, Editor<List<GroupVO>>, HasSaveEventHandlers<List<GroupVO>> {
    interface DriverImpl extends
        SimpleBeanEditorDriver<List<GroupVO>, GroupsWindow> {
    }

    interface UiBinderImpl extends UiBinder<Widget, GroupsWindow> {
    }

    private DriverImpl driver = GWT.create(DriverImpl.class);    
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

    @UiField
    Window window;
    
    @UiField
    VerticalLayoutContainer topContainer;
    
    @UiField
    @Path("")
    protected GroupsList groups;
    
    public GroupsWindow() {        
        uiBinder.createAndBindUi(this);        
        driver.initialize(this);
    }
    
    @Ignore
    public void show() {
        Admin.RPC.getGroups(new AdminAsyncCallback<List<GroupVO>>() {
            @Override
            public void onSuccess(List<GroupVO> result) {
                driver.edit(result);
                window.show();
            }
        });
    }

    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }

    @UiHandler("saveButton")
    protected void saveButtonClick(SelectEvent event) {
        final List<GroupVO> groups = driver.flush();
        
        if (validate(groups)) {
            Admin.RPC.setGroups(groups, new AdminAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    fireEvent(new SaveEvent<List<GroupVO>>(groups));
                }
            });
            window.hide();
        }
    }
    
    private boolean validate(List<GroupVO> groups) {
        StringBuilder sb = new StringBuilder();
        
        Widget ensureVisibleWidget = null;
        Component focusWidget = null;
        
        for (GroupVO group: groups) {
            if (StringHelper.isEmptyOrNull(group.getName())) {
                sb.append("Название группы не может быть пустым" + "<br>");

                if (ensureVisibleWidget == null) {
                    GroupsListItem emptyItem = this.groups.getEmptyGroupItem();
                    if (emptyItem != null) {
                        ensureVisibleWidget = emptyItem;
                        focusWidget = emptyItem.getNameField();
                    }
                }
                
                break;
            }
        }
        
        if (sb.length() > 0) {
            AlertMessageBox alert = new AlertMessageBox("Ошибка", sb.toString());
            
            if (ensureVisibleWidget != null) {
                final Widget finalEnsureVisibleWidget = ensureVisibleWidget;
                final Component finalFocusWiget = focusWidget;
                alert.addHideHandler(new HideHandler() {
                    @Override
                    public void onHide(HideEvent event) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                            
                            @Override
                            public void execute() {
                                GroupsWindow.this.groups.
                                    getItemsContainer().
                                        getScrollSupport(). 
                                            ensureVisible(finalEnsureVisibleWidget);
                                if (finalFocusWiget != null) {
                                    finalFocusWiget.focus();
                                }
                            }
                        });
                    }
                });
            }
            
            alert.show();
            return false;
        }
        
        return true;
    }
    
    @Override
    public Window asWidget() {
        return window;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        window.fireEvent(event);        
    }

    @Override
    public HandlerRegistration addSaveHandler(SaveHandler<List<GroupVO>> handler) {
        return window.addHandler(handler, SaveEvent.getType());
    }
}
