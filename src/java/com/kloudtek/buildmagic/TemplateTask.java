/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ImportTask;
import org.apache.tools.ant.types.resources.JavaResource;

/**
 * Import a buildmagic template.
 */
public class TemplateTask extends Task {
    private String name;

    @Override
    public void execute() throws BuildException {
        Project p = getProject();
        String loadedId = "buildmagic.templates.loaded." + name;
        String loaded = p.getProperty(loadedId);
        if (loaded == null) {
            JavaResource classPath = (JavaResource) p.createDataType("javaresource");
            classPath.setName("templates/" + name + "/" + name + ".xml");
            ImportTask importTask = (ImportTask) p.createTask("import");
            importTask.setOwningTarget(getOwningTarget());
            importTask.setLocation(getLocation());
            importTask.setProject(p);
            importTask.add(classPath);
            importTask.execute();
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}
