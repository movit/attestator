package com.attestator.player.client.cache.co;

import java.util.Date;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class TenantCacheVersionCO implements JsonSerializable {
    private String tenantId;
    private Date   time;
    
    public TenantCacheVersionCO() {
    }

    public TenantCacheVersionCO(String tenantId, Date time) {
        super();
        this.tenantId = tenantId;
        this.time = time;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    
}
