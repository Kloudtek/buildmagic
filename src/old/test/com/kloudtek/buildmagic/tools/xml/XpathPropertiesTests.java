/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic.tools.xml;

import com.kloudtek.buildmagic.tools.TestHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.easymock.classextension.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.HashMap;

@Test
public class XpathPropertiesTests {
    public void testSimpleSuccessNode() {
        final String testKey1 = "test1";
        final XpathProperties xpathProperties = createXpathProperties();
        final Project project = xpathProperties.getProject();
        project.setProperty(testKey1, "exampleDataSourceJndi");
        EasyMock.replay(project);
        final XpathProperties.Entry entry1 = xpathProperties.createEntry();
        entry1.setKey(testKey1);
        entry1.setXpath("//jta-data-source/text()");
        xpathProperties.execute();
        EasyMock.verify(project);
    }

    public void testSimpleSuccessBoolean() {
        final String testKey1 = "test1";
        final XpathProperties xpathProperties = createXpathProperties();
        final Project project = xpathProperties.getProject();
        project.setProperty(testKey1, "true");
        EasyMock.replay(project);
        final XpathProperties.Entry entry1 = xpathProperties.createEntry();
        entry1.setKey(testKey1);
        entry1.setXpath("//jta-data-source");
        entry1.setType("boolean");
        xpathProperties.execute();
        EasyMock.verify(project);
    }

    public void testSimpleSuccessNumber() {
        final String testKey1 = "test1";
        final XpathProperties xpathProperties = createXpathProperties();
        final Project project = xpathProperties.getProject();
        project.setProperty(testKey1, "2.0");
        EasyMock.replay(project);
        final XpathProperties.Entry entry1 = xpathProperties.createEntry();
        entry1.setKey(testKey1);
        entry1.setXpath("count(//property)");
        entry1.setType("number");
        xpathProperties.execute();
        EasyMock.verify(project);
    }

    public void testWithPrefix() {
        final XpathProperties xpathProperties = createXpathProperties();
        xpathProperties.setPrefix("test");
        final Project project = xpathProperties.getProject();
        project.setProperty("test.test1", "exampleDataSourceJndi");
        EasyMock.replay(project);
        final XpathProperties.Entry entry1 = xpathProperties.createEntry();
        entry1.setKey("test1");
        entry1.setXpath("//jta-data-source/text()");
        xpathProperties.execute();
        EasyMock.verify(project);
    }

    public void testWrongXpath() {
        final XpathProperties xpathProperties = createXpathProperties();
        final XpathProperties.Entry entry1 = xpathProperties.createEntry();
        EasyMock.replay(xpathProperties.getProject());
        entry1.setKey("test1");
        entry1.setXpath("//jta-data");
        try {
            xpathProperties.execute();
        } catch (BuildException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to find"));
        }
    }

    public void testWrongXpathNoFailOnMissing() {
        final String testKey1 = "test1";
        final XpathProperties xpathProperties = createXpathProperties();
        final Project project = xpathProperties.getProject();
        EasyMock.replay(project);
        final XpathProperties.Entry entry1 = xpathProperties.createEntry();
        entry1.setKey(testKey1);
        entry1.setXpath("/xxxx/aaaa/ddfd//qq");
        entry1.setFailOnNoMatch(false);
        xpathProperties.execute();
        EasyMock.verify(project);
    }

    public void testXpathWithNamespace() {
        final URL testXml = getClass().getResource("testxmlns.xml");
        final HashMap<String, String> p = new HashMap<String, String>();
        p.put("url", testXml.toString());
        final Project project = TestHelper.executeAnt("xml/xml-test-build.xml", "testXpathWithNamespace", p);
        Assert.assertEquals(project.getProperty("result"), "yey");
    }

    private XpathProperties createXpathProperties() {
        final XpathProperties xpathProperties = new XpathProperties();
        final Project project = EasyMock.createStrictMock(Project.class);
        EasyMock.expect(project.getReference(XmlNamespaceMapping.REFNAME)).andReturn(new HashMap<String, String>());
        xpathProperties.setProject(project);
        xpathProperties.setUrl(getClass().getResource("/com/kloudtek/buildmagic/reconfigure/resources/persistence.xml"));
        return xpathProperties;
    }
}
