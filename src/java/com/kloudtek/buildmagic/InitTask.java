/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Initialize buildmagic
 */
public class InitTask extends Task {
    private String ivy;
    private String ivySettings;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws BuildException {

    }

    public void setIvy(String ivy) {
        this.ivy = ivy;
    }

    public void setIvySettings(String ivySettings) {
        this.ivySettings = ivySettings;
    }
}
