/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.util.ant;

import org.apache.tools.ant.BuildException;

import java.lang.reflect.Field;

public class AntValidator {
    public static void validate(Object obj) throws BuildException {
        try {
            final Class<?> objClass = obj.getClass();
            final Field[] fields = objClass.getDeclaredFields();
            for (final Field field : fields) {
                final AntAttribute req = field.getAnnotation(AntAttribute.class);
                if (req != null && req.required()) {
                    field.setAccessible(true);
                    if (field.get(obj) == null) {
                        throw new BuildException(objClass.getSimpleName() + "." + field.getName() + " is missing");
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
