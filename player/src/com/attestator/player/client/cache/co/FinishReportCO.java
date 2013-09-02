package com.attestator.player.client.cache.co;

import java.util.Date;

import com.attestator.common.shared.vo.InterruptionCauseEnum;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class FinishReportCO implements JsonSerializable {
    private String  tenantId;
    private String  reportId;
    private Date end;
    private InterruptionCauseEnum interruptionCause;
    
    public FinishReportCO() {
    }
    public FinishReportCO(String tenantId, String reportId, Date finish, InterruptionCauseEnum interruptionCause) {
        super();
        this.tenantId = tenantId;
        this.reportId = reportId;
        this.interruptionCause = interruptionCause;
        this.end = finish;
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
    public InterruptionCauseEnum getInterruptionCause() {
        return interruptionCause;
    }
    public void setInterruptionCause(InterruptionCauseEnum interruptionCause) {
        this.interruptionCause = interruptionCause;
    }
    public Date getEnd() {
        return end;
    }
    public void setEnd(Date finish) {
        this.end = finish;
    }    
}
