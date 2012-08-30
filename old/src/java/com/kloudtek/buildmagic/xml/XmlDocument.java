/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.xml;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;

public class XmlDocument extends DataType {
    private File file;
    private URL url;
    private Node node;
    private boolean namespaceAware = true;

    public XmlDocument() {
    }

    public XmlDocument(final Node node) {
        this.node = node;
    }

    public void setFile(final File file) {
        this.file = file;
    }

    public void setUrl(final URL url) {
        this.url = url;
    }

    public void setNamespaceAware(final boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    public Node getNode(final Project project) {
        if( isReference() ) {
            return ((XmlDocument) getCheckedRef(project)).getNode(project);
        } else {
            if( node == null ) {
                try {
                    if( file != null && url != null ) {
                        throw new BuildException("Only one of file or url may be set");
                    } else if( file == null && url == null ) {
                        throw new BuildException("One of file or url must be set");
                    }
                    final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    builderFactory.setNamespaceAware(namespaceAware);
                    final DocumentBuilder builder = builderFactory.newDocumentBuilder();
                    if( file != null ) {
                        node = builder.parse(file).getDocumentElement();
                    } else {
                        node = builder.parse(url.toString()).getDocumentElement();
                    }
                } catch (Exception e) {
                    throw new BuildException(e);
                }
            }
        }
        return node;
    }
}
