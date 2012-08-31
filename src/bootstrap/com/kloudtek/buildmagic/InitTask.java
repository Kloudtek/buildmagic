package com.kloudtek.buildmagic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Taskdef;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static java.io.File.separator;

/**
 * Initialize buildmagic
 */
public class InitTask extends Task {
    public static final String BMHOME = "buildmagic.home";
    public static final String BMRDY = "buildmagic.init.done";

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
            Taskdef loadCoreTaskDef = new Taskdef();
            loadCoreTaskDef.setResource("com/kloudtek/buildmagic/antlib-core.xml");
            loadCoreTaskDef.setURI("antlib:com.kloudtek.buildmagic");
            loadCoreTaskDef.setClasspath(getBMCoreCLPath(home));
            loadCoreTaskDef.setProject(p);
            loadCoreTaskDef.execute();
            p.setProperty(BMRDY, "true");
        }
    }

    private Path getBMCoreCLPath(String home) {
        return new Path(getProject(), home + separator + "lib" + separator + "buildmagic-core.jar");
    }

    private String locateHome() {
        Project p = getProject();
        String override = getProject().getProperty(BMHOME);
        if (override != null && !override.isEmpty()) {
            if (!new File(override).exists()) {
                throw new BuildException("buildmagic home does not exist: " + override);
            }
            return override;
        }
        String uhome = p.getProperty("user.home");
        List<String> paths = Arrays.asList(p.getProperty("buildmagic.home"), uhome + "/buildmagic",
                uhome + "/apps/buildmagic", p.getProperty("ant.home") + "/buildmagic", "/usr/share/buildmagica",
                "/opt/buildmagic", "/Library/buildmagic", "C:\\Program File\\buildmagic");
        for (String path : paths) {
            if (path != null && !path.isEmpty() && new File(path).exists()) {
                getProject().setProperty(BMHOME, path);
                return path;
            }
        }
        return null;
    }
}
