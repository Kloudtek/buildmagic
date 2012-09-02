/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.reconfigure;


import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplaceAction extends ReconfigureAction {
    private ArrayList<Replace> replaces = new ArrayList<Replace>();
    private String encoding = "UTF-8";

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public Replace createReplace() {
        return addComponent(new Replace());
    }

    @Override
    public void doExecute(@Nullable final InputStream is, @NotNull final OutputStream os) throws IOException {
        String txt = IOUtils.toString(is);
        for (Replace rep : getEnabledComponents(Replace.class)) {
            final Pattern pattern = Pattern.compile(rep.regex);
            final Matcher matcher = pattern.matcher(txt);
            if (rep.all) {
                txt = matcher.replaceAll(rep.value);
            } else {
                txt = matcher.replaceFirst(rep.value);
            }
        }
        OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName(encoding));
        writer.write(txt);
        writer.flush();
    }

    @Override
    public boolean isNonExistingSourceAllowed() {
        return false;
    }

    public class Replace extends ReconfigureActionComponent {
        private boolean all = true;
        private String regex;
        private String value;

        public void setAll(final boolean all) {
            this.all = all;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public void setRegex(final String regex) {
            this.regex = regex;
        }
    }
}
