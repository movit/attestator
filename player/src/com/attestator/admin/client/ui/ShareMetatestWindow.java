package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.helper.WidgetHelper;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.HasSaveEventHandlers;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.SharingEntriesList;
import com.attestator.common.shared.helper.DateHelper;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.SharingEntryVO;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;

public class ShareMetatestWindow implements IsWidget, Editor<MetaTestVO>, HasSaveEventHandlers<MetaTestVO>,  HasHideHandlers {
    interface DriverImpl extends
            SimpleBeanEditorDriver<MetaTestVO, ShareMetatestWindow> {
    }

    interface UiBinderImpl extends UiBinder<Widget, ShareMetatestWindow> {
    }
    
    private DriverImpl driver = GWT.create(DriverImpl.class);    
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    Window window;
    
    @UiField
    VerticalLayoutContainer top;
    
    @UiField
    Label name;
    
    @UiField
    SharingEntriesList sharingEntries;
    
    private ShareMetatestWindow(MetaTestVO metatest) {
        super();
        uiBinder.createAndBindUi(this);
        driver.initialize(this);
        driver.edit(metatest);
    }

    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }

    private boolean validate(MetaTestVO metatest) {
        StringBuilder sb = new StringBuilder();
        Widget ensureVisibleWidget = null;

        for (SharingEntryVO sharingEntry: sharingEntries.getListStore().getAll()) {
            if (sharingEntry.getStart() != null && sharingEntry.getEnd() != null) {
                if (!DateHelper.afterOrEqualOrNull(sharingEntry.getEnd(), sharingEntry.getStart())) {
                    sb.append("Начало периода для пользователя " + sharingEntry.getUsername() + " должно быть раньше окончания" + "<br>");
                    if (ensureVisibleWidget == null) {
                        ensureVisibleWidget = sharingEntries;                        
                    }
                }
            }
        }
        
        if (sb.length() > 0) {
            AlertMessageBox alert = new AlertMessageBox("Ошибка", sb.toString());
            
            if (ensureVisibleWidget != null) {
                final Widget finalEnsureVisibleWidget = ensureVisibleWidget;
                alert.addHideHandler(new HideHandler() {
                    @Override
                    public void onHide(HideEvent event) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                            
                            @Override
                            public void execute() {
                                top.getScrollSupport().ensureVisible(finalEnsureVisibleWidget);
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
    public HandlerRegistration addSaveHandler(SaveHandler<MetaTestVO> handler) {
        return window.addHandler(handler, SaveEvent.getType());
    }

    @Override
    public HandlerRegistration addHideHandler(HideHandler handler) {
        return window.addHideHandler(handler);
    }
    
    public static void showWindow(String id, final SaveHandler<MetaTestVO> saveHandler, final HideHandler hideHandler) {
        final AdminAsyncCallback<MetaTestVO> showWindowCallback = new AdminAsyncCallback<MetaTestVO>() {
            @Override
            public void onSuccess(MetaTestVO result) {
                ShareMetatestWindow window = new ShareMetatestWindow(result);
                if (hideHandler != null) {
                    window.addHideHandler(hideHandler);
                }
                if (saveHandler != null) {
                    window.addSaveHandler(saveHandler);
                }
                window.asWidget().show();
            }
        };
        
        Admin.RPC.get(MetaTestVO.class.getName(), id, showWindowCallback);
    }    
    
    @UiHandler("saveButton")
    protected void saveButtonClick(SelectEvent event) {
        final MetaTestVO metatest = driver.flush();
        
        if (!validate(metatest)) {
            return;
        }
        
        // Save metatest sharing
        Admin.RPC.saveMetatestSharingEntries(metatest.getId(), metatest.getSharingEntries(), new AdminAsyncCallback<Void>() {            
            @Override
            public void onSuccess(Void result) {
                window.hide();
                fireEvent(new SaveEvent<MetaTestVO>(metatest));
            }            
        });
        
        
    }
}
