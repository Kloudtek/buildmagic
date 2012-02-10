package com.kloudtek.buildmagic.changelog;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;

/**
 * Task used to create a debian changelog
 */
public class DebianChangelogTask extends Task {
    private File dest;
    private String version;
    private File jenkinsChangelog;

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setJenkinsChangelog(File jenkinsChangelog) {
        this.jenkinsChangelog = jenkinsChangelog;
    }

    @Override
    public void execute() throws BuildException {

    }
}
