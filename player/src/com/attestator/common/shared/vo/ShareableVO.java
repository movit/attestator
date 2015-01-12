package com.attestator.common.shared.vo;

import java.util.HashSet;
import java.util.Set;

import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import com.attestator.common.server.db.annotation.SetOnSave;

@Indexes({
    @Index(name = "idxSharedForTenantIdsAndId", value = "sharedForTenantIds, _id"),
    @Index(name = "idxOwnerUserName", value = "ownerUsername, _id")
})
public abstract class ShareableVO extends TenantableVO {
    private static final long serialVersionUID = -3351713766211842363L;
    private Set<String> sharedForTenantIds = new HashSet<String>();
    
    @SetOnSave(refField = "tenantId", targetClass = UserVO.class, targetIdField="tenantId", targetValueField = "username")
    private String ownerUsername;
    
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

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
