package com.attestator.common.client.helper;

import java.util.HashMap;
import java.util.Map;

import com.attestator.common.shared.helper.StringHelper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.History;

public class HistoryHelper {
    public static class HistoryToken{
        private String                 name = "";
        private Map<String, String>    properties = new HashMap<String, String>();
        
        public HistoryToken(String tokenString) {
            if (tokenString == null) {
                name = "";
                return;
            }
            
            String[] parts = tokenString.split("\\?", 2);
            name = parts[0];
            if (parts.length == 1) {
                return;
            }
            
            String[] params = parts[1].split("\\&");
            for (String param: params) {
                String[] paramParts = param.split("\\=", 2);
                if (paramParts.length == 1) {
                    properties.put(paramParts[0], "");
                }
                else {
                    properties.put(paramParts[0], paramParts[1]);
                }
            }
        }
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Map<String, String> getProperties() {
            return properties;
        }
        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }
    
    public static String newToken(String tokenName, String ... params) {
        if (tokenName == null) {
            tokenName = "";
        }
        StringBuilder sb = new StringBuilder(tokenName);
        
        int len = ((int)(params.length / 2)) * 2;
        for (int i = 0; i < len; i += 2 ) {
            if (i == 0) {
                sb.append("?");
            }
            else {
                sb.append("&");
            }
            
            sb.append(StringHelper.nullToEmptyString(params[i]));
            sb.append("=");
            sb.append(StringHelper.nullToEmptyString(params[i + 1]));
        }
        
        return sb.toString();
    }
    
    public static String getTokenName(String tokenString) {
        if (tokenString == null) {
            return null;
        }
        
        String[] parts = tokenString.split("\\?");
        return parts[0];
    }
    
    public static String getParam(String tokenString, String paramName) {
        if (tokenString == null) {
            return null;
        }
        
        if (paramName == null) {
            return null;
        }
        
        String[] parts = tokenString.split("\\?");
        if (parts.length == 1) {
            return null;
        }
        
        String[] params = parts[1].split("\\&");
        for (String param: params) {
            String[] paramParts = param.split("\\=");
            if (paramName.equals(paramParts[0])) {
                if (paramParts.length > 1) {
                    return paramParts[1];
                }
                else {
                    return "";
                }
            }
        }
        
        return null;
    }
    
    public static String getAnchor(String str) {
        String[] parts = str.split("\\#", 2);
        if (parts.length < 2) {
            return "";
        }
        else {
            return parts[1];
        }
    }
    
    public static void deferredHistoryItem(final String token) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                                
            @Override
            public void execute() {
                History.newItem(token);                                    
            }
        });
    }
}