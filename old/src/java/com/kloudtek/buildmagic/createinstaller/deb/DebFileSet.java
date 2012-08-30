/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

public class DebFileSet extends ZipFileSet {
    private String user;
    private String group;
    private String filemode;

    public DebFileSet() {
    }

    public DebFileSet(final FileSet fileset) {
        super(fileset);
    }

    public DebFileSet(final ZipFileSet fileset) {
        super(fileset);
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getFilemode() {
        return filemode;
    }

    public void setFilemode(final String filemode) {
        this.filemode = filemode;
    }
}
