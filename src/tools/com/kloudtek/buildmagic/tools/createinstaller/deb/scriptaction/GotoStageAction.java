/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.tools.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.tools.createinstaller.deb.ScriptAction;
import com.kloudtek.buildmagic.tools.util.ant.AntAttribute;
import com.kloudtek.buildmagic.tools.util.ant.AntValidator;

/**
 * Created by IntelliJ IDEA.
 * User: yannick
 * Date: 13-Aug-2010
 * Time: 09:45:50
 * To change this template use File | Settings | File Templates.
 */
public class GotoStageAction extends ScriptAction {
    @AntAttribute(required = true)
    private String dest;
    @AntAttribute(required = true)
    private String eq;
    @AntAttribute(required = true)
    private String question;

    public GotoStageAction(final DebScript debScript) {
        super(debScript);
    }

    public void setDest(final String dest) {
        this.dest = dest;
    }

    public void setEq(final String eq) {
        this.eq = eq;
    }

    public void setQuestion(final String question) {
        this.question = question;
    }

    @Override
    public void process() {
        AntValidator.validate(this);
        debScript.addNavStageLine(stage, "if db.getString('" + question + "') == '" + eq + "':");
        debScript.addNavStageLine(stage, "\treturn '" + dest + "'");
        debScript.addNavStageLine(stage, "else:");
        debScript.addNavStageLine(stage, "\treturn None");
    }
}
