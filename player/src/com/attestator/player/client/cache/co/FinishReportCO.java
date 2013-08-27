package com.attestator.player.client.cache.co;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class FinishReportCO implements JsonSerializable {
    private String  tenantId;
    private String  reportId;
    private boolean interrupted;
    
    public FinishReportCO() {
    }
    public FinishReportCO(String tenantId, String reportId, boolean interrupted) {
        super();
        this.tenantId = tenantId;
        this.reportId = reportId;
        this.interrupted = interrupted;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public String getReportId() {
        return reportId;
    }
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    public boolean isInterrupted() {
        return interrupted;
    }
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }    
}
