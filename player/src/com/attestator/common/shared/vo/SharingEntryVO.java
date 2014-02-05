package com.attestator.common.shared.vo;

import java.util.Date;

public class SharingEntryVO extends BaseVO {
    private static final long serialVersionUID = -9125821775945058776L;
    private String tenantId;
    private String username;
    private Date start;
    private Date end;
    
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public Date getStart() {
        return start;
    }
    public void setStart(Date start) {
        this.start = start;
    }
    public Date getEnd() {
        return end;
    }
    public void setEnd(Date end) {
        this.end = end;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @Override
    public String toString() {
        return "SharingEntryVO [tenantId=" + tenantId + ", username="
                + username + ", start=" + start + ", end=" + end + ", getId()="
                + getId() + "]";
    }
}
