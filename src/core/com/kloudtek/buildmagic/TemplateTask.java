/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ImportTask;

import static com.kloudtek.buildmagic.InitTask.BMHOME;
import static java.io.File.separator;

/**
 * Import a buildmagic template.
 */
public class TemplateTask extends Task {
    private String name;

    @Override
    public void execute() throws BuildException {
        String loadedId = "buildmagic.templates.loaded." + name;
        Project p = getProject();
        String loaded = p.getProperty(loadedId);
        String bmhome = p.getProperty(BMHOME);
        String path = bmhome + separator + "templates" + separator + name + separator + name + ".xml";
        ImportTask importTask = new ImportTask();
        importTask.setOwningTarget(getOwningTarget());
        importTask.setLocation(getLocation());
        importTask.setProject(p);
        importTask.setFile(path);
        importTask.execute();
        if (loaded != null && loaded.equals("true")) {
            log("Skipping template, already loaded: " + name, Project.MSG_DEBUG);
        }
        p.setProperty(loadedId, "true");
    }

    public void setName(String name) {
        this.name = name;
    }
}
