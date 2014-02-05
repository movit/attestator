package com.attestator.common.shared.vo;

import java.util.Date;

import org.mongodb.morphia.annotations.Entity;
@Entity("cronTask")
public abstract class CronTaskVO extends ModificationDateAwareVO {
    private static final long serialVersionUID = -7124444526086309446L;
    private Date time;
    
    public CronTaskVO() {
    }

    public CronTaskVO(Date time) {
        super();
        this.time = time;
    }

    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    @Override
    public String toString() {
        return "CronTaskVO [time=" + time + ", getCreated()=" + getCreated()
                + ", getModified()=" + getModified() + ", getId()=" + getId()
                + ", getClass()=" + getClass() + "]";
    }
}
