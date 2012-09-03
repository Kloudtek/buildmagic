/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Taskdef;
import org.apache.tools.ant.types.Path;

import java.io.File;

import static java.io.File.separator;

/**
 * Initialize buildmagic
 */
public class InitTask extends Task {
    public static final String BMHOME = "buildmagic.home";
    public static final String BMRDY = "buildmagic.init.done";
    private String ivy = "${basedir}/ivy.xml";

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws BuildException {
        Project p = getProject();
        if (p.getProperty(BMRDY) == null) {
            String home = locateHome();
            if (home == null) {
                throw new BuildException("Unable to find buildmagic home");
            }
            p.setUserProperty(BMHOME, new File(home).getAbsolutePath());
            log("buildmagic home: " + home);
            // Load core module
            Path coreClasspath = getBMCoreCLPath(home);
            Taskdef loadCoreTaskDef = (Taskdef) getProject().createTask("taskdef");
            loadCoreTaskDef.setResource("com/kloudtek/buildmagic/antlib-core.xml");
            loadCoreTaskDef.setURI("antlib:com.kloudtek.buildmagic");
            loadCoreTaskDef.setClasspath(coreClasspath);
            loadCoreTaskDef.setProject(p);
            loadCoreTaskDef.execute();
            p.setProperty(BMRDY, "true");
        }
    }

    private Path getBMCoreCLPath(String home) {
        return new Path(getProject(), home + separator + "lib" + separator + "buildmagic-core.jar");
    }

    private String locateHome() {
        return findFile(BMHOME, "${user.home}/buildmagic", "${user.home}/apps/buildmagic", "${ant.home}/buildmagic",
                "/usr/share/buildmagic", "/opt/buildmagic", "/Library/buildmagic", "C:\\Program File\\buildmagic");
    }

    private String findFile(String propertyName, String... directories) {
        Project p = getProject();
        String value = getProject().getProperty(propertyName);
        if (value != null && !value.isEmpty()) {
            if (!new File(value).exists()) {
                throw new BuildException(propertyName + " directory " + value + " does not exist");
            }
            return value;
        }
        if (directories != null) {
            for (String dir : directories) {
                dir = getProject().replaceProperties(dir);
                if (dir != null && !dir.isEmpty() && new File(dir).exists()) {
                    getProject().setProperty(propertyName, dir);
                    return dir;
                }
            }
        }
        return null;
    }
}
