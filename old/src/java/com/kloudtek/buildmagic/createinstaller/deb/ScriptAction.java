/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
* Created by IntelliJ IDEA.
* User: yannick
* Date: 13-Aug-2010
* Time: 09:45:25
* To change this template use File | Settings | File Templates.
*/
public abstract class ScriptAction {
    protected final DebScript debScript;
    protected String stage;

    protected ScriptAction(final DebScript debScript) {
        this.debScript = debScript;
    }

    public DebScript getDebScript() {
        return debScript;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setStage(final String stage) {
        this.stage = stage;
    }

    @Nullable
    public Collection<? extends TemplateEntry> generateTemplateEntries() {
        return null;
    }

    protected TemplateEntry createEntry( final String id, final TemplateEntry.Type type, final String defaultValue,
                                         final String shortDesc, final String longDesc ) {
        return new TemplateEntry(debScript.getTask().getName(), id, type, defaultValue, shortDesc, longDesc);
    }

    public void init() {

    }

    public abstract void process();
}
