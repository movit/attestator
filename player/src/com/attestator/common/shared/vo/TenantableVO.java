package com.attestator.common.shared.vo;

import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Indexes({@Index(name = "tenantAndId", value = "tenantId, _id")})
public abstract class TenantableVO extends BaseVO {
	private static final long serialVersionUID = 2220524524737242868L;
	
	private String tenantId;
	
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
