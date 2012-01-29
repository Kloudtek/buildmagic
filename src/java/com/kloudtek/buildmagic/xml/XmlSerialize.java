/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.xml;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class XmlSerialize extends Task {
    private XmlDocument xmlDoc;
    private File dest;
    private boolean prettyPrint = true;
    private String encoding;
    private String systemId;
    private ArrayList<DomConfig> domConfigs = new ArrayList<DomConfig>();

    public void setDest(final File dest) {
        this.dest = dest;
    }

    public void setPrettyPrint(final boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public XmlDocument createXmlDoc() {
        if (xmlDoc == null) {
            xmlDoc = new XmlDocument();
        }
        return xmlDoc;
    }

    public DomConfig createDomConfig() {
        final DomConfig op = new DomConfig();
        domConfigs.add(op);
        return op;
    }

    @Override
    public void execute() throws BuildException {
        if (xmlDoc == null) {
            throw new BuildException("No xml document set");
        }
        try {
            final Node document = xmlDoc.getNode(getProject());
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();
            final DOMConfiguration cfg = writer.getDomConfig();
            if( prettyPrint ) {
                cfg.setParameter("format-pretty-print",true);
            }
            final LSOutput output = impl.createLSOutput();
            if( encoding != null ) {
                output.setEncoding(encoding);
            }
            if( systemId != null ) {
                output.setEncoding(systemId);
            }
            final FileOutputStream os = new FileOutputStream(dest);
            output.setByteStream(os);
            writer.write(document, output);
            os.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    class DomConfig {
        private String key;
        private String value;

        public void setKey(final String key) {
            this.key = key;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
