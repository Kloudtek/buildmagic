/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic.tools.reconfigure;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public abstract class ReconfigureAction extends ReconfigureActionComponent {
    protected String path;
    protected ReconfigureTask task;

    public void setTask(final ReconfigureTask task) {
        this.task = task;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        if (path != null && path.startsWith("/")) {
            this.path = path.substring(1);
        } else {
            this.path = path;
        }
    }

    public void execute(final InputStream is, final OutputStream os) throws IOException {
        if (isEnabled()) {
            doExecute(is, os);
        } else if (is != null) {
            IOUtils.copy(is, os);
        }
    }

    public static boolean evalConditions(Enumeration conditionsEnumeration) {
        while (conditionsEnumeration.hasMoreElements()) {
            Condition c = (Condition) conditionsEnumeration.nextElement();
            if (!c.eval()) {
                return false;
            }
        }
        return true;
    }

    public abstract void doExecute(@Nullable final InputStream is, @NotNull final OutputStream os) throws IOException;

    public abstract boolean isNonExistingSourceAllowed();
}
