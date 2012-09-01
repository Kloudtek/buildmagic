/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.tools.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.tools.createinstaller.deb.ScriptAction;
import com.kloudtek.buildmagic.tools.createinstaller.deb.TemplateEntry;
import org.apache.tools.ant.BuildException;

import java.util.Arrays;
import java.util.Collection;

import static com.kloudtek.buildmagic.tools.createinstaller.deb.TemplateEntry.Type.*;

public class ConfirmLicenseAction extends ScriptAction {
    private static final String STGASK = "confirmlicence";
    private static final String STGWARN = "licenserefusedwarn";
    private static final String STGFAIL = "licenserefused";
    private static final String STGACCEPT = "licenseaccepted";
    private String name;
    private String id;
    private String licenseText;
    private TemplateEntry licenseTxtEntry;
    private TemplateEntry licenseConfirmEntry;
    private TemplateEntry licenseErrorEntry;

    public ConfirmLicenseAction(final DebScript debScript) {
        super(debScript);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLicenseText() {
        return licenseText;
    }

    public void setLicenseText(final String licenseText) {
        this.licenseText = licenseText;
    }

    public void addText(final String licenseText) {
        if (this.licenseText == null) {
            this.licenseText = licenseText;
        } else {
            this.licenseText = this.licenseText + licenseText;
        }
    }

    @Override
    public void init() {
        if (id == null) {
            id = stage;
        }
        if (licenseText == null || licenseText.trim().length() == 0) {
            throw new BuildException("confirmlicense is missing license text");
        }
        licenseTxtEntry = createEntry("licensetext-" + id, NOTE, null, name, licenseText);
        licenseConfirmEntry = createEntry("confirmlicense-" + id, BOOLEAN, null,
                "Do you agree with the " + name + " license terms?",
                "In order to install this package, you must agree NOTE its license terms, the \"" + name + "\". Not accepting will cancel the installation.");
        licenseErrorEntry = createEntry("licenserefused-" + id, ERROR, null,
                "Declined " + name,
                "In order to install this package, you must agree to its license terms, the \"" + name + "\". Not accepting will cancel the installation.");
    }

    @Override
    public void process() {
        if (licenseText == null || licenseText.trim().length() == 0) {
            throw new BuildException("confirmlicense is missing license text");
        }
        ScriptInputAction.writeDebConfInput(debScript, STGASK, licenseTxtEntry.getFullId(), "CRITICAL");
        ScriptInputAction.writeDebConfInput(debScript, STGASK, licenseConfirmEntry.getFullId(), "CRITICAL");
        ScriptInputAction.writeDebConfInput(debScript, STGASK, licenseConfirmEntry.getFullId(), "CRITICAL");
        debScript.addNavStageLine(STGASK, "if db.getBoolean(\"" + licenseConfirmEntry.getFullId() + "\"):");
        debScript.addNavStageLine(STGASK, "\treturn '" + STGACCEPT + "'");
        debScript.addNavStageLine(STGASK, "else:");
        debScript.addNavStageLine(STGASK, "\treturn '" + STGWARN + "'");
        ScriptInputAction.writeDebConfInput(debScript, STGWARN, licenseErrorEntry.getFullId(), "CRITICAL");
        debScript.addFailLine(STGFAIL, "License not accepted", -1);
        debScript.addStageLine(STGACCEPT, "pass");
    }

    @Override
    public Collection<? extends TemplateEntry> generateTemplateEntries() {
        return Arrays.asList(licenseTxtEntry, licenseConfirmEntry, licenseErrorEntry);
    }
}