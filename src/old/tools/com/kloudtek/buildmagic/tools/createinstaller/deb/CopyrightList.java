/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import com.kloudtek.buildmagic.tools.util.DataBuffer;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CopyrightList extends ArrayList<Copyright> {
    private String year;

    public void validate() {
        for (Copyright copyright : this) {
            copyright.validate();
        }
    }

    public void write(DataBuffer copyright, CreateDebTask task) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(copyright));
        writer.write("Format: http://dep.debian.net/deps/dep5\n");
        if (task.getUpstreamName() != null) {
            writer.write("Upstream-Name: " + task.getUpstreamName() + " \n");
        }
        if (task.getUpstreamUrl() != null) {
            writer.write("Source: " + task.getUpstreamUrl() + " \n");
        }
        writer.write("\n");
        for (Copyright cr : this) {
            writer.write("Files: ");
            if (cr.getFiles() != null) {
                writer.write("*");
            } else {
                writer.write(cr.getFiles());
            }
            boolean first = true;
            for (CopyrightAuthor author : cr.getAuthors()) {
                if (first) {
                    writer.write("\nCopyright: ");
                    first = false;
                } else {
                    writer.write("\n           ");
                }
                String year = author.getYear() != null ? author.getYear() : getCurrentYear();
                writer.write(year);
                writer.write(" ");
                String name = author.getName() != null ? author.getName() : task.getMaintName();
                writer.write(name);
                String email = author.getEmail() != null ? author.getEmail() : task.getMaintEmail();
                writer.write(" <");
                writer.write(email);
                writer.write(">\nLicense: ");
                writer.write(cr.getLicense());
                writer.write("\n ");
                String licTxt = FileUtils.readFileToString(cr.getLicenseFile());
                licTxt = licTxt.replace("\n\n", "\n.\n").replace("\n", "\n ");
                writer.write(licTxt);
                writer.write("\n\n");
            }
        }
        writer.close();
    }

    public String getCurrentYear() {
        if (year == null) {
            year = Integer.toString(new GregorianCalendar().get(Calendar.YEAR));
        }
        return year;
    }
}
