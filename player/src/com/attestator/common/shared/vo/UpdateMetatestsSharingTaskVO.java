package com.attestator.common.shared.vo;

import java.util.Date;

import org.mongodb.morphia.annotations.Entity;

@Entity("cronTask")
public class UpdateMetatestsSharingTaskVO extends TenantableCronTaskVO {
    private static final long serialVersionUID = -6545124320524000982L;

    public UpdateMetatestsSharingTaskVO() {
    }

    public UpdateMetatestsSharingTaskVO(Date time) {
        super(time);
    }
}
