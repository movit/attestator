package com.attestator.common.server.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Reference {
    String      fromField() default "_id";
    String      toField() default "_id";
    String[]    excludeFields() default {};
    String[]    includeFields() default {};
}
