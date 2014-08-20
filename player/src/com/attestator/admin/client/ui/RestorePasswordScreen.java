package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.widgets.InfoMessageBox;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;

public class RestorePasswordScreen implements IsWidget {
	public static String HISTORY_TOKEN = "restorePassword";
	        
    interface UiBinderImpl extends UiBinder<Widget, RestorePasswordScreen> {
	}

	private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

	@UiField
	protected TextField email;

	@UiField
	TextButton sendPasswordButton;

	@Override
	public Widget asWidget() {
		Widget result = uiBinder.createAndBindUi(this);

		email.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            sendPasswordButtonClick(null);
                        }
                    });
				}
			}
		});
		
		return result;
	}

	@UiHandler("sendPasswordButton")
	public void sendPasswordButtonClick(SelectEvent event) {
		final String email = this.email.getValue();

		if (StringHelper.isEmptyOrNull(email)) {
			return;
		}
		
		Admin.RPC.isThisEmailExists(email, new AdminAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (!NullHelper.nullSafeTrue(result)) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {                        
                        @Override
                        public void execute() {
                            (new AlertMessageBox("Ошибка", "Пользователь с этим email не зарегистрирован.")).show();                            
                        }
                    });                 
                    return;
                }
                
                Admin.RPC.restorePassword(email, new AdminAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                        
                            @Override
                            public void execute() {
                                (new InfoMessageBox("Пароль изменен", "Новый пароль отправлен на " + email){
                                    @Override
                                    protected void onHide() {
                                        super.onHide();
                                        Admin.navigateTo(LoginScreen.HISTORY_TOKEN);
                                    }
                                }).show();
                            }
                        });                 
                    }
                });
            }
        });
	}
}
