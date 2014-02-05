package com.attestator.common.shared.vo;

public class CronTaskLockVO extends LockVO {
    private static final long serialVersionUID = 1599632401016840618L;
    private String cronTaskId;

    public String getCronTaskId() {
        return cronTaskId;
    }

    public void setCronTaskId(String cronTaskId) {
        this.cronTaskId = cronTaskId;
    }

    public CronTaskLockVO(String cronTaskId) {
        super();
        this.cronTaskId = cronTaskId;
    }

    public CronTaskLockVO() {
        super();        
    }    
}
