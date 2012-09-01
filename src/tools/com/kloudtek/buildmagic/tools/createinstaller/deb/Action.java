/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import java.util.Collection;

public interface Action {
    void init(final CreateDebTask createDebTask);

    public abstract Collection<? extends TemplateEntry> generateTemplateEntries();

    public abstract void generateScript(DebScript script);
}
