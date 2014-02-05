package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Indexes({@Index(name = "tenantAndId", value = "tenantId, _id")})
public abstract class TenantableVO extends ModificationDateAwareVO {
	private static final long serialVersionUID = 2220524524737242868L;
	
	private String tenantId;
	
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
