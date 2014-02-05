package com.attestator.common.shared.vo;

public class UpdateAllMetattestsVisiblityLockVO extends LockVO {
    private static final long serialVersionUID = 7499944704338636245L;
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UpdateAllMetattestsVisiblityLockVO(String tenantId) {
        super();
        this.tenantId = tenantId;
    }

    public UpdateAllMetattestsVisiblityLockVO() {        
    }
}
