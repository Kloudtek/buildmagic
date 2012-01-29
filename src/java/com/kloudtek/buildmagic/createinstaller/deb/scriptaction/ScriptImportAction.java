/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.createinstaller.deb.DebScript;
import com.kloudtek.buildmagic.createinstaller.deb.ScriptAction;
import org.apache.tools.ant.BuildException;

/**
* Created by IntelliJ IDEA.
* User: yannick
* Date: 13-Aug-2010
* Time: 09:57:40
* To change this template use File | Settings | File Templates.
*/
public class ScriptImportAction extends ScriptAction {
    private String pkg;

    public ScriptImportAction(final DebScript debScript) {
        super(debScript);
    }

    public void setPkg(final String pkg) {
        this.pkg = pkg;
    }

    @Override
    public void process() {
        if( pkg == null ) {
            throw new BuildException("import pkg not set");
        }
        debScript.addImport(pkg);
    }
}
