package com.attestator.admin.client.ui;

import java.util.Set;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.helper.Validator;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.common.shared.dto.UserValidationError;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

public class UserRegistrationScreen implements IsWidget {
    public static String HISTORY_TOKEN = "userRegistration";

    interface UiBinderImpl extends UiBinder<Widget, UserRegistrationScreen> {
	}

	private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
	
	@UiField
	protected TextField username;
	
	@UiField
	protected TextField email;

	@UiField
	protected PasswordField password;
    
	@UiField
    protected PasswordField passwordAgain;

	@UiField
	TextButton registerButton;

	@Override
	public Widget asWidget() {
		Widget result = uiBinder.createAndBindUi(this);

		return result;
	}
	
	@UiHandler("registerButton")
	public void registerButtonClick(SelectEvent event) {
	    final Validator validator = validate();
	    
	    if (!validator.isValid()) {
	        validator.showAlert();
	        return;
	    }
	    
        Admin.RPC.validateForCreateNewUser(email.getValue(), username.getValue(), password.getValue(), new AdminAsyncCallback<Set<UserValidationError>>() {
            @Override
            public void onSuccess(Set<UserValidationError> result) {
                for (UserValidationError ve: result) {
                    switch (ve) {
                    case emailAlreadyExists:
                        validator.addError("Такой email уже используется другим пользователем", email);
                        break;
                    
                    case usernameAlreadyExists:
                        validator.addError("Такой логин уже используется другим пользователем", email);
                        break;
                    
                    case incorrectEmail:
                        validator.addError("Неверный email", email);
                        break;
                    
                    case incorrectUsername:
                        validator.addError("Логин может содержать только строчные латинские буквы, цифры и знак \"-\"", username);
                        validator.addError("Логин не может начинаться с цифры", username);
                        break;
                        
                    default:
                        validator.addError("Ошибка регистрации", null);
                        break;
                    }
                }
                
                if (!validator.isValid()) {
                    validator.showAlert();
                    return;
                }
                
                Admin.RPC.createNewUser(email.getValue(), username.getValue(), password.getValue(), new AdminAsyncCallback<UserVO>() {
                    @Override
                    public void onSuccess(UserVO result) {
                        Admin.login(email.getValue(), password.getValue(), null);
                    }
                });
            }            
        });
	}
	
	private Validator validate() {
	    final Validator vb = new Validator();
	    
	    if (StringHelper.isEmptyOrNull(username.getValue())) {
	        vb.addError("Логин не может быть пустым", username);
	    } 

        if (StringHelper.isEmptyOrNull(email.getValue())) {
            vb.addError("Email не может быть пустым", email);
        } 
        
        if (StringHelper.isEmptyOrNull(password.getValue())) {
            vb.addError("Пароль не может быть пустым", password);
        }
        else if (!NullHelper.nullSafeEquals(password.getValue(), passwordAgain.getValue())) {
            vb.addError("Пароли не совпадают", password);
        }

        return vb;
	}
}
