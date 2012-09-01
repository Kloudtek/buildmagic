/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import org.apache.tools.ant.BuildException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a debconf template entry
 */
public class TemplateEntry {
    private String packageName;
    private String id;
    private Type type;
    private String defaultValue;
    private String shortDesc;
    private String longDesc;

    public TemplateEntry() {
    }

    public TemplateEntry(final String id, final Type type) {
        this.id = id;
        this.type = type;
    }

    public TemplateEntry(final String id, final Type type, final String shortDesc, final String longDesc) {
        this.id = id;
        this.type = type;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    public TemplateEntry(final String id, final Type type,
                         final String defaultValue, final String shortDesc, final String longDesc) {
        this.id = id;
        this.type = type;
        this.defaultValue = defaultValue;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    public TemplateEntry(@NotNull final String packageName, final String id, final Type type,
                         final String defaultValue, final String shortDesc, final String longDesc) {
        this.packageName = packageName;
        this.id = id;
        this.type = type;
        this.defaultValue = defaultValue;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    public String getFullId() {
        return packageName + "/" + id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void assignType(final Type type) {
        this.type = type;
    }

    public void setType(final String type) {
        for (Type t : Type.values()) {
            if (t.name().equalsIgnoreCase(type)) {
                this.type = t;
                return;
            }
        }
        throw new BuildException("Invalid type " + type);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(final String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(final String longDesc) {
        this.longDesc = longDesc;
    }

    @Nullable
    public static String getId(final ScriptAction action, String id) {
        if (id == null) {
            if (action.stage != null) {
                id = action.stage;
            } else {
                return null;
            }
        }
        if (!id.contains("/")) {
            id = action.getDebScript().getTask().getName() + "/" + id;
        }
        return id;
    }

    public enum Type {
        SELECT, MULTISELECT, STRING, BOOLEAN, NOTE, TEXT, PASSWORD, ERROR
    }
}
