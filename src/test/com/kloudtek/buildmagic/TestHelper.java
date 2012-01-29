/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.CRC32;

import static org.testng.Assert.assertEquals;

public class TestHelper {
    private static final String BASE = "/com/kloudtek/buildmagic/";
    private static CRC32 crc;
    private static File[] list1;

    public static Project executeAnt(String buildScript, String target, Map<String, String> properties) {
        DefaultLogger antLogger = new DefaultLogger();
        antLogger.setErrorPrintStream(System.err);
        antLogger.setOutputPrintStream(System.out);
        antLogger.setMessageOutputLevel(Project.MSG_INFO);
        final File resource = getResourceFile(buildScript);
        Project project = new Project();
        project.init();
        project.setBasedir(resource.getParent());
        project.addBuildListener(antLogger);
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                project.setProperty(entry.getKey(), entry.getValue());
            }
        }
        ProjectHelper.getProjectHelper().parse(project, resource);
        try {
            project.executeTarget(target);
        } catch (BuildException e) {
            e.printStackTrace();
            throw e;
        }
        return project;
    }

    public static File getResourceFile(final String buildScript) {
        try {
            return new File(TestHelper.class.getResource(BASE + buildScript).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static File createDir(final File basedir, final String name, String cpContent) throws IOException {
        final URL url = TestHelper.class.getResource(BASE + cpContent);
        try {
            return createDir(basedir, name, new File(url.toURI()));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public static File createDir(final String basedir, final String name, String cpContent) throws IOException {
        return createDir(new File(basedir), name, cpContent);
    }

    public static File createDir(final String basedir, final String name, File... content) throws IOException {
        return createDir(new File(basedir), name, content);
    }

    public static File createDir(final File basedir, final String name, File... content) throws IOException {
        final File tempFile = new File(basedir, name);
        if (tempFile.exists()) {
            FileUtils.deleteDirectory(tempFile);
        }
        if (!tempFile.exists() && !tempFile.mkdirs()) {
            throw new IOException("Unable to create " + tempFile.getAbsolutePath());
        }
        if (content != null && content.length > 0) {
            for (File file : content) {
                if (file.isDirectory()) {
                    FileUtils.copyDirectory(file, tempFile);
                } else {
                    FileUtils.copyFileToDirectory(file, tempFile);
                }
            }
        }
        return tempFile;
    }

    public static void compareDirs(final String resource, final File dir) throws IOException {
        compareDirs(getResourceFile(resource), dir);
    }

    public static void compareDirs(final File dir1, final File dir2) throws IOException {
        list1 = dir1.listFiles();
        final List<File> files1 = new ArrayList<File>(Arrays.asList(list1));
        final List<File> files2 = new ArrayList<File>(Arrays.asList(dir2.listFiles()));
        removeSvn(files1);
        removeSvn(files2);
        assertEquals(files1.size(), files2.size());
        while (!files1.isEmpty()) {
            File f1 = files1.remove(0);
            for (File f2 : new ArrayList<File>(files2)) {
                if (f1.getName().equals(f2.getName())) {
                    assertEquals(f1.isDirectory(), f2.isDirectory());
                    if (f1.isDirectory()) {
                        files1.addAll(Arrays.asList(f1.listFiles()));
                        files2.addAll(Arrays.asList(f2.listFiles()));
                    } else {
                        assertEquals(getCRC(f1), getCRC(f2));
                    }
                }
            }
        }
    }

    private static void removeSvn(final List<File> files) {
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File file = iterator.next();
            if (file.isDirectory() && file.getName().equals(".svn")) {
                iterator.remove();
            }
        }
    }

    private static long getCRC(final File file) throws IOException {
        crc = new CRC32();
        crc.update(FileUtils.readFileToByteArray(file));
        return crc.getValue();
    }
}
