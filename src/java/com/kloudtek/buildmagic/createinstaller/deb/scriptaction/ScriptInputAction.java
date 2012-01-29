/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.createinstaller.deb.ScriptAction;
import com.kloudtek.buildmagic.createinstaller.deb.TemplateEntry;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptInputAction extends ScriptAction {
    private String id;
    private String priority;
    private String var;
    private final List<GotoStageAction> gotoStageList = new ArrayList<GotoStageAction>();

    public ScriptInputAction(final DebScript debScript) {
        super(debScript);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setId(final String id) {
        this.id = id;
    }

    public void setVar(final String var) {
        this.var = var;
    }

    public String getId() {
        return id;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setPriority(final String priority) {
        if (!DebScript.PRIORITIES.contains(priority.toUpperCase())) {
            throw new BuildException("Invalid priority: " + priority);
        }
        this.priority = priority.toUpperCase();
    }

    @Override
    public void init() {
        if( id == null ) {
            id = stage;
        }
    }

    @Override
    public void process() {
        final TemplateEntry templateEntry = debScript.getTask().getTemplateEntry(id);
        if (id == null) {
            throw new BuildException("Either id or stage must be set");
        }
        writeDebConfInput(debScript, stage, templateEntry.getFullId(), priority);
        for (final GotoStageAction gotoStage : gotoStageList) {
            gotoStage.setQuestion(templateEntry.getFullId());
            gotoStage.setStage(stage);
            gotoStage.process();
        }
        if (var != null) {
            debScript.addStageLine(stage+"2", "global "+var);
            debScript.addStageLine(stage+"2", "vars."+var+" = db.get('" + templateEntry.getFullId() + "')");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public GotoStageAction createGotostage() {
        final GotoStageAction gotoStage = new GotoStageAction(debScript);
        gotoStageList.add(gotoStage);
        return gotoStage;
    }

    public static void writeDebConfInput(final DebScript debScript, final String stage, final String id, final String priority) {
        debScript.addStageLine(stage, "if returned:");
        debScript.addStageLine(stage, "    db.fset('" + id + "','seen','false')");
        debScript.addStageLine(stage, "try:");
        debScript.addStageLine(stage, "    db.input(debconf." + priority.toUpperCase() + ", '" + id + "')");
        debScript.addStageLine(stage, "except debconf.DebconfError as e:");
        debScript.addStageLine(stage, "    if e.args[0] != 30:");
        debScript.addStageLine(stage, "        raise");
    }
}
