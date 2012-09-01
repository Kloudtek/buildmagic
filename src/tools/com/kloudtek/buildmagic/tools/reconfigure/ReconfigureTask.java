/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.reconfigure;

import com.kloudtek.buildmagic.tools.util.DataBuffer;
import com.kloudtek.buildmagic.tools.util.DataBufferManager;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Task used to change configuration details (property files or xml) inside a jar/war/zip/ear file.
 */
public class ReconfigureTask extends Task {
    private File file;
    private File destFile;
    private OutputStream dest;
    private final LinkedList<ReconfigureAction> actions = new LinkedList<ReconfigureAction>();
    private final HashMap<String, FileType> suffixToArchTypes = new HashMap<String, FileType>();
    private File xmlSource;
    private Document xmlDoc;
    private final HashMap<String, String> xpathCache = new HashMap<String, String>();
    private final XPath xPath;
    private DataBufferManager dataBufferManager;
    private boolean updateSelf;
    private boolean strict = true;

    public ReconfigureTask() {
        xPath = XPathFactory.newInstance().newXPath();
    }

    public DataBufferManager getDataBufferManager() {
        return dataBufferManager;
    }

    public boolean isStrict() {
        return strict;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    public File getFile() {
        return file;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setFile(final File file) {
        this.file = file;
    }

    public File getDestFile() {
        return destFile;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setDestFile(final File destFile) {
        this.destFile = destFile;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setXmlSource(final File xmlSource) {
        this.xmlSource = xmlSource;
    }

    public void add(final ReconfigureAction action) {
        action.setTask(this);
        actions.add(action);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addPropertyfile(final UpdatePropertyFileReconfigureAction action) {
        add(action);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addXml(final UpdateXmlFileReconfigureAction action) {
        add(action);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addRegexreplace(final RegexReplaceAction action) {
        add(action);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addInsert(final InsertFileAction action) {
        add(action);
    }

    @Override
    public void execute() throws BuildException {
        dataBufferManager = new DataBufferManager();
        try {
            if (xmlSource != null) {
                xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlSource);
            }
            if (file == null) {
                throw new BuildException("file not specified");
            }
            setupStandardSuffixMappings();
            final CopyStreams copyStreams = prepareToProcess();
            process(copyStreams);
            copyStreams.close();
            if (updateSelf) {
                ((DataBuffer) dest).writeTo(dest);
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
        log("Reconfigured " + file.getPath() + " as " + destFile.getPath(), Project.MSG_INFO);
        xpathCache.clear();
        dataBufferManager.clear();
    }

    public String getXmlValue(final String xpath) {
        String value = xpathCache.get(xpath);
        if (value == null) {
            if (xmlDoc == null) {
                throw new BuildException("xmlValue can only be used if an xmlSource has been specified");
            }
            try {
                final XPathExpression expression = xPath.compile(xpath);
                value = (String) expression.evaluate(xmlDoc, XPathConstants.STRING);
                xpathCache.put(xpath, value);
            } catch (XPathExpressionException e) {
                throw new BuildException("Invalid XPath expression: " + xpath, e);
            }
        }
        return value;
    }

    protected CopyStreams prepareToProcess() throws IOException {
        updateSelf = destFile == null;
        final FileInputStream fis = new FileInputStream(file);
        if (updateSelf) {
            dest = dataBufferManager.createBuffer();
        } else {
            dest = new FileOutputStream(destFile);
        }
        return new CopyStreams(this, file.getName(), fis, dest, actions, xmlDoc);
    }

    protected void setupStandardSuffixMappings() {
        suffixToArchTypes.put("zip", FileType.ZIP);
        suffixToArchTypes.put("jar", FileType.JAR);
        suffixToArchTypes.put("ear", FileType.JAR);
        suffixToArchTypes.put("war", FileType.JAR);
    }

    private void process(final CopyStreams copyStreams)
            throws IOException {
        boolean fail = false;
        // iterate through zip entries
        for (ZipArchiveEntry zipEntry = copyStreams.getSourceStream().getNextZipEntry(); zipEntry != null; zipEntry = copyStreams.getSourceStream().getNextZipEntry()) {
            log("Processing " + copyStreams.getPath() + "/" + zipEntry.getName(), Project.MSG_VERBOSE);
            copyStreams.preCopy(zipEntry);
            final LinkedList<ReconfigureAction> containerActions = getActions(zipEntry.getName(), copyStreams.getActions(), true);
            final LinkedList<ReconfigureAction> fileActions = getActions(zipEntry.getName(), copyStreams.getActions(), false);
            if (containerActions != null && (getFileType(zipEntry.getName()) == FileType.JAR
                    || getFileType(zipEntry.getName()) == FileType.JAR)) {
                log("Archive entry found which has container actions, processing archive: " + copyStreams.getPath() + "/" + zipEntry.getName(), Project.MSG_VERBOSE);
                // there is container actions and this is an archive, so let's process those
                final CopyStreams subArchiveCopyStream = new CopyStreams(this, zipEntry.getName(),
                        copyStreams.getSourceStream(), copyStreams.getDestBufferStream(), containerActions, xmlDoc);
                process(subArchiveCopyStream);
            } else if (fileActions != null && !fileActions.isEmpty()) {
                // there's file actions, let's process them
                log("File entry with file actions, processing: " + copyStreams.getPath() + "/" + zipEntry.getName(), Project.MSG_VERBOSE);
                if (fileActions.size() > 1) {
                    // there's multiple actions, so we're going to have to make the actions work on the buffers
                    DataBuffer preProcessBuffer = dataBufferManager.createBuffer();
                    DataBuffer postProcessBuffer = dataBufferManager.createBuffer();
                    IOUtils.copy(copyStreams.getSourceStream(), preProcessBuffer);
                    preProcessBuffer.close();
                    for (final ReconfigureAction fileAction : fileActions) {
                        if (fileAction.isStrict() == null) {
                            fileAction.setStrict(strict);
                        }
                        fileAction.execute(preProcessBuffer.getDataInputStream(), postProcessBuffer);
                        preProcessBuffer.clear();
                        postProcessBuffer.close();
                        preProcessBuffer = postProcessBuffer;
                        postProcessBuffer = dataBufferManager.createBuffer();
                    }
                    postProcessBuffer.clear();

                    preProcessBuffer.writeTo(copyStreams.getDestBufferStream());
                } else {
                    // only one action, we can work directly on the stream
                    final ReconfigureAction reconfigureAction = fileActions.iterator().next();
                    if (reconfigureAction.isStrict() == null) {
                        reconfigureAction.setStrict(strict);
                    }
                    reconfigureAction.execute(copyStreams.getSourceStream(), copyStreams.getDestBufferStream());
                }
            } else {
                copyStreams.copy(zipEntry);
            }
            copyStreams.postCopy();
        }
        final boolean handleNonExitentDuplicates = true;
//        while(handleNonExitentDuplicates) {
//            final Iterator<ReconfigureAction> it = copyStreams.getActions().iterator();
//            final HashSet<String> paths = new HashSet<String>();
//            final LinkedList<ReconfigureAction> duplicatedPathActions = new LinkedList<ReconfigureAction>();
//            while (it.hasNext()) {
//                ReconfigureAction action = it.next();
//                if( duplicatedPathActions.isEmpty() && paths.contains(action.getPath() ) ) {
//                    duplicatedPathActions.add(action);
//                } else {
//                    paths.add(action.getPath());
//                }
//            }
//        }
        for (final ReconfigureAction action : copyStreams.getActions()) {
            copyStreams.preCopy(action.getPath());
            if (action.isNonExistingSourceAllowed()) {
                action.execute(null, copyStreams.getDestBufferStream());
            } else {
                log(action.getPath() + " does not exist, unable to update", Project.MSG_ERR);
                fail = true;
            }
            copyStreams.postCopy();
        }
        copyStreams.finish();
        if (fail) {
            throw new BuildException("Reconfiguration failed, see logs for more details");
        }
    }

    /**
     * Get all actions that affect the specified path, removing them from the list.
     * File actions are actions that directly affect the specified path. Container actions are actions that affect
     * files inside an nested archive, those files have the path of the archive removed, so that that become relative to the
     * nested archive.
     *
     * @param path               Path to check for affecting actions.
     * @param reconfigureActions List of actions.
     * @param container          Boolean indicating if we should return container actions, or file actions.
     * @return List of Actions that affect the specified path
     */
    private LinkedList<ReconfigureAction> getActions(final String path, final List<ReconfigureAction> reconfigureActions,
                                                     final boolean container) {
        LinkedList<ReconfigureAction> match = null;
        final Iterator<ReconfigureAction> it = reconfigureActions.iterator();
        while (it.hasNext()) {
            final ReconfigureAction action = it.next();
            final String actionPath = action.getPath();
            if (container ? actionPath.length() > path.length() && actionPath.charAt(path.length()) == '/'
                    && actionPath.substring(0, path.length()).equals(path) : actionPath.equals(path)) {
                if (match == null) {
                    match = new LinkedList<ReconfigureAction>();
                }
                it.remove();
                if (container) {
                    action.setPath(actionPath.substring(path.length() + 1));
                }
                match.add(action);
            }
        }
        return match;
    }

    private FileType getFileType(final String name) {
        final int dotIdx = name.lastIndexOf('.');
        final int slashIdx = name.lastIndexOf('/');
        if (dotIdx == -1 || (slashIdx != -1 && slashIdx > dotIdx)) {
            return FileType.OTHER;
        }
        final FileType type = suffixToArchTypes.get(name.substring(dotIdx + 1).toLowerCase());
        if (type != null) {
            return type;
        } else {
            return FileType.OTHER;
        }
    }

    public static void validateXmlValueArgs(final String value, final String xmlValue) {
        if (value != null && xmlValue != null) {
            throw new BuildException("both value and xmlValue cannot be specified at the same time");
        } else if (value == null && xmlValue == null) {
            throw new BuildException("neither value nor xmlValue are specified");
        }
    }

    public enum FileType {
        OTHER, ZIP, JAR
    }
}
