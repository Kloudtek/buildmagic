/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.xml;

import com.kloudtek.buildmagic.TestHelper;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

public class XmlDocumentTests {
    @Test
    public void testXmlDoc() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        final URL testXml = getClass().getResource("testxml.xml");
        HashMap<String,String> p = new HashMap<String, String>();
        p.put("url",testXml.toString());
        final File tmp = File.createTempFile("bmtest", null);
        try {
            p.put("dest",tmp.getAbsolutePath());
            TestHelper.executeAnt("xml/xml-test-build.xml","testXmlDoc",p);
            if( !tmp.exists() ) {
                throw new BuildException("Dest not created");
            }
            System.out.println(FileUtils.readFileToString(tmp));
            final Document genDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tmp);
            final XPath xp = XPathFactory.newInstance().newXPath();
            final XPathExpression xpe = xp.compile("/test/bla[contains(text(),'yey')]/../ble[contains(text(),'Wop')]");
            Assert.assertTrue((Boolean) xpe.evaluate(genDoc, XPathConstants.BOOLEAN));
        } finally {
            if( tmp.exists() && ! tmp.delete() ) {
                tmp.deleteOnExit();
            }
        }
    }

    @Test
    public void testXmlDocXpathProperties() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        final URL testXml = getClass().getResource("testxml.xml");
        final HashMap<String,String> p = new HashMap<String, String>();
        p.put("url",testXml.toString());
        final Project project = TestHelper.executeAnt("xml/xml-test-build.xml", "testXmlDocXpathProperties", p);
        Assert.assertEquals(project.getProperty("result"),"yey");
    }
}
