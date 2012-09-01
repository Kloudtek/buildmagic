/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class PythonScriptWriter {
    private StringWriter writer = new StringWriter();
    private int indent = 0;
    private boolean elseif;
    private boolean firstElseif;

    public void raw(final String raw) {
        writer.write(raw);
    }

    public void indent() {
        for (int i = 0; i < indent; i++) {
            writer.write('\t');
        }
    }

    public void line(final String line) {
        indent();
        writer.write(line);
        writer.write("\n");
    }

    public void doImport(final String module) {
        indent();
        writer.write("import ");
        writer.write(module);
        writer.write("\n");
    }

    public void newLine() {
        writer.write("\n");
    }

    public void defClass(final String className) {
        indent();
        writer.write("class ");
        writer.write(className);
        writer.write(":\n");
        indent++;
    }

    public void defMethod(final String methodName, final String... args) {
        indent();
        writer.write("def ");
        writer.write(methodName);
        writer.write("(");
        boolean first = true;
        if (args != null && args.length > 0) {
            for (final String arg : args) {
                if (first) {
                    first = false;
                } else {
                    writer.write(',');
                }
                writer.write(arg);
            }
        }
        writer.write("):\n");
        indent++;
    }

    public void defClassMethod(final String methodName, final String... args) {
        final ArrayList<String> argList = new ArrayList<String>();
        argList.add("self");
        argList.addAll(Arrays.asList(args));
        defMethod(methodName, argList.toArray(new String[argList.size()]));
    }

    public void endBlock() {
        indent--;
    }

    public byte[] getBytes() {
        return writer.toString().getBytes();
    }

    public void doTry() {
        indent();
        writer.write("try:\n");
        indent++;
    }

    public void except() {
        indent--;
        indent();
        writer.write("except:\n");
        indent++;
    }

    public void except(final String exception) {
        indent--;
        indent();
        writer.write("except ");
        writer.write(exception);
        writer.write(":\n");
        indent++;
    }

    public void doIf(final String txt) {
        indent();
        writer.write("if ");
        writer.write(txt);
        writer.write(":\n");
        indent++;
    }

    public void doElseIf(final String txt) {
        if (!elseif || firstElseif) {
            if (elseif) {
                firstElseif = false;
            }
            indent();
            writer.write("if ");
            writer.write(txt);
            writer.write(":\n");
            indent++;
        } else {
            indent--;
            indent();
            writer.write("elif ");
            writer.write(txt);
            writer.write(":\n");
            indent++;
        }
    }

    public void doWhile(final String txt) {
        indent();
        writer.write("while ");
        writer.write(txt);
        writer.write(":\n");
        indent++;
    }

    public void stderr(final String txt) {
        indent();
        writer.write("sys.stderr.write('");
        writer.write(txt);
        writer.write("\\n')\n");
    }

    public void lastLine(final String line) {
        line(line);
        endBlock();
    }

    public void doElse() {
        indent--;
        indent();
        writer.write("else:\n");
        indent++;
    }

    public void comment(final String txt) {
        indent();
        writer.write("# ");
        writer.write(txt);
        writer.write("\n");
    }

    public void startElseIf() {
        elseif = true;
        firstElseif = true;
    }

    public void endElseIf() {
        endBlock();
        elseif = false;
    }
}
