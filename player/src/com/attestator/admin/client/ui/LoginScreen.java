package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.rpc.AdminAsyncUnmaskCallback;
import com.attestator.common.client.helper.WindowHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.UserVO;
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
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

public class LoginScreen implements IsWidget {
	public static String HISTORY_TOKEN = "login";
	        
    interface UiBinderImpl extends UiBinder<Widget, LoginScreen> {
	}

	private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

	@UiField
	protected TextField email;

	@UiField
	protected PasswordField password;

	@UiField
	TextButton loginButton;

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
							password.focus();
						}
					});
				}
			}
		});
		
		password.addDomHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							loginButtonClick(null);
						}
					});
				}
			}
		}, KeyDownEvent.getType());

		return result;
	}

	@UiHandler("loginButton")
	public void loginButtonClick(SelectEvent event) {
		String email = this.email.getValue();
		String password = this.password.getValue();

		if (StringHelper.isEmptyOrNull(email)
				|| StringHelper.isEmptyOrNull(password)) {
			return;
		}
		
		WindowHelper.mask("Вход...");
		Admin.login(email, password, new AdminAsyncUnmaskCallback<UserVO>() {
		    @Override
		    public void onSuccess(UserVO result) {
		        super.onSuccess(result);
		        if (result == null) {
		            Scheduler.get().scheduleDeferred(new ScheduledCommand() {                        
                        @Override
                        public void execute() {
                            (new AlertMessageBox("Ошибка", "Неверный логин или пароль")).show();                            
                        }
                    });		            
		        }
		    }
		});
	}
}
