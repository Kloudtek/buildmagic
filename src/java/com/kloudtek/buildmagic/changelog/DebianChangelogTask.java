package com.kloudtek.buildmagic.changelog;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import javax.swing.text.PlainDocument;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Task used to create a debian changelog.
 */
public class DebianChangelogTask extends Task {
    private File dest;
    private String version;
    private Type type;
    private File src;
    private String projectName;
    private String maintainerName;
    private String maintainerEmail;
    private String distributions;
    private String urgency = "low";

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setType(String type) {
        try {
            this.type = Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BuildException("Invalid type: "+type);
        }
    }

    public void setSrc(File src) {
        this.src = src;
    }

    public void setMaintainerName(String maintainerName) {
        this.maintainerName = maintainerName;
    }

    public void setMaintainerEmail(String maintainerEmail) {
        this.maintainerEmail = maintainerEmail;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setDistributions(String distributions) {
        this.distributions = distributions;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    @Override
    public void execute() throws BuildException {
        if( projectName == null ) {
            throw new BuildException("Type is not defined");
        }
        if( distributions == null ) {
            throw new BuildException("Type is not defined");
        }
        if( type == null ) {
            throw new BuildException("Type is not defined");
        }
        if( src == null ) {
            throw new BuildException("Src is not defined");
        }
        if( version == null ) {
            throw new BuildException("Version");
        }
        if( maintainerName == null ) {
            throw new BuildException("maintainerName is not defined");
        }
        if( maintainerEmail == null ) {
            throw new BuildException("maintainerEmail is not defined");
        }
        if( dest == null ) {
            dest = new File("debian/changelog.txt");
        }
        StringBuilder changelog = new StringBuilder();
        try {
            List<String> changes = getChanges();
            if( changes.isEmpty() ) {
                changes.add("Release "+version);
            }
            changelog.append(projectName+" ("+version+") "+ distributions +"; urgency="+urgency+"\n\n");
            for (String change : changes) {
                changelog.append("  * ").append(change).append("\n\n");
            }
            final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ"); // day-of-week, dd month yyyy hh:mm:ss +zzzz
            changelog.append(" -- ").append(maintainerName).append(" <").append(maintainerEmail).append(">  ")
                    .append(format.format(new Date())).append("\n");
            FileUtils.writeStringToFile(dest,changelog.toString());
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getChanges() throws IOException {
        HashSet<String> changes = new HashSet<String>();
        final List<String> lines = FileUtils.readLines(src);
        switch (type) {
            case JENKINSGIT:
                for (String line : lines) {
                    if( ! line.trim().isEmpty() && line.startsWith(" ") ) {
                        changes.add(line.trim());
                    }
                }
                break;
            case PLAIN:
                for (String line : lines) {
                    if( ! line.trim().isEmpty()  ) {
                        changes.add(line.trim());
                    }
                }
                break;
        }
        return new ArrayList<String>(changes);
    }
    
    public enum Type {
        PLAIN,JENKINSGIT
    }
}
