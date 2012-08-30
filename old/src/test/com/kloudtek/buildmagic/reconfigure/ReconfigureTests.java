/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.reconfigure;

import com.kloudtek.buildmagic.TestHelper;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.testng.Assert.assertEquals;

@Test
public class ReconfigureTests {
    private static final String RSCBASE = "/com/kloudtek/buildmagic/reconfigure/resources/";
    private File tmpDir;
    private File testJar;

    @BeforeClass
    @Parameters({"tmpDir"})
    public void createTestJar(final String tmpDirPath) throws IOException {
        tmpDir = new File(tmpDirPath + File.separator + "reconfigure");
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Error creating tmp dir: "+tmpDir.getAbsolutePath());
        }
        testJar = new File(tmpDir, "test.ear");
        // Create EAR
        OutputStream testJarOutputStream = new BufferedOutputStream(new FileOutputStream(testJar));
        final JarArchiveOutputStream earJarOS = new JarArchiveOutputStream(testJarOutputStream);

        addDirToJar(earJarOS, "META-INF/");
        addResourceToJar(earJarOS, "META-INF/", "application.xml");
        addResourceToJar(earJarOS, "META-INF/", "weblogic-application.xml");
        earJarOS.putArchiveEntry(new JarArchiveEntry("testejb1.jar"));
        // create EJB
        final JarArchiveOutputStream ejbJarOS = new JarArchiveOutputStream(earJarOS);
        addDirToJar(ejbJarOS, "META-INF/");
        addResourceToJar(ejbJarOS, "META-INF/", "ejb-jar.xml");
        addResourceToJar(ejbJarOS, "META-INF/", "persistence.xml");
        ejbJarOS.finish();
        earJarOS.closeArchiveEntry();
        earJarOS.putArchiveEntry(new JarArchiveEntry("testwar.war"));
        final JarArchiveOutputStream warOS = new JarArchiveOutputStream(earJarOS);
        addDirToJar(warOS, "WEB-INF/");
        addResourceToJar(warOS, "WEB-INF/", "web.xml");
        addResourceToJar(warOS, "WEB-INF/", "test.properties");
        warOS.finish();
        earJarOS.closeArchiveEntry();

