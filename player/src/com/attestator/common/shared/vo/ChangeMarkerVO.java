package com.attestator.common.shared.vo;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.google.code.morphia.annotations.Entity;

@Entity("changemarker")
public class ChangeMarkerVO extends TenantableVO {
    private static final long serialVersionUID = -6155856555799173610L;
    private Date time = new Date();
    private Map<String, String> key = new TreeMap<String, String>();
    
    public ChangeMarkerVO() {
    }    
    public ChangeMarkerVO(String tenantId, String ... entries) {
        setTenantId(tenantId);        
        for (int i = 0; i < entries.length - 1; i += 2) {
            key.put(entries[i], entries[i+1]);
        }
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
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
        return key.isEmpty();
    }
}
