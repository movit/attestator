package com.attestator.common.shared.vo;

import java.util.Date;

public abstract class ModificationDateAwareVO extends TenantableVO {
    private static final long serialVersionUID = 9053483188451134959L;
    private Date created = new Date();
    private Date modified = new Date();
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    public Date getModified() {
        return modified;
    }
    public void setModified(Date modified) {
        this.modified = modified;
    }
    @Override
    public void resetIdentity() {        
        super.resetIdentity();
        created = new Date();
        modified = new Date();
    }
}
