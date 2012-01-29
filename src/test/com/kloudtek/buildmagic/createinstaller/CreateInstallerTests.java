/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller;

import com.kloudtek.buildmagic.TestHelper;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.kloudtek.buildmagic.TestHelper.compareDirs;
import static com.kloudtek.buildmagic.TestHelper.executeAnt;

public class CreateInstallerTests {
    @Test
    @Parameters(value = {"tmpDir"})
    public void testSimpleCreateDeb(@Optional String tmpDirPath) throws IOException {
        if( tmpDirPath == null ) {
            tmpDirPath = "_tmp";
        }
        File tmpDir = TestHelper.createDir(tmpDirPath, "createinstaller-simple");
        final String source = "reconfigure/resources";
        File data = TestHelper.createDir(tmpDir, "data", source);
        File installDir = TestHelper.createDir(tmpDir, "install");
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("src", data.getAbsolutePath());
        props.put("workspace", tmpDir.getAbsolutePath());
        props.put("installdir", installDir.getAbsolutePath());
        executeAnt("createinstaller/createinstallers-build.xml", "simple-createdeb", props);
        compareDirs(source, new File(installDir.getAbsolutePath() + File.separator + "opt" + File.separator + "bmtest"));
    }

    @Test(groups = "needsudo")
    @Parameters(value = {"tmpDir"})
    public void testComplexCreateDeb(@Optional String tmpDirPath) throws IOException {
        if( tmpDirPath == null ) {
            tmpDirPath = "_tmp";
        }
        File tmpDir = TestHelper.createDir(tmpDirPath, "createinstaller-complex");
        final String source = "reconfigure/resources";
        File data = TestHelper.createDir(tmpDir, "data", source);
        File installDir = TestHelper.createDir(tmpDir, "install");
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("src", data.getAbsolutePath());
        props.put("workspace", tmpDir.getAbsolutePath());
        props.put("installdir", installDir.getAbsolutePath());
        try {
            executeAnt("createinstaller/createinstallers-build.xml", "complex-createdeb", props);
            compareDirs(source, new File("/opt/bmtest"));
        } finally {
            executeAnt("createinstaller/createinstallers-build.xml", "uninstall", props);
        }
    }
}
