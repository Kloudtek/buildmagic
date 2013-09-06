package com.kloudtek.buildmagic;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Command line interface
 */
public class Cli {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("init")) {
            try {
                FileWriter fileWriter = new FileWriter("build.xml");
                fileWriter.write("<project name=\"[PROJECT]\" xmlns:bm=\"antlib:com.kloudtek.buildmagic\">\n" +
                        "    <property name=\"ivysettings.url\" value=\"http://s3.amazonaws.com/ivy.kloudtek.com/ivysettings.xml\"/>\n" +
                        "    <bm:template name=\"ivy\"/>\n" +
                        "    <bm:template name=\"simple-java\"/>\n" +
                        "</project>");
                fileWriter.close();
                fileWriter = new FileWriter("ivy.xml");
                fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ivy-module version=\"2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "            xsi:noNamespaceSchemaLocation=\"http://ant.apache.org/ivy/schemas/ivy.xsd\">\n" +
                        "    <info organisation=\"[org]\" module=\"[module]\"/>\n" +
                        "    <configurations>\n" +
                        "    </configurations>\n" +
                        "    <publications>\n" +
                        "    </publications>\n" +
                        "    <dependencies>\n" +
                        "    </dependencies>\n" +
                        "</ivy-module>");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            usage();
        }
    }

    private static void usage() {
        System.out.println("usage: buildmagic [command]\n");
        System.out.println("Commands:");
        System.out.println("  init\tCreates default ant build script with ivy template");
    }
}
