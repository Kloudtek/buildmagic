/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic.tools.reconfigure;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

@SuppressWarnings({"UnusedDeclaration"})
public class UpdateXmlFileReconfigureAction extends ReconfigureAction {
    private LinkedList<XpathActionComponent> actionElements = new LinkedList<XpathActionComponent>();
    private String factory;
    private XPathFactory xpathFactory;
    private DocumentBuilderFactory builderFactory;

    @SuppressWarnings({"UnusedDeclaration"})
    public void setFactory(final String factory) {
        this.factory = factory;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Set createSet() {
        return addComponent(new Set());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Del createDel() {
        return addComponent(new Del());
    }

    @Override
    public void doExecute(@Nullable final InputStream is, @NotNull final OutputStream os) throws IOException {
        try {
            log("Updating XML file", Project.MSG_VERBOSE);
            xpathFactory = XPathFactory.newInstance();
            if (factory != null) {
                builderFactory = DocumentBuilderFactory.newInstance(factory, UpdateXmlFileReconfigureAction.class.getClassLoader());
            } else {
                builderFactory = DocumentBuilderFactory.newInstance();
            }
            final DocumentBuilder builder = builderFactory.newDocumentBuilder();
            final Document doc = builder.parse(new CloseShieldInputStream(is));
            for (XpathActionComponent component : getEnabledComponents(XpathActionComponent.class)) {
                if (component.isStrict() == null) {
                    component.setStrict(strict);
                }
                component.process(doc);
            }
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            final LSOutput output = impl.createLSOutput();
            output.setByteStream(os);
            writer.write(doc, output);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    @Override
    public boolean isNonExistingSourceAllowed() {
        return false;
    }

    public abstract class XpathActionComponent extends ReconfigureActionComponent {
        protected String xpath;

        @SuppressWarnings({"UnusedDeclaration"})
        public void setXpath(final String xpath) {
            this.xpath = xpath;
        }

        public abstract void process(final Document xmlDoc) throws Exception;
    }

    public class Set extends XpathActionComponent {
        private String value;
        private String xmlValue;

        public void setValue(final String value) {
            this.value = value;
        }

        public void setXmlValue(final String xmlValue) {
            this.xmlValue = xmlValue;
        }

        @Override
        public void process(final Document xmlDoc) throws Exception {
            final XPathExpression xpathEl = xpathFactory.newXPath().compile(xpath);
            final NodeList list = (NodeList) xpathEl.evaluate(xmlDoc, XPathConstants.NODESET);
            if (list == null || list.getLength() == 0) {
                if (strict) {
                    throw new BuildException("Unable to find node at xpath " + xpath);
                } else {
                    return;
                }
            }
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                final String resolvedValue;
                if (value != null) {
                    resolvedValue = value;
                } else {
                    resolvedValue = task.getXmlValue(xmlValue);
                }
                log("Setting XML at " + xpath + " to " + resolvedValue, Project.MSG_VERBOSE);
                node.setNodeValue(resolvedValue);
            }
        }
    }

    public class Del extends XpathActionComponent {
        @Override
        public void process(final Document xmlDoc) throws Exception {
            final XPathExpression xpathEl = xpathFactory.newXPath().compile(xpath);
            final NodeList list = (NodeList) xpathEl.evaluate(xmlDoc, XPathConstants.NODESET);
            if (list == null || list.getLength() == 0) {
                if (strict) {
                    throw new BuildException("Unable to find node at xpath " + xpath);
                } else {
                    return;
                }
            }
            log("Deleting XML nodes at " + xpath + " found " + list.getLength(), Project.MSG_VERBOSE);
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getParentNode() == null) {
                    throw new BuildException("Unable to delete a node without a parent through xpath: " + xpath);
                }
                node.getParentNode().removeChild(node);
            }
        }
    }
}
