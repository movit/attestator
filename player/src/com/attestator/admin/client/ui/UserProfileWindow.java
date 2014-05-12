package com.attestator.admin.client.ui;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;

public class UserProfileWindow implements IsWidget, Editor<UserVO>{
    interface DriverImpl extends
            SimpleBeanEditorDriver<UserVO, UserProfileWindow> {
    }    
    
    interface UiBinderImpl extends UiBinder<Widget, UserProfileWindow> {
    }

    private DriverImpl driver = GWT.create(DriverImpl.class);    
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    Window window;
    
    @UiField
    protected TextField email;    
    
    @UiField
    @Ignore
    protected PasswordField oldPassword;
    
    @UiField
    @Ignore
    protected PasswordField newPassword;

    @UiField
    @Ignore
    protected PasswordField newPasswordAgain;
    
    @UiField
    @Ignore
    protected TextButton saveButton;    
    @UiField
    @Ignore
    protected TextButton cancelButton;
    
    private UserProfileWindow(UserVO user) {
        super();        

        uiBinder.createAndBindUi(this);
        
        driver.initialize(this);
        driver.edit(user);
    }
    
    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }

    @UiHandler("saveButton")
    protected void saveButtonClick(SelectEvent event) {
        String oldPasswordStr = oldPassword.getValue();
        String newPasswordStr = newPassword.getValue();
        String newPasswordAgainStr = newPasswordAgain.getValue();
        
        if (!StringHelper.allEmptyOrNull(oldPasswordStr, newPasswordStr, newPasswordAgainStr)) {
            Admin.RPC.isThisLoggedUserPassword(oldPasswordStr, new AdminAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (NullHelper.nullSafeTrue(result)) {
                        validateAndSave();
                    }
                    else {
                        AlertMessageBox alert = new AlertMessageBox("Ошибка", "Неверный пароль");
                        alert.show();
                    }
                }
            });            
        }
        else {
            validateAndSave();
        }
    }
    
    private void validateAndSave() {
        if (validate()) {
            String oldPasswordStr = oldPassword.getValue();
            String newPasswordStr = newPassword.getValue();
            String emailStr = email.getValue();
            
            Admin.RPC.updateLoggedUser(oldPasswordStr, emailStr, newPasswordStr, new AdminAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Admin.refreshLoggedUser();
                    window.hide();
                }
            });
        }
    }
    
    private boolean validate() {
        String oldPasswordStr = oldPassword.getValue();
        String newPasswordStr = newPassword.getValue();
        String newPasswordAgainStr = newPasswordAgain.getValue();
        String emailStr = email.getValue();

        StringBuilder sb = new StringBuilder();        
        Component focusWidget = null;        
        if (!StringHelper.allEmptyOrNull(oldPasswordStr, newPasswordStr, newPasswordAgainStr)) {
            if (!StringHelper.nullAndEmptySafeEquals(newPasswordStr, newPasswordAgainStr)) {
                sb.append("Поля <b>Новый пароль</b> и <b>Новый пароль еще раз</b> не совпадают.<br/>");
                focusWidget = newPassword;
            }
        }
        
        if (!emailStr.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            sb.append("Неверный <b>Email</b>");
            if (focusWidget == null) {
                focusWidget = email;
            }
        }
        
        if (sb.length() > 0) {
            AlertMessageBox alert = new AlertMessageBox("Ошибка", sb.toString());
            
            if (focusWidget != null) {
                final Component finalFocusWiget = focusWidget;
                alert.addHideHandler(new HideHandler() {
                    @Override
                    public void onHide(HideEvent event) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                            
                            @Override
                            public void execute() {
                                finalFocusWiget.focus();
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

    public static void showWindow() {
        UserProfileWindow window = new UserProfileWindow(Admin.getLoggedUser());
        window.asWidget().show();
    }
}
