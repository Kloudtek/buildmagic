/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.tools.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.tools.createinstaller.deb.ScriptAction;

/**
 * Created by IntelliJ IDEA.
 * User: yannick
 * Date: 13-Aug-2010
 * Time: 09:46:16
 * To change this template use File | Settings | File Templates.
 */
public class RawScriptLineAction extends ScriptAction {
    private String line;
    private String type;

    public RawScriptLineAction(final DebScript debScript) {
        super(debScript);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setValue(final String value) {
        this.line = value;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addText(final String txt) {
        if (line == null) {
            this.line = txt;
        } else {
            this.line += txt;
        }
    }

    @Override
    public void process() {
        if (type != null && type.equalsIgnoreCase("nav")) {
            debScript.addNavStageLine(stage, debScript.getTask().getProject().replaceProperties(line));
        } else {
            debScript.addStageLine(stage, debScript.getTask().getProject().replaceProperties(line));
        }
    }
}
