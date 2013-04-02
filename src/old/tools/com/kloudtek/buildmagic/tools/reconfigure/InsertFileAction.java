/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic.tools.reconfigure;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;

/**
 * Action to insert a file into an archive
 */
public class InsertFileAction extends ReconfigureAction {
    private File file;
    private URL url;
    private boolean overwrite = true;

    public void setFile(final File file) {
        this.file = file;
    }

    public void setUrl(final URL url) {
        this.url = url;
    }

    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public void doExecute(@Nullable final InputStream is, @NotNull final OutputStream os) throws IOException {
        if (is != null && !overwrite) {
            throw new BuildException(path + " already exists and overwrite not allowed");
        }
        if (file != null && url != null) {
            throw new BuildException("Only one of file or url may be set");
        } else if (file == null && url == null) {
            throw new BuildException("One of file or url must be set");
        }
        final InputStream src;
        if (url != null) {
            src = url.openStream();
        } else {
            src = new FileInputStream(file);
        }
        IOUtils.copy(src, os);
        src.close();
    }

    @Override
    public boolean isNonExistingSourceAllowed() {
        return true;
    }
}
