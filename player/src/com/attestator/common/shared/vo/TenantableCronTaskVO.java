package com.attestator.common.shared.vo;

import java.util.Date;

public class TenantableCronTaskVO extends CronTaskVO {
    private static final long serialVersionUID = -3487746947561680520L;
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public TenantableCronTaskVO() {
    }

    public TenantableCronTaskVO(Date time) {
        super(time);
    }
}
