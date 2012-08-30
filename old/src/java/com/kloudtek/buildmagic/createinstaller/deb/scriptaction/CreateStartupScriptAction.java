/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.createinstaller.deb.Action;
import com.kloudtek.buildmagic.createinstaller.deb.CreateDebTask;
import com.kloudtek.buildmagic.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.createinstaller.deb.TemplateEntry;

import java.util.Collection;

public class CreateStartupScriptAction implements Action {
    private String executable;
    private boolean spawn;
    private String authbind;
    private String consoleLogs;

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(final String executable) {
        this.executable = executable;
    }

    public boolean isSpawn() {
        return spawn;
    }

    public void setSpawn(final boolean spawn) {
        this.spawn = spawn;
    }

    public String getAuthbind() {
        return authbind;
    }

    public void setAuthbind(final String authbind) {
        this.authbind = authbind;
    }

    public String getConsoleLogs() {
        return consoleLogs;
    }

    public void setConsoleLogs(final String consoleLogs) {
        this.consoleLogs = consoleLogs;
    }

    @Override
    public void init(final CreateDebTask createDebTask) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<? extends TemplateEntry> generateTemplateEntries() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void generateScript(final DebScript script) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
