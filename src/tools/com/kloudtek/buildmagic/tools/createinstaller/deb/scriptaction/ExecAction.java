/*
 * Copyright (c) 2011. Kloudtek Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.tools.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.tools.createinstaller.deb.ScriptAction;

public class ExecAction extends ScriptAction {
    private String cmd;
    private boolean haltOnError = true;

    public ExecAction(final DebScript debScript) {
        super(debScript);
    }

    public void setCmd(final String cmd) {
        this.cmd = cmd;
    }

    public void setHaltOnError(final boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setValue(final String value) {
        this.cmd = value;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addText(final String txt) {
        if (cmd == null) {
            this.cmd = txt;
        } else {
            this.cmd += txt;
        }
    }

    @Override
    public void process() {
        final String pcmd = debScript.getTask().getProject().replaceProperties(cmd.trim());
        debScript.addStageLine(stage, "debug('" + pcmd + "')");
        debScript.addStageLine(stage, "p = subprocess.Popen('" + pcmd + "',shell=True,stdout=sys.stderr,stderr=sys.stderr)");
        debScript.addStageLine(stage, "p.wait()");
        if (haltOnError) {
            debScript.addStageLine(stage, "if p.returncode != 0:");
            debScript.addStageLine(stage, "\tsys.exit(p.returncode)");
        }
    }
}
