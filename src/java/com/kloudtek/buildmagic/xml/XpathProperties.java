/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.xml;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Task used to extract data from an xml file and assign them to ant properties
 */
public class XpathProperties extends Task {
    private static final String TYPE_NODE = "node";
    private static final String TYPE_STRING = "string";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_NUMBER = "number";
    private File file;
    private URL url;
    private XmlDocument xmlDoc;
    private String prefix;
    private ArrayList<Entry> entries = new ArrayList<Entry>();
    private Node doc;
    private static final XPathFactory xpFac = XPathFactory.newInstance();
    private Map<String, String> mappings;

    public void setXmlDocRef(final Reference r) {
        createXmlDoc().setRefid(r);
    }

    public XmlDocument createXmlDoc() {
        if( xmlDoc == null ) {
            xmlDoc = new XmlDocument();
        }
        return xmlDoc;
    }

    /**
     * Execute task
     *
     * @throws BuildException
     */
    @Override
    public void execute() throws BuildException {
        try {
            mappings = XmlNamespaceMapping.get(getProject());
            loadDocument();
            final XPath xpath = xpFac.newXPath();
            if( mappings != null && ! mappings.isEmpty() ) {
                xpath.setNamespaceContext(new NamespaceMapper(mappings));
            }
            for (final Entry entry : entries) {
                if (entry.xpath == null) {
                    throw new BuildException("Entry is missing xpath");
                }
                if (entry.key == null) {
                    throw new BuildException("Entry is missing key");
                }
                final String value = getXpath(doc, xpath, entry);
                if (value != null) {
                    final String key;
                    if (prefix != null) {
                        key = prefix + "." + entry.key;
                    } else {
                        key = entry.key;
                    }
                    getProject().setProperty(key, value);
                }
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public void setFile(final File file) {
        this.file = file;
    }

    public void setUrl(final URL url) {
        this.url = url;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public Entry createEntry() {
        final Entry entry = new Entry();
        entries.add(entry);
        return entry;
    }

    private String getXpath(final Node doc, final XPath xpath, final Entry entry) throws XPathExpressionException {
        final XPathExpression expression = xpath.compile(entry.xpath);
        String value;
        if (entry.type == null) {
            throw new BuildException("entry is missing a type");
        } else if (entry.type.equalsIgnoreCase(TYPE_NODE)) {
            final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
            if (node == null) {
                value = null;
            } else {
                value = node.getNodeValue();
            }
        } else if (entry.type.equalsIgnoreCase(TYPE_STRING)) {
            value = (String) expression.evaluate(doc, XPathConstants.STRING);
        } else if (entry.type.equalsIgnoreCase(TYPE_NUMBER)) {
            final Double val = (Double) expression.evaluate(doc, XPathConstants.NUMBER);
            if (val != null) {
                value = val.toString();
            } else {
                value = null;
            }
        } else if (entry.type.equalsIgnoreCase(TYPE_BOOLEAN)) {
            final Boolean val = (Boolean) expression.evaluate(doc, XPathConstants.BOOLEAN);
            if (val != null) {
                value = val.toString().toLowerCase();
            } else {
                value = null;
            }
        } else {
            throw new BuildException("Invalid entry type: " + entry.type);
        }
        if (value == null) {
            if (entry.failOnNoMatch) {
                throw new BuildException("Unable to find node at xpath " + entry.xpath);
            } else {
                value = null;
            }
        }
        return value;
    }

    private void loadDocument() throws IOException, SAXException, ParserConfigurationException {
        if( xmlDoc != null) {
            doc = xmlDoc.getNode(getProject());
        } else {
            final InputStream is;
            if (file != null) {
                is = new FileInputStream(file);
            } else if(url != null) {
                is = url.openStream();
            } else {
                throw new BuildException("No xml source specified");
            }
            doc = getDocumentBuilder().parse(is);
            is.close();
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(mappings != null && !mappings.isEmpty());
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new BuildException(e);
        }
    }

    public class NamespaceMapper implements NamespaceContext {
        private Map<String,String> mapper;

        public NamespaceMapper(final Map<String, String> mapper) {
            this.mapper = mapper;
        }

        @Override
        public String getNamespaceURI(final String prefix) {
            return mapper.get(prefix);
        }

        @Override
        public String getPrefix(final String namespaceURI) {
            final Iterator list = getPrefixes(namespaceURI);
            if( list.hasNext() ) {
                return (String) list.next();
            } else {
                return null;
            }
        }

        @Override
        public Iterator getPrefixes(final String namespaceURI) {
            ArrayList<String> list = new ArrayList<String>();
            for (Map.Entry<String, String> entry : mapper.entrySet()) {
                if( entry.getValue().equals(namespaceURI)) {
                    list.add(entry.getKey());
                }
            }
            return list.iterator();
        }
    }

    public class Entry {
        private String key;
        private String xpath;
        private String type = TYPE_NODE;
        private boolean failOnNoMatch = true;

        public void setKey(final String key) {
            this.key = key;
        }

        public void setXpath(final String xpath) {
            this.xpath = xpath;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public void setFailOnNoMatch(final boolean failOnNoMatch) {
            this.failOnNoMatch = failOnNoMatch;
        }
    }
}
