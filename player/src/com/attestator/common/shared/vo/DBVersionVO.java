package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Entity;

import com.attestator.common.shared.helper.NullHelper;

@Entity("dbversion")
public class DBVersionVO extends BaseVO {
    private static final long serialVersionUID = -8369396316739486833L;
    private Integer version;

    public Integer getVersion() {
        return version;
    }
    public int getVersionOrZero() {
        return NullHelper.nullSafeIntegerOrZerro(version);
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

    public DBVersionVO() {        
    }
    
    public DBVersionVO(Integer version) {
        super();
        this.version = version;
    }    
}