        earJarOS.close();
        System.out.println("Created " + testJar.getAbsolutePath());
    }

    private void addDirToJar(JarArchiveOutputStream outputStream, String dir) throws IOException {
        outputStream.putArchiveEntry(new JarArchiveEntry(dir));
        outputStream.closeArchiveEntry();
    }

    private void addResourceToJar(JarArchiveOutputStream outputStream, String destDir, String resourceName) throws IOException {
        outputStream.putArchiveEntry(new JarArchiveEntry(destDir + resourceName));
        final InputStream stream = getClass().getResourceAsStream(RSCBASE + resourceName);
        IOUtils.copy(stream, outputStream);
        outputStream.closeArchiveEntry();
    }

    private File executeProject(final String target, String... extraProps) throws IOException, ArchiveException {
        return executeProject(target, testJar, true, extraProps);
    }

    private File executeProject(final String target, File ear, boolean extract, String... extraProps) throws IOException, ArchiveException {
        try {
            if (extraProps != null && extraProps.length % 2 > 0) {
                throw new IllegalArgumentException("extraProps must be an even number (key/value pairs)");
            }
            final File destFile = new File(tmpDir, target + "-processed-test.ear");
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("ear", ear.getAbsolutePath());
            props.put("destear", destFile.getAbsolutePath());
            if (extraProps != null) {
                for (int i = 0; i < extraProps.length; i += 2) {
                    props.put(extraProps[i], extraProps[i + 1]);
                }
            }
            final URL xmlSourceUrl = getClass().getResource(RSCBASE + "persistence.xml");
            props.put("xmlSource", new File(xmlSourceUrl.toURI()).getAbsolutePath());
            TestHelper.executeAnt("reconfigure/test-reconfigure.xml", target, props);
            final File extracted = new File(destFile.getAbsolutePath() + ".extracted");
            if( extracted.exists() ) {
                if( extracted.isDirectory() ) {
                    FileUtils.deleteDirectory(extracted);
                } else {
                    FileUtils.deleteQuietly(extracted);
                }
            }
            FileUtils.copyFile(destFile, extracted);
            if (extract) {
                return extractZip(extracted);
            } else {
                return extracted;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private File extractZip(File zipFile) throws IOException, ArchiveException {
        ZipFile zf = null;
        try {
            zf = new JarFile(zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Error opening " + zipFile.getAbsolutePath(), e);
        }
        final Enumeration<? extends ZipEntry> zgEn = zf.entries();
        while (zgEn.hasMoreElements()) {
            zgEn.nextElement();
        }
        final File tmpZip = new File(zipFile.getAbsoluteFile() + ".unzipping");
        FileUtils.moveFile(zipFile, tmpZip);
        if (!zipFile.mkdirs()) {
            throw new IOException("Unable to create " + zipFile.getAbsolutePath());
        }
        final ArchiveStreamFactory aFact = new ArchiveStreamFactory();
        final ArchiveInputStream zis = aFact.createArchiveInputStream(new BufferedInputStream(new FileInputStream(tmpZip)));
        ArchiveEntry e;
        while ((e = zis.getNextEntry()) != null) {
            File nf = new File(zipFile + File.separator + e.getName().replace('/', File.separatorChar));
            if (e.isDirectory()) {
                if (!nf.mkdirs()) {
                    throw new IOException("Unable to create " + nf.getAbsolutePath());
                }
            } else {
                if (!nf.getParentFile().exists() && !nf.getParentFile().mkdirs()) {
                    throw new IOException("Unable to create " + nf.getAbsolutePath());
                }
                final FileOutputStream w = new FileOutputStream(nf);
                IOUtils.copy(zis, w);
                w.close();
                try {
                    // validation
                    aFact.createArchiveInputStream(new BufferedInputStream(new FileInputStream(nf))).close();
                    extractZip(nf);
                } catch (ArchiveException ex) {
                    // not an archive
                }
            }
        }
        if (!tmpZip.delete()) {
            throw new IOException("Unable to delete " + tmpZip.getAbsolutePath());
        }
        return zipFile;
    }

    public void testAddNewProperties() throws IOException, ArchiveException {
        final File result = executeProject("testAddNewProperties");
        assertExtractedProperties(result, "META-INF/test.properties", 2,
                "testkey1", "testvalue1",
                "testkey2", "testvalue2");
    }

    public void testAddNewPropertiesInNestedWar() throws IOException, ArchiveException {
        final File result = executeProject("testAddNewPropertiesInNestedWar");
        assertExtractedProperties(result, "testwar.war/WEB-INF/classes/test.properties", 2,
                "testkey3", "testvalue3",
                "testkey4", "testvalue4");
    }

    public void testModifyPropertiesInNestedWar() throws IOException, ArchiveException {
        final File result = executeProject("testModifyPropertiesInNestedWar");
        assertExtractedProperties(result, "testwar.war/WEB-INF/test.properties", 4,
                "chuck", "norris",
                "testkey5", "testvalue5",
                "testkey6", "testvalue6"
        );
    }

    public void testMultipleActionsOnSamePath() throws IOException, ArchiveException {
        // TODO !!!!!!!!!!!!!!!!!!!
//        final File result = executeProject("multipleActionsOnSamePath");
//        assertExtractedProperties(result, "/testwar.war/WEB-INF/classes/test.properties", 1,
//                "testkey1", "tesXXXue1"
//        );
    }

    public void testConditionalActions() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        try {
            final File result = executeProject("conditionalActions");
            Assert.assertFalse(getExtractedFile(result, "META-INF/test1.properties").exists());
            assertExtractedProperties(result, "META-INF/test2.properties", 2,
                    "testkey2", "testvalue2",
                    "testkey3", "testvalue3"
            );
            assertExtractedXmlUsingXpath(result, "/testejb1.jar/META-INF/persistence.xml"
                    , "//property[@name='debugLevel'][@value='High']");
        } catch (BuildException e) {
            Assert.assertTrue(e.getMessage().contains("Multiple actions on same path invalid"), "Wrong exception: " + e.getMessage());
        }
    }

    public void testUpdateXmlAttr() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        final File result = executeProject("updateXmlAttr");
        assertExtractedXmlUsingXpath(result, "/testejb1.jar/META-INF/persistence.xml"
                , "//class[2][contains(text(),'somepackage.SomeOtherClass')]"
                , "//property[@name='debugLevel'][@value='low']"
                , "not(//property[@name='selfdestruct'])");
    }

    public void testRegexReplace() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        final File result = executeProject("regexReplace");
        assertExtractedXmlUsingXpath(result, "/testejb1.jar/META-INF/persistence.xml"
                , "//jta-data-source[contains(text(),'examXXaSoYYndi')]");
        assertExtractedXmlUsingXpath(result, "/testejb1.jar/META-INF/ejb-jar.xml"
                , "//ejb-name[contains(text(),'BadBean')]");
    }

    public void testInsert() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        final File result = executeProject("testInsert");
        final String orig = IOUtils.toString(getClass().getResourceAsStream("/com/kloudtek/buildmagic/reconfigure/test-reconfigure.xml"));
        final String inserted = FileUtils.readFileToString(new File(result+File.separator+"testejb1.jar"+File.separator+"META-INF"+File.separator+"test-reconfigure.xml"));
        assertEquals(inserted,orig);
    }

    public void testXmlSource() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        final File result = executeProject("xmlSource");
        assertExtractedProperties(result, "META-INF/fromXml.properties", 1,
                "testkey1", "High"
        );
    }

    public void testStrictCausesFailure() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        try {
            final File result = executeProject("testStrictCausesFailure");
            Assert.fail();
        } catch (BuildException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to find node"));
        }
    }

    public void testNotStrictStopsFailure() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
        final File result = executeProject("testNotStrictStopsFailure");
    }

