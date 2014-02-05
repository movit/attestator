package com.attestator.common.shared.vo;

import org.mongodb.morphia.annotations.Entity;

@Entity("lock")
public abstract class LockVO extends ModificationDateAwareVO {
    private static final long serialVersionUID = -988266862725448969L;
}
