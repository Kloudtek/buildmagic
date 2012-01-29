package com.kloudtek.buildmagic.bootstrap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Taskdef;

import java.lang.reflect.Method;

public class BootstrapInitTask extends Task {
    private String minIvy = "2.1";
    private String minBuildMagic = "0.8";

    public void setMinIvy(String minIvy) {
        this.minIvy = minIvy;
    }

    public void setMinBuildMagic(String minBuildMagic) {
        this.minBuildMagic = minBuildMagic;
    }

    @Override
    public void execute() throws BuildException {
        if( ! loadInMemoryIvy() ) {
            if( ! loadFromFile("/usr/share/java/ivy.jar") ) {
                throw new BuildException("Couldn't load ivy");
            }
        }
    }

    private boolean loadInMemoryIvy() {
        try {
            final Class<?> cl = Class.forName("org.apache.ivy.Ivy");
            if( checkVersion(cl,"in-memory") ) {
                loadIvy(cl);
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException e) {
            log("No in-memory found", Project.MSG_DEBUG);
            return false;
        }
    }

    private boolean loadFromFile(String path) {
        return false;
    }

    private void loadIvy(Class<?> cl) {
        final Taskdef taskDef = new Taskdef();
        taskDef.setProject(getProject());
        taskDef.setAntlib("antlib:org.apache.ivy.ant");
    }

    private boolean checkVersion(Class<?> cl, String type) {
        try {
            final Method method = cl.getMethod("getIvyVersion");
            final String version = (String) method.invoke(null);
            final String msg = "Found " + type + " ivy version " + version;
            if( version.startsWith("2") ) {
                log(msg+", loading it",Project.MSG_DEBUG);
                return true;
            } else {
                log(msg+" which is too old",Project.MSG_DEBUG);
                return false;
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public static void main(String[] args) {
        new BootstrapInitTask().execute();
    }
}