//    public void testDuplicatedZipEntry() throws IOException, ArchiveException, SAXException, XPathExpressionException, ParserConfigurationException {
//        throw new RuntimeException("TODO - handle bloody duplicated zip entries");
//    }

//    public void testUpdateXmlAttrWithMultipleXPathMatches() throws IOException, ArchiveException {
//        final File result = executeProject("updateXmlAttrWithMultipleXPathMatches");
//  TODO
//    }

    /* UTILITY METHODS */

    private static File getExtractedFile(final File result, final String path) throws FileNotFoundException {
        return new File(result + File.separator + path.replace('/', File.separatorChar));
    }

    private static InputStream getExtractedFileInputStream(final File result, final String path) throws FileNotFoundException {
        final File extractedFile = getExtractedFile(result, path);
        return new FileInputStream(extractedFile);
    }

    private static Properties getExtractedProperties(final File result, final String path) throws IOException {
        Properties props = new Properties();
        props.load(getExtractedFileInputStream(result, path));
        return props;
    }

    private static Document getExtractedXml(final File result, final String path) throws IOException, ParserConfigurationException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getExtractedFile(result, path));
    }

    private static void assertExtractedProperties(File result, String path, int count, String... keyvalues) throws IOException {
        Properties props = getExtractedProperties(result, path);
        assertEquals(props.size(), count);
        for (int i = 0; i < keyvalues.length; i += 2) {
            assertEquals(props.getProperty(keyvalues[i]), keyvalues[i + 1]);
        }
    }

    private static void assertExtractedXmlUsingXpath(File result, String path, String... xpaths) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        final Document doc = getExtractedXml(result, path);
        final XPathFactory xpathFac = XPathFactory.newInstance();
        for (String xpath : xpaths) {
            final XPathExpression xpathEx = xpathFac.newXPath().compile(xpath);
            final Boolean ret = (Boolean) xpathEx.evaluate(doc, XPathConstants.BOOLEAN);
            Assert.assertTrue(ret, "Xpath check failed: " + xpath);
        }
    }
}
