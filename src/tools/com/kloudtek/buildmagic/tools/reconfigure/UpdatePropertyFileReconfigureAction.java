/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.reconfigure;

import org.apache.tools.ant.BuildException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class UpdatePropertyFileReconfigureAction extends ReconfigureAction {
    public Entry createEntry() {
        return addComponent(new Entry());
    }

    @Override
    public void doExecute(@Nullable final InputStream is, @NotNull final OutputStream os) throws IOException {
        Properties props = new Properties();
        if (is != null) {
            props.load(is);
        }
        for (Entry entry : getEnabledComponents(Entry.class)) {
            final String key = entry.getKey();
            if (key == null || key.trim().length() == 0) {
                throw new BuildException("Invalid property file entry, key missing");
            }
            final String op = entry.getOperation();
            if (op == null || op.trim().length() == 0) {
                throw new BuildException("Invalid property file entry, operation missing");
            }
            if (op.equals("set") || op.equals("=")) {
                props.setProperty(key, entry.getValue(task));
            } else if (op.equals("del") || op.equals("-")) {
                props.remove(key);
            } else {
                throw new BuildException("Invalid entry operation type: " + op);
            }
        }
        props.store(os, "Updated by reconfigure task");
    }

    @Override
    public boolean isNonExistingSourceAllowed() {
        return true;
    }

    /**
     * Property file entry
     */
    public static class Entry extends ReconfigureActionComponent {
        private String key;
        private String value;
        private String xmlValue;
        private String operation = "set";

        public Entry() {
        }

        public Entry(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(final String operation) {
            this.operation = operation;
        }

        public void setXmlValue(final String xmlValue) {
            this.xmlValue = xmlValue;
        }

        public String getValue(final ReconfigureTask task) {
            ReconfigureTask.validateXmlValueArgs(value, xmlValue);
            if (xmlValue != null) {
                return task.getXmlValue(xmlValue);
            } else {
                return value;
            }
        }
    }
}
