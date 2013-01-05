/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import com.kloudtek.buildmagic.tools.util.ArchiveHelper;
import com.kloudtek.buildmagic.tools.util.DataBuffer;
import com.kloudtek.buildmagic.tools.util.DataBufferManager;
import com.kloudtek.buildmagic.tools.util.TarArchive;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
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
    private String section = "misc";
    private String priority = "extra";
    private String depends = "";
    private String arch = "all";
    private String maintName;
    private String maintEmail;
    private String upstreamUrl;
    private String upstreamName;
    private static final byte[] DEBVERSION = "2.0\n".getBytes();
    private static final List<String> PRIORITIES = Arrays.asList("required", "important", "standard", "optional", "extra");
    private ArrayList<TemplateEntry> templateEntries = new ArrayList<TemplateEntry>();
    private CopyrightList copyrights = new CopyrightList();
    private DataBuffer copyright;
    private String license;
    private File licenseFile;
    private Changelog changelog;
    private String changelogData;
    private byte[] changelogDataGzipped;

    private void prepare() throws IOException {
        // validate parameters
        if (name == null || name.trim().length() == 0) {
            throw new BuildException("name attribute is missing or invalid");
        }
        if (version == null || version.trim().length() == 0) {
            throw new BuildException("version attribute is missing or invalid");
        }
        if (arch == null || arch.trim().length() == 0) {
            throw new BuildException("arch attribute is missing or invalid");
        }
        if (maintName == null || maintName.trim().length() == 0) {
            throw new BuildException("maintName attribute is missing");
        }
        if (maintEmail == null || maintEmail.trim().length() == 0) {
            throw new BuildException("maintEmail attribute is missing");
        }
        if (priority != null && !PRIORITIES.contains(priority.toLowerCase())) {
            log("Priority '" + priority + "' isn't recognized as a valid value", Project.MSG_WARN);
        }
        if (control != null) {
            for (final TemplateEntry entry : control.getTemplateEntries()) {
                if (entry.getPackageName() == null) {
                    entry.setPackageName(name);
                }
                templateEntries.add(entry);
            }
        }
        // generate copyright file
        if (copyrights.isEmpty()) {
            if (license == null) {
                throw new BuildException("No license has been specified");
            }
            if (licenseFile == null) {
                licenseFile = new File("license.txt");
            }
            copyrights.add(new Copyright(license, licenseFile));
        }
        copyrights.validate();
        copyright = dataBufferManager.createBuffer();
        copyrights.write(copyright, this);
        // generate changelog
        changelog = new Changelog(name);
        Changelog.Entry entry = changelog.createEntry();
        changelog.setDistributions("main"); // TODO
        entry.setMaintName(maintName);
        entry.setMaintEmail(maintEmail);
        entry.setChanges("Released");
        entry.setVersion(version);
        entry.setDate(new Date());
        changelog.validate();
        changelogData = changelog.export(Changelog.Type.DEBIAN);
        ByteArrayOutputStream changelogBuf = new ByteArrayOutputStream();
        GZIPOutputStream changelogGzipBuf = new GZIPOutputStream(changelogBuf);
        changelogGzipBuf.write(changelogData.getBytes());
        changelogGzipBuf.finish();
        changelogGzipBuf.close();
        changelogDataGzipped = changelogBuf.toByteArray();
    }

    @Override
    public void execute() throws BuildException {
        try {
            prepare();
            final ArArchiveOutputStream debFile = new ArArchiveOutputStream(new FileOutputStream(destfile));
            // create debian-binary
            debFile.putArchiveEntry(new ArArchiveEntry("debian-binary", DEBVERSION.length));
            debFile.write(DEBVERSION);
            debFile.closeArchiveEntry();
            // create control.tar.gz
            DataBuffer controlFile = createControlArchive();
            ArchiveHelper.write(debFile, controlFile, "control.tar.gz");
            controlFile.clear();
            // create data.tar.gz
            DataBuffer dataArchive = createDataArchive();
            ArchiveHelper.write(debFile, dataArchive, "data.tar.gz");
            dataArchive.clear();
            debFile.close();
            log("Created " + destfile.getPath());
        } catch (RuntimeException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            dataBufferManager.clear();
        }
    }

    @SuppressWarnings({"unchecked"})
    private DataBuffer createControlArchive() throws IOException {
        try {
            final DataBuffer dataBuffer = dataBufferManager.createBuffer();
            TarArchive controlArchive = new TarArchive(dataBuffer, getProject());
            controlArchive.write(generateControlFile(), "control");
            controlArchive.write(copyright, "copyright");
            if (control == null || !control.hasChangeLog()) {
                controlArchive.write(changelogData, "changelog");
            }
            if (control != null) {
                controlArchive.write(control.getResources());
            }
            if (!templateEntries.isEmpty()) {
                writeTemplate(controlArchive, templateEntries);
            }
            controlArchive.close();
            return dataBuffer;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void writeTemplate(final TarArchive tarArchive, final ArrayList<TemplateEntry> templateEntries) throws IOException {
        final Writer w = new StringWriter();
        for (Iterator<TemplateEntry> iterator = templateEntries.iterator(); iterator.hasNext(); ) {
            final TemplateEntry entry = iterator.next();
            if (entry.getFullId() == null) {
                throw new BuildException("template id missing");
            }
            if (entry.getShortDesc() == null) {
                throw new BuildException("template " + entry.getId() + " has no shortDesc defined");
            }
            w.write("Template: ");
            w.write(entry.getFullId());
            if (entry.getDefaultValue() != null) {
                w.write("\nDefault: ");
                w.write(entry.getDefaultValue());
            }
            w.write("\nType: ");
            w.write(entry.getType().name().toLowerCase());
            w.write("\nDescription: ");
            w.write(entry.getShortDesc());
            w.write("\n");
            String longDesc = entry.getLongDesc();
            if (longDesc == null) {
                longDesc = entry.getShortDesc();
            }
            w.write(generateExtendedControlField(longDesc));
            w.write("\n");
            if (iterator.hasNext()) {
                w.write("\n");
            }
        }
        w.close();
        tarArchive.write(w.toString(), "templates");
    }

    private byte[] generateControlFile() {
        final StringBuilder controlFile = new StringBuilder();
        final HashMap<String, String> attrs = new HashMap<String, String>();
        if (control != null) {
            for (final Control.Field field : control.getFields()) {
                final String name = field.getName();
                if (attrs.containsKey(name)) {
                    throw new BuildException("Duplicated control field: " + name);
                } else if (name.equals("Description")) {
                    throw new BuildException("package description musn't be defined using custom control field, use description element instead");
                } else if (name.equals("Description")) {
                    throw new BuildException("package description musn't be defined using custom control field, use description element instead");
                } else if (name.equals("Maintainer")) {
                    throw new BuildException("package Maintainer musn't be defined using custom control field, use maintName and maintEmail element instead");
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
        attrs.put("Section", section);
        attrs.put("Priority", priority);
        attrs.put("Maintainer", maintName + " <" + maintEmail + ">");
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
            TarArchive tarArchive = new TarArchive(dataBuffer, getProject());
            tarArchive.write(resources);
            tarArchive.write(copyright, "/usr/share/doc/" + name + "/copyright");
            tarArchive.write(changelogDataGzipped, "/usr/share/doc/" + name + "/changelog.gz");
            tarArchive.close();
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
    public void setSection(String section) {
        this.section = section;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setDepends(String depends) {
        this.depends = depends;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addFileset(final FileSet set) {
        add(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addTarFileset(final TarFileSet set) {
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

    @SuppressWarnings({"UnusedDeclaration"})
    public void addSymlink(final DebSymlink set) {
        add(set);
    }

    public void add(final ResourceCollection col) {
        resources.add(col);
    }

    public void addCopyright(final Copyright col) {
        copyrights.add(col);
    }

    public String getMaintName() {
        return maintName;
    }

    public void setMaintName(String maintName) {
        this.maintName = maintName;
    }

    public String getMaintEmail() {
        return maintEmail;
    }

    public void setMaintEmail(String maintEmail) {
        this.maintEmail = maintEmail;
    }

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public void setUpstreamUrl(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    public String getUpstreamName() {
        return upstreamName;
    }

    public void setUpstreamName(String upstreamName) {
        this.upstreamName = upstreamName;
    }

    public Control getControl() {
        return control;
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
    public Changelog.Entry createChangelog() {
        Changelog.Entry entry = changelog.createEntry();
        return entry;
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

    public TemplateEntry getTemplateEntry(@NotNull final String id) {
        for (final TemplateEntry entry : templateEntries) {
            if (id.contains("/")) {
                if (entry.getFullId().equals(id)) {
                    return entry;
                }
            } else {
                if (entry.getId().equals(id)) {
                    return entry;
                }
            }
        }
        throw new BuildException("No such template entry: " + id);
    }

}
