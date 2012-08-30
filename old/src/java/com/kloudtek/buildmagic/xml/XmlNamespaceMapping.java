/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.xml;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.util.HashMap;
import java.util.Map;

public class XmlNamespaceMapping extends Task {
    public static final String REFNAME = "buildmaster.xml.xmlnamespacemappings";
    private String prefix;
    private String namespace;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    @SuppressWarnings({"unchecked"})
    public static Map<String,String> get( Project project ) {
        Map<String,String> mappings = (Map<String, String>) project.getReference(REFNAME);
        if( mappings == null ) {
            mappings = new HashMap<String, String>();
            project.addReference(REFNAME,mappings);
        }
        return mappings;
    }

    @Override
    public void execute() throws BuildException {
        if( prefix == null || namespace == null ) {
            throw new BuildException("prefix and namespace must be both set");
        }
        get(getProject()).put(prefix,namespace);
    }
}
