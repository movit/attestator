package com.attestator.common.shared.vo;

import java.util.Date;

import com.attestator.common.shared.SharedConstants;

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
    /**
     * Value which shows in UI
     */
    public Date getStart() {
        return start;
    }
    /**
     * Effective point in time when sharing event should happen
     */
    public Date getStartTime() {
        return start;
    }
    public void setStartTime(Date start) {
        this.start = start;
    }
    public void setStart(Date start) {
        this.start = start;
    }
    /**
     * Value which shows in UI
     */
    public Date getEnd() {
        return end;
    }
    /**
     * Effective point in time when sharing event should happen
     */
    public Date getEndTime() {
        if (end == null) {
            return null;
        }
        return new Date(end.getTime() + SharedConstants.MILLISECONDS_IN_DAY);
    }    
    public void setEnd(Date end) {
        this.end = end;
    }
    public void setEndTime(Date end) {
        if (end != null) {
            end = new Date(end.getTime() - SharedConstants.MILLISECONDS_IN_DAY);
        }
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
