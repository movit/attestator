package com.attestator.player.client.cache.co;

import java.util.Date;

import com.attestator.common.shared.vo.ReportVO;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class StartReportCO implements JsonSerializable {
    private Date start;
    private String tenantId;
    private ReportVO report;
    
    public StartReportCO() {
        super();
    }
    public StartReportCO(String tenantId, ReportVO report, Date start) {
        super();
        this.tenantId = tenantId;
        this.report = report;
        this.start = start;
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
    public Date getStart() {
        return start;
    }
    public void setStart(Date start) {
        this.start = start;
    }    
}
