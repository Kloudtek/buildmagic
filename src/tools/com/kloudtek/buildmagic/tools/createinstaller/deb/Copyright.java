/*
 * Copyright (c) KloudTek Ltd 2013.
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.ArrayList;

public class Copyright {
    private String files = "*";
    private String year;
    private String authorName;
    private String authorEmail;
    private String license;
    private File licenseFile;
    private ArrayList<CopyrightAuthor> authors = new ArrayList<CopyrightAuthor>();

    public Copyright() {
        authors.add(new CopyrightAuthor());
    }

    public Copyright(String license, File licenseFile) {
        this();
        this.license = license;
        this.licenseFile = licenseFile;
    }

    public ArrayList<CopyrightAuthor> getAuthors() {
        return authors;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getYear() {
        return authors.get(0).getYear();
    }

    public void setYear(String year) {
        authors.get(0).setYear(year);
    }

    public String getAuthorName() {
        return authors.get(0).getName();
    }

    public void setAuthorName(String authorName) {
        authors.get(0).setName(authorName);
    }

    public String getAuthorEmail() {
        return authors.get(0).getEmail();
    }

    public void setAuthorEmail(String authorEmail) {
        authors.get(0).setEmail(authorEmail);
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public File getLicenseFile() {
        return licenseFile;
    }

    public void setLicenseFile(File licenseFile) {
        this.licenseFile = licenseFile;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public CopyrightAuthor createAuthor() {
        CopyrightAuthor author = new CopyrightAuthor();
        authors.add(author);
        return author;
    }

    public void validate() {
        if (license == null) {
            throw new BuildException("copyright is missing license");
        }
        if (licenseFile == null) {
            throw new BuildException("copyright is missing license filename");
        }
        if (!licenseFile.exists()) {
            throw new BuildException("License file not found: " + licenseFile);
        }
    }
}
