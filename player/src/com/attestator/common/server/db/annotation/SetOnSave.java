package com.attestator.common.server.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SetOnSave {
    String     refField();
    Class<?>   targetClass();
    String     targetIdField() default "_id";
    String     targetValueField();
}
