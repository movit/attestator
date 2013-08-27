package com.attestator.player.client.cache.co;

import com.attestator.common.shared.vo.AnswerVO;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class AddAnswerCO implements JsonSerializable {
    private String tenantId;
    private String reportId;
    private AnswerVO answer;
    
    public AddAnswerCO() {
    }
    public AddAnswerCO(String tenantId, String reportId, AnswerVO answer) {
        super();
        this.tenantId = tenantId;
        this.reportId = reportId;
        this.answer = answer;
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
    public AnswerVO getAnswer() {
        return answer;
    }
    public void setAnswer(AnswerVO answer) {
        this.answer = answer;
    }    
}
