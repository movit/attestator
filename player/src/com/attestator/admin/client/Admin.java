package com.attestator.admin.client;

//Now
//TODO Backup sources!!!!!

//TODO Order reports by startDate by default
//TODO Admin errors+unanswered report mode
//TODO Admin add middleName column
//TODO Login screen login by "enter" button click
//TODO Max error counter to interrupt test

//TODO Buy Jelasstic hosting

//TODO Move questions from publication to report

//TODO Player offline mode 
//TODO Player several browser instances go online from one machine. What happens with calls queue?
//TODO Reset attempts counter without deleting reports
//TODO Opera disable text selection

//TODO File format check
//TODO Optimize report update

//Later

//TODO Firefox copy paste to search box do not start search
//TODO Error message about unsuccessful login
//TODO User registration
//TODO Multydomain system
//TODO Save grids state
//TODO Admin userfriendly exceptions
//TODO Admin session timeout
//TODO Show number of questions in groups where useful
//TODO Add indexes
//TODO Password encryption
//TODO XSRF Token
//TODO Database backup
//TODO ClientID using evercookie
//TODO Report printing
//TODO Better report formatting
//TODO Tests sharing between clients 
//TODO Test editing screen
//TODO Study mode. (Show correct answers)
//TODO Build wars automatically
//TODO Client Id ban
//TODO My profile edit window
//TODO Logo and icons

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
