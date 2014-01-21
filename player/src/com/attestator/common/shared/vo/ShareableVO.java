package com.attestator.common.shared.vo;

import java.util.HashSet;
import java.util.Set;

import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Indexes({@Index(name = "sharedForTenantIdsAndId", value = "sharedForTenantIds, _id")})
public abstract class ShareableVO extends TenantableVO {
    private static final long serialVersionUID = -3351713766211842363L;
    private Set<String> sharedForTenantIds = new HashSet<String>();
    
    @Override
    public void resetIdentity() {        
        super.resetIdentity();
        sharedForTenantIds = new HashSet<String>();
    }
    
    public Set<String> getSharedForTenantIds() {
        return sharedForTenantIds;
    }
    
    public void setSharedForTenantIds(Set<String> sharedForTenantIds) {
        this.sharedForTenantIds = sharedForTenantIds;
    }
}
