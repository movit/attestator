package com.attestator.player.client;

import java.util.ArrayList;

import com.attestator.player.client.helper.HistoryHelper;
import com.attestator.player.client.helper.HistoryHelper.HistoryToken;
import com.google.gwt.user.client.ui.IsWidget;

public abstract class MainScreen implements IsWidget {
    private String tenantId;
    
    public final String getTenantId() {
        return tenantId;
    } 
    
    public final String newToken(String tokenName, String ... params) {
        ArrayList<String> paramsArray = new ArrayList<String>();
        paramsArray.add("t");
        paramsArray.add(getTenantId());
        for (String param: params) {
            paramsArray.add(param);
        }
        String result = HistoryHelper.newToken(tokenName, paramsArray.toArray(new String[0]));
        return result;
    }

    public final void init(HistoryToken token) {
        tenantId = token.getProperties().get("t");
        initContent(token);
    }
    
    public abstract void initContent(HistoryToken token);
}
