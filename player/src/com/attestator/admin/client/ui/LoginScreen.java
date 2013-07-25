package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.attestator.common.shared.helper.StringHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

public class LoginScreen implements IsWidget {
    interface UiBinderImpl extends UiBinder<Widget, LoginScreen> {
    }

    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

    @UiField
    protected TextField email;
    
    @UiField
    protected PasswordField password;
    
    @Override
    public Widget asWidget() {
        return uiBinder.createAndBindUi(this);
    }

    @UiHandler("loginButton")
    public void loginButtonClick(SelectEvent event) {
        String email = this.email.getValue();
        String password = this.password.getValue();
        
        if (StringHelper.isEmptyOrNull(email) || StringHelper.isEmptyOrNull(password)) {
            return;            
        }
        
        Admin.login(email, password);
    }
}
