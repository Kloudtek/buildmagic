/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb;

import com.kloudtek.buildmagic.util.ArchiveHelper;
import com.kloudtek.buildmagic.util.DataBuffer;
import com.kloudtek.buildmagic.util.DataBufferManager;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class CreateDebTask extends Task {
    private DataBufferManager dataBufferManager = new DataBufferManager();
    private ArrayList<ResourceCollection> resources = new ArrayList<ResourceCollection>();
    private Control control;
    private Description desc;
    private File destfile;
    private String name;
    private String version;
    private String arch = "all";
    private static final byte[] DEBVERSION = "2.0\n".getBytes();
    private static final List<String> PRIORITIES = Arrays.asList("required", "important", "standard", "optional", "extra");
    private static final List<String> SECTIONS = Arrays.asList("admin", "cli-mono", "comm", "database", "devel", "debug", "doc", "editors", "electronics", "embedded", "fonts", "games", "gnome", "graphics", "gnu-r", "gnustep", "hamradio", "haskell", "httpd", "interpreters", "java", "kde", "kernel", "libs", "libdevel", "lisp", "localization", "mail", "math", "misc", "net", "news", "ocaml", "oldlibs", "otherosfs", "perl", "php", "python", "ruby", "science", "shells", "sound", "tex", "text", "utils", "vcs", "video", "web", "x11", "xfce", "zope");
    private ArrayList<TemplateEntry> templateEntries = new ArrayList<TemplateEntry>();

    @SuppressWarnings({"UnusedDeclaration"})
    public void setDestfile(final File destfile) {
        this.destfile = destfile;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setName(final String name) {
        this.name = name;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setVersion(final String version) {
        this.version = version;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setArch(final String arch) {
        this.arch = arch;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addFileset(final FileSet set) {
        add(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addDebFileset(final DebFileSet set) {
        add(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addZipGroupFileset(final FileSet set) {
        add(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addZipfileset(final ZipFileSet set) {
        add(set);
    }

    public void add(final ResourceCollection col) {
        resources.add(col);
    }

    public Control getControl() {
        return control;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Control createControl() {
        if (control == null) {
            control = new Control(this);
            return control;
        } else {
            throw new BuildException("Only one control element allowed");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Description createDescription() {
        if (desc != null) {
            throw new BuildException("Duplicated description elements");
        }
        desc = new Description();
        return desc;
    }

    public TemplateEntry getTemplateEntry( @NotNull final String id ) {
        for (final TemplateEntry entry : templateEntries) {
            if( id.contains("/") ) {
                if( entry.getFullId().equals(id) ) {
                    return entry;
                }
            } else {
                if( entry.getId().equals(id) ) {
                    return entry;
                }
            }
        }
        throw new BuildException("No such template entry: "+id);
    }

    private void setup() {
        if (name == null || name.length() == 0) {
            throw new BuildException("name attribute is missing or invalid");
        }
        if (version == null || version.length() == 0) {
            throw new BuildException("version attribute is missing or invalid");
        }
        if (arch == null || arch.length() == 0) {
            throw new BuildException("arch attribute is missing or invalid");
        }
        if( control != null ) {
            for (final TemplateEntry entry : control.getTemplateEntries()) {
                if( entry.getPackageName() == null ) {
                    entry.setPackageName(name);
                }
                templateEntries.add(entry);
            }
            // initialise all script actions
            for (final DebScript script : control.getScripts()) {
                script.init();
            }
            // getting templates from script actions that supports generating them
            for (final DebScript script : control.getScripts()) {
                for (final ScriptAction scriptAction : script.getScriptActions()) {
                    final Collection<? extends TemplateEntry> entries = scriptAction.generateTemplateEntries();
                    if (entries != null && !entries.isEmpty()) {
                        templateEntries.addAll(entries);
                    }
                }
            }
            for (final DebScript script : control.getScripts()) {
                script.process();
            }
        }
    }

    @Override
    public void execute() throws BuildException {
        setup();
        try {
            final ArArchiveOutputStream debFile = new ArArchiveOutputStream(new FileOutputStream(destfile));
            // create debian-binary
            debFile.putArchiveEntry(new ArArchiveEntry("debian-binary", DEBVERSION.length));
            debFile.write(DEBVERSION);
            debFile.closeArchiveEntry();
            // create control.tar.gz
            final DataBuffer controlArchive = createControlArchive();
            ArchiveHelper.writeDataBufferToAr(debFile, controlArchive, "control.tar.gz");
            // create data.tar.gz
            ArchiveHelper.writeDataBufferToAr(debFile, createDataArchive(), "data.tar.gz");
            debFile.close();
            log("Created "+destfile.getPath());
        } catch (RuntimeException e) {
            throw new BuildException(e);
        } catch ( IOException e) {
            throw new BuildException(e);
        } finally {
            dataBufferManager.clear();
        }
    }

    @SuppressWarnings({"unchecked"})
    private DataBuffer createControlArchive() throws IOException {
        try {
            final DataBuffer dataBuffer = dataBufferManager.createBuffer();
            final TarArchiveOutputStream data = new TarArchiveOutputStream(new GZIPOutputStream(dataBuffer));
            data.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            writeData(data, generateControlFile());
            // TODO: check the scripts haven't already been generated
            if( control != null ) {
                final DebScript preInst = control.getScript("preinst");
                final DebScript postInst = control.getScript("postinst");
                final DebScript postRmScript = control.getScript("postrm");
                if (control.isPurge() && !templateEntries.isEmpty()) {
                    postRmScript.addStageLine(DebScript.MAIN,"if action == 'purge':");
                    postRmScript.addStageLine(DebScript.MAIN,"    db.purge()");
                }
                writeScript(preInst, data);
                writeScript(postInst, data);
                writeScript(postRmScript, data);
                addResourcesToTar(data, control.getResources());
            }
            if (!templateEntries.isEmpty()) {
                writeTemplate(data, templateEntries);
            }
            data.close();
            return dataBuffer;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void writeTemplate(final TarArchiveOutputStream stream, final ArrayList<TemplateEntry> templateEntries) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final Writer w = new BufferedWriter(new OutputStreamWriter(buf));
        for (Iterator<TemplateEntry> iterator = templateEntries.iterator(); iterator.hasNext();) {
            final TemplateEntry entry = iterator.next();
            if( entry.getFullId() == null ) {
                throw new BuildException("template id missing");
            }
            if( entry.getShortDesc() == null ) {
                throw new BuildException("template "+entry.getId()+" has no shortDesc defined");
            }
            w.write("Template: ");
            w.write(entry.getFullId());
            if( entry.getDefaultValue() != null ) {
                w.write("\nDefault: ");
                w.write(entry.getDefaultValue());
            }
            w.write("\nType: ");
            w.write(entry.getType().name().toLowerCase());
            w.write("\nDescription: ");
            w.write(entry.getShortDesc());
            w.write("\n");
            String longDesc = entry.getLongDesc();
            if( longDesc == null ) {
                longDesc = entry.getShortDesc();
            }
            w.write(generateExtendedControlField(longDesc));
            w.write("\n");
            if (iterator.hasNext()) {
                w.write("\n");
            }
        }
        w.close();
        final byte[] data = buf.toByteArray();
        final TarArchiveEntry entry = new TarArchiveEntry("templates");
        entry.setSize(data.length);
        stream.putArchiveEntry(entry);
        stream.write(data);
        stream.closeArchiveEntry();
    }

    private void writeData(final TarArchiveOutputStream stream, final byte[] controlFileData) throws IOException {
        final TarArchiveEntry controlEntry = new TarArchiveEntry("control");
        controlEntry.setSize(controlFileData.length);
        stream.putArchiveEntry(controlEntry);
        stream.write(controlFileData);
        stream.closeArchiveEntry();
    }

    private void addResourcesToTar(final TarArchiveOutputStream data, final Collection<ResourceCollection> collections) throws IOException {
        final HashSet<String> dataParentDirs = new HashSet<String>();
        for (final ResourceCollection rsCol : collections) {
            final Iterator rsIt = rsCol.iterator();
            while (rsIt.hasNext()) {
                final Resource rs = (Resource) rsIt.next();
                final StringBuilder fnBuf = new StringBuilder();
                if (rsCol instanceof ArchiveFileSet) {
                    fnBuf.append(((ArchiveFileSet) rsCol).getPrefix(getProject()));
                    if (!rs.getName().startsWith(File.separator)) {
                        fnBuf.append(File.separator);
                    }
                }
                fnBuf.append(rs.getName());
                final String filename = fnBuf.toString();
                final String user;
                final String group;
                Integer filemode = null;
                if (rsCol instanceof DebFileSet) {
                    user = ((DebFileSet) rsCol).getUser();
                    group = ((DebFileSet) rsCol).getGroup();
                    final String filemodeStr = ((DebFileSet) rsCol).getFilemode();
                    if( filemodeStr != null ) {
                        filemode = Integer.valueOf(filemodeStr);
                    }
                } else {
                    user = null;
                    group = null;
                }
                ArchiveHelper.createTarParentDirs(dataParentDirs, data, filename, user, group, filemode);
                ArchiveHelper.writeTarEntry(data, rs, filename, user, group, filemode);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void writeScript(final DebScript script, final TarArchiveOutputStream stream) throws IOException {
        if (!script.isScriptEmpty()) {
            final byte[] scriptData = script.generate();
            final TarArchiveEntry entry = new TarArchiveEntry(script.getName());
            entry.setSize(scriptData.length);
            stream.putArchiveEntry(entry);
            stream.write(scriptData);
            stream.closeArchiveEntry();
        }
    }

    private byte[] generateControlFile() {
        final StringBuilder controlFile = new StringBuilder();
        final HashMap<String, String> attrs = new HashMap<String, String>();
        if( control != null ) {
            for (final Control.Field field : control.getFields()) {
                final String name = field.getName();
                if (attrs.containsKey(name)) {
                    throw new BuildException("Duplicated control field: " + name);
                } else if (name.equals("Description")) {
                    throw new BuildException("package description musn't be defined using custom control field, use description element instead");
                } else if (name.equals("Package")) {
                    throw new BuildException("package name musn't be defined using custom control field, use 'name' attribute instead");
                } else if (name.equals("Version")) {
                    throw new BuildException("package Version musn't be defined using custom control field, use 'version' attribute instead");
                } else if (name.equals("Architecture")) {
                    throw new BuildException("package Architecture musn't be defined using custom control field, use 'arch' attribute instead");
                } else {
                    attrs.put(name, field.getValue());
                }
            }
        }
        attrs.put("Package", name);
        attrs.put("Version", version);
        attrs.put("Architecture", arch);
        if (!attrs.containsKey("Installed-Size")) {
            int totalSize = 0;
            for (final ResourceCollection rsCol : resources) {
                final Iterator rsIt = rsCol.iterator();
                while (rsIt.hasNext()) {
                    totalSize += ((Resource) rsIt.next()).getSize();
                }
            }
            attrs.put("Installed-Size", Integer.toString(totalSize / 1024));
        }
        for (final Map.Entry<String, String> entry : attrs.entrySet()) {
            controlFile.append(entry.getKey());
            controlFile.append(": ");
            controlFile.append(entry.getValue());
            controlFile.append('\n');
        }
        if (desc != null) {
            controlFile.append("Description: ");
            controlFile.append(desc.getShortDesc());
            controlFile.append('\n');
            controlFile.append(' ');
            final String ldesc = desc.getLongDesc();
            if (ldesc != null) {
                controlFile.append(generateExtendedControlField(ldesc));
            }
        }
        return controlFile.toString().getBytes();
    }

    private String generateExtendedControlField(final String txt) {
        final StringBuilder buf = new StringBuilder(" ");
        final char[] ldescChars = txt.trim().toCharArray();
        for (int i = 0; i < ldescChars.length; i++) {
            buf.append(ldescChars[i]);
            if (ldescChars[i] == '\n') {
                buf.append(' ');
                if (i + 1 < ldescChars.length && ldescChars[i + 1] == '\n') {
                    buf.append(".");
                }
            }
        }
        buf.append('\n');
        return buf.toString();
    }

    private DataBuffer createDataArchive() throws IOException {
        final File dataTmp = File.createTempFile("bmbuildata", ".tmp.tar.gz");
        try {
            final DataBuffer dataBuffer = dataBufferManager.createBuffer();
            final TarArchiveOutputStream data = new TarArchiveOutputStream(new GZIPOutputStream(dataBuffer));
            data.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            addResourcesToTar(data, resources);
            data.close();
            return dataBuffer;
        } catch (Exception e) {
            FileUtils.deleteQuietly(dataTmp);
            throw new BuildException(e);
        }
    }


    public String getName() {
        return name;
    }

    public class Description {
        private String shortDesc = "";
        private String longDesc = "";

        public String getShortDesc() {
            return shortDesc;
        }

        public String getLongDesc() {
            return longDesc;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void setShort(final String shortDesc) {
            this.shortDesc = shortDesc;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void setLong(final String longDesc) {
            this.longDesc = longDesc;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void addText(final String longDesc) {
            this.longDesc += getProject().replaceProperties(longDesc);
        }
    }
}
