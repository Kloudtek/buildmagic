/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.util.ant;

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})
public @interface AntAttribute {
    boolean required() default false;
}
