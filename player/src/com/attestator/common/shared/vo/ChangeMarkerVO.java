package com.attestator.common.shared.vo;

import java.util.Map;
import java.util.TreeMap;

import com.attestator.common.shared.helper.CheckHelper;
import com.google.code.morphia.annotations.Entity;

@Entity("changemarker")
public class ChangeMarkerVO extends TenantableVO {
    private static final long serialVersionUID = -6155856555799173610L;
    private String clientId;
    
    private CacheType type;
    
    private Map<String, String> key = new TreeMap<String, String>();
    
    public ChangeMarkerVO() {
    }
    
    public ChangeMarkerVO(String clientId, String tenantId, CacheType type, String ... entries) {
        CheckHelper.throwIfNull(tenantId, "tenantId");
        
        this.clientId = clientId;
        this.type = type;
        
        setTenantId(tenantId);
        
        for (int i = 0; i < entries.length - 1; i += 2) {
            key.put(entries[i], entries[i+1]);
        }
    }
    
    public CacheType getType() {
        return type;
    }
    public void setType(CacheType type) {
        this.type = type;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public Map<String, String> getKey() {
        return key;
    }
    public String getKeyEntry(String name) {
        return key.get(name);
    }
    public void setKey(Map<String, String> key) {
        this.key = key;
    }
    public boolean isGlobal() {
        return type == null;
    }
}
