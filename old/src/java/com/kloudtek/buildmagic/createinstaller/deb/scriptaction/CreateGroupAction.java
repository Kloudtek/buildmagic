/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.createinstaller.deb.*;
import com.kloudtek.buildmagic.util.ant.AntAttribute;
import com.kloudtek.buildmagic.util.ant.AntValidator;

public class CreateGroupAction extends ScriptAction {
    @AntAttribute(required = true)
    private String name;
    private boolean system = true;

    public CreateGroupAction(final DebScript debScript) {
        super(debScript);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setName(final String name) {
        this.name = name;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setSystem(final boolean system) {
        this.system = system;
    }

    @Override
    public void process() {
        AntValidator.validate(this);
        final StringBuilder line = new StringBuilder("os.system('getent group " + name + " >/dev/null 2>&1 || addgroup");
        if (system) {
            line.append(" --system");
        }
        line.append(" ").append(name).append("')");
        debScript.addStageLine(DebScript.MAIN,line.toString());
    }
}
