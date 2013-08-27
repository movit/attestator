package com.attestator.player.client.cache.co;

import com.attestator.common.shared.vo.ReportVO;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class StartReportCO implements JsonSerializable {
    private String tenantId;
    private ReportVO report;
    
    public StartReportCO() {
        super();
    }
    public StartReportCO(String tenantId, ReportVO report) {
        super();
        this.tenantId = tenantId;
        this.report = report;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public ReportVO getReport() {
        return report;
    }
    public void setReport(ReportVO report) {
        this.report = report;
    }
}
