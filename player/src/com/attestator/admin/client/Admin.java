package com.attestator.admin.client;

import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.rpc.AdminAsyncRedirectingCallback;
import com.attestator.admin.client.rpc.AdminService;
import com.attestator.admin.client.rpc.AdminServiceAsync;
import com.attestator.admin.client.ui.AdminScreen;
import com.attestator.admin.client.ui.LoginScreen;
import com.attestator.admin.client.ui.RestorePasswordScreen;
import com.attestator.admin.client.ui.UserRegistrationScreen;
import com.attestator.common.client.helper.HistoryHelper;
import com.attestator.common.client.helper.HistoryHelper.HistoryToken;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.container.Viewport;

public class Admin implements EntryPoint {
    
    public  static final AdminServiceAsync RPC = GWT.create(AdminService.class);
    private static UserVO loggedUser;
    
    public static UserVO getLoggedUser() {
        return loggedUser;
    }

    public static void login(String email, String password, final AdminAsyncCallback<UserVO> callback) {
        Admin.RPC.login(email.trim(), password, new AdminAsyncRedirectingCallback<UserVO>(callback) {
            @Override
            public void onAfterSuccess(UserVO result) {
                if (result != null) {
                    loggedUser = result;
                    switchTo(AdminScreen.HISTORY_TOKEN);
                }
                else {
                    loggedUser = null;
                    switchTo(LoginScreen.HISTORY_TOKEN);
                }
            }
            @Override
            public void onAfterFailure(Throwable caught) {
                loggedUser = null;
                switchTo(LoginScreen.HISTORY_TOKEN);
            }
        });        
    }
    
    public static void logout(final AdminAsyncCallback<Void> callback) {
        Admin.RPC.logout(new AdminAsyncRedirectingCallback<Void>(callback) {
            @Override
            public void onAfterSuccess(Void result) {
                loggedUser = null;
                switchTo(LoginScreen.HISTORY_TOKEN);
            }
            @Override
            public void onAfterFailure(Throwable caught) {
                loggedUser = null;
                switchTo(LoginScreen.HISTORY_TOKEN);
            }
        });
    }
    
    public static void refreshLoggedUser(final AdminAsyncCallback<UserVO> callback) {
        Admin.RPC.getLoggedUser(new AdminAsyncRedirectingCallback<UserVO>(callback) {
            @Override
            public void onBeforeSuccess(UserVO result) {
                loggedUser = result;
            }
            
            @Override
            public void onBeforeFailure(Throwable caught) {
                loggedUser = null;
            }
        });
    }
    
    private static String currentTokenStr = null;
    
    private static String toValidToken(String token) {
        if (loggedUser != null) {            
            return AdminScreen.HISTORY_TOKEN;
        }
        
        if (!NullHelper.nullSafeEquals(AdminScreen.HISTORY_TOKEN, token)
        &&  !NullHelper.nullSafeEquals(LoginScreen.HISTORY_TOKEN, token)
        &&  !NullHelper.nullSafeEquals(UserRegistrationScreen.HISTORY_TOKEN, token)
        &&  !NullHelper.nullSafeEquals(RestorePasswordScreen.HISTORY_TOKEN, token)) {
            return LoginScreen.HISTORY_TOKEN;
        }
        
        return token;
    }
    
    public static void navigateTo(String tokenStr) {
        String   url = Window.Location.getHref();
        String[] urlParts = url.split("#", 2);
        url = urlParts[0] + "#" + tokenStr;
        Window.Location.assign(url);
    }
    
    private static void switchTo(String tokenStr) {        
        String validTokenStr = toValidToken(tokenStr);
        if (NullHelper.nullSafeEquals(currentTokenStr, validTokenStr)) {
            return;
        }
        
        HistoryToken token = new HistoryToken(validTokenStr);
        
        IsWidget newMainScreen = null;                 
        if (UserRegistrationScreen.HISTORY_TOKEN.equals(token.getName())) {
            newMainScreen = new UserRegistrationScreen();
        }
        else if (AdminScreen.HISTORY_TOKEN.equals(token.getName())) {
            newMainScreen = new AdminScreen();
        }
        else if (LoginScreen.HISTORY_TOKEN.equals(token.getName())) {
            newMainScreen = new LoginScreen();
        }
        else if (RestorePasswordScreen.HISTORY_TOKEN.equals(token.getName())) {
            newMainScreen = new RestorePasswordScreen();
        }
        
        if (newMainScreen != null) {
            switchTo(newMainScreen);
            currentTokenStr = tokenStr;
        }
    }

    private static void switchTo(IsWidget screen) {
        RootPanel.get().clear();
        Viewport viewport = new Viewport();
        viewport.setWidget(screen);
        RootPanel.get().add(viewport);
    }
    
    @Override
    public void onModuleLoad() {        
        History.addValueChangeHandler(new ValueChangeHandler<String>() {            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                switchTo(event.getValue());
            }
        });
        
        refreshLoggedUser(new AdminAsyncCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO result) {
                switchTo(HistoryHelper.getAnchor(Window.Location.getHref()));
            }
        });
    }
}
