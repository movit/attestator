package com.attestator.player.client;

import com.attestator.common.client.helper.WindowHelper;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.player.client.cache.PlayerStorageServiceAsync;
import com.attestator.player.client.helper.ApplicationCache;
import com.attestator.player.client.helper.HistoryHelper;
import com.attestator.player.client.helper.HistoryHelper.HistoryToken;
import com.attestator.player.client.rpc.PlayerService;
import com.attestator.player.client.rpc.PlayerServiceAsync;
import com.attestator.player.client.ui.PublicationsScreen;
import com.attestator.player.client.ui.ReportScreen;
import com.attestator.player.client.ui.TestScreen;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Player implements EntryPoint {
    public static int VERSION = 9;
    
    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    public static PlayerServiceAsync rpc;

    private void switchTo(String tokenStr) {
        HistoryToken token = new HistoryToken(tokenStr);
        
        MainScreen newMainScreen;                 
        if (TestScreen.HISTORY_TOKEN.equals(token.getName())) {
            newMainScreen = TestScreen.instance();
        }
        else if (ReportScreen.HISTORY_TOKEN.equals(token.getName())) {
            newMainScreen = ReportScreen.instance();
        }
        else {
            newMainScreen = PublicationsScreen.instance();
        }
        
        newMainScreen.init(token);
        RootPanel.get().getElement().addClassName(Resources.STYLES.playerCss().noSelection());
        RootPanel.get().clear();
        RootPanel.get().add(newMainScreen);
    }
    
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        refreshCacheAndReload(new UpToDateHandler() {            
            @Override
            public void onApllicationIsUpTodate() {
                PlayerServiceAsync rpc = GWT.create(PlayerService.class);
                if (Storage.isLocalStorageSupported()) {
                    Player.rpc = new PlayerStorageServiceAsync(rpc);
                }
                else {
                    Player.rpc = rpc;
                }
                
//                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//                    @Override
//                    public void execute() {                    
//                        Info.display("Version", "VERSION: " + VERSION);
//                    }
//                });
                
                History.addValueChangeHandler(new ValueChangeHandler<String>() {            
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        switchTo(event.getValue());
                    }
                });
                
                switchTo(HistoryHelper.getAnchor(Window.Location.getHref()));                
            }
        });
    }
    
    private static interface UpToDateHandler {
        public void onApllicationIsUpTodate();
    }
    
    private void refreshCacheAndReload(final UpToDateHandler upToDateHandler) {
        if (Cookies.getCookie("RELOAD") != null) {
            reloadApplication(upToDateHandler);
            return;
        }        
        
        if (!ApplicationCache.isSupported()) {
            reloadApplication(upToDateHandler);
            return;
        }
        
        if (!ApplicationCache.getApplicationCache().isOnline()) {
            reloadApplication(upToDateHandler);
            return;
        }
        
        EventListener reloadApplication = new EventListener() {            
            @Override
            public void onBrowserEvent(Event event) {
                reloadApplication(upToDateHandler);                
            }
        };  
        
        ApplicationCache.getApplicationCache().addEventListener(ApplicationCache.ONUPDATEREADY, reloadApplication, false);
        ApplicationCache.getApplicationCache().addEventListener(ApplicationCache.ONNOUPDATE, reloadApplication, false);
        ApplicationCache.getApplicationCache().addEventListener(ApplicationCache.ONERROR, reloadApplication, false);
        ApplicationCache.getApplicationCache().addEventListener(ApplicationCache.ONCACHED, reloadApplication, false);
        
        ApplicationCache.getApplicationCache().update();
    }
    
    private void reloadApplication(UpToDateHandler upToDateHandler) {        
        if (Cookies.getCookie("RELOAD") != null) {
            Cookies.removeCookie("RELOAD");
            upToDateHandler.onApllicationIsUpTodate();
        }
        else {
            Cookies.setCookie("RELOAD", "1");
            WindowHelper.forceReload();
        }
    }
}
