package com.attestator.admin.client;


import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.rpc.AdminService;
import com.attestator.admin.client.rpc.AdminServiceAsync;
import com.attestator.admin.client.ui.AdminScreen;
import com.attestator.admin.client.ui.LoginScreen;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.container.Viewport;

public class Admin implements EntryPoint {
    
    public  static final AdminServiceAsync RPC = GWT.create(AdminService.class);
    private static UserVO loggedUser;
    
    public static UserVO getLoggedUser() {
        return loggedUser;
    }

    public static void login(String email, String password) {
        Admin.RPC.login(email.trim(), password, new AdminAsyncCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO result) {
                if (result != null) {
                    refresh();
                }
            }
        });        
    }
    
    public static void logout() {
        Admin.RPC.logout(new AdminAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Admin.refresh();
            }
        });
    }
    
    public static void refreshLoggedUser() {
        Admin.RPC.getLoggedUser(new AdminAsyncCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO result) {
                loggedUser = result;                
            }
        });
    }
    
    public static void refresh() {
        Admin.RPC.getLoggedUser(new AdminAsyncCallback<UserVO>() {
            @Override
            public void onSuccess(UserVO result) {
                loggedUser = result;                
                if (loggedUser != null) {
                    switchToAdmin();
                }
                else {
                    switchToLogin();
                }
            }
        });
    }
    
    public static void switchToLogin() {
        switchTo(new LoginScreen());
    }

    
    public static void switchToAdmin() {
        switchTo(new AdminScreen());
    }
    
    public static void switchTo(IsWidget screen) {
        RootPanel.get().clear();
        Viewport viewport = new Viewport();
        viewport.setWidget(screen);
        RootPanel.get().add(viewport);
    }
    
    @Override
    public void onModuleLoad() {        
        refresh();
    }    
}
