/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.createinstaller.deb.ScriptAction;
import com.kloudtek.buildmagic.createinstaller.deb.TemplateEntry;
import org.apache.tools.ant.BuildException;

import java.util.Arrays;
import java.util.Collection;

public class ValidateAction extends ScriptAction {
    private String test;
    private String failStage;
    private String errorId;
    private String title;
    private String description;
    private TemplateEntry validateQuestion;

    public ValidateAction(final DebScript debScript) {
        super(debScript);
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setErrorId(final String errorId) {
        this.errorId = errorId;
    }

    public void setFailStage(final String failStage) {
        this.failStage = failStage;
    }

    public void setTest(final String test) {
        this.test = test;
    }

    @Override
    public void init() {
        if( errorId == null ) {
            errorId = stage;
        }
        validateQuestion = createEntry(errorId, TemplateEntry.Type.ERROR, null, title, description);
    }

    @Override
    public Collection<? extends TemplateEntry> generateTemplateEntries() {
        return Arrays.asList(validateQuestion);
    }

    @Override
    public void process() {
        debScript.addStageLine(stage, "global "+errorId+"val");
        debScript.addStageLine(stage, errorId+"val = "+test);
        debScript.addStageLine(stage, "if not " + errorId + "val:");
        debScript.addStageLine(stage, "\tdb.input(debconf.CRITICAL, '"+validateQuestion.getFullId()+"')");
        debScript.addNavStageLine(stage, "if not " + errorId + "val:");
        if( failStage != null ) {
            debScript.addNavStageLine(stage, "\tdebug('validation failed, going to stage: "+failStage+"')");
            debScript.addNavStageLine(stage, "\treturn '"+failStage+"'");
        } else {
            debScript.addNavStageLine(stage, "\tdebug('validation failed, exiting')");
            debScript.addNavStageLine(stage, "\tsys.exit(-1)");
        }
        debScript.addNavStageLine(stage, "else:");
        debScript.addNavStageLine(stage, "\treturn None");
    }
}
