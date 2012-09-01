/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.reconfigure;

import com.kloudtek.buildmagic.tools.util.DataBuffer;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;

class CopyStreams {
    private ZipArchiveInputStream sourceStream;
    private ZipArchiveOutputStream destinationStream;
    private DataBuffer destBufferStream;
    private List<ReconfigureAction> actions;
    private ZipArchiveEntry zipEntry;
    private int compressionMethod;
    private final Document xmlDoc;
    private ReconfigureTask reconfigureTask;
    private State state;
    private String path;

    CopyStreams(final ReconfigureTask reconfigureTask, String path, InputStream ois, OutputStream oos, List<ReconfigureAction> actions,
                final Document xmlDoc) throws IOException {
        this.reconfigureTask = reconfigureTask;
        this.xmlDoc = xmlDoc;
        final boolean isJar = path.toLowerCase().endsWith(".jar");
        if (isJar) {
            sourceStream = new JarArchiveInputStream(ois);
            destinationStream = new ZipArchiveOutputStream(oos);
        } else {
            sourceStream = new ZipArchiveInputStream(ois);
            destinationStream = new ZipArchiveOutputStream(oos);
        }
        this.actions = actions;
        this.path = path;
    }

    /**
     * Copy a source entry as-is into the current zipEntry
     *
     * @param zipEntry ZipEntry to copy
     * @throws java.io.IOException If an I/O error occurs
     */
    public void copy(final ZipEntry zipEntry) throws IOException {
        if (!zipEntry.isDirectory()) {
            IOUtils.copy(getSourceStream(), getDestBufferStream());
        }
    }

    /**
     * Create zip entry and copy data from destBufferStream to destinationStream
     *
     * @throws IOException If an I/O error occurs
     */
    public void postCopy() throws IOException {
        destBufferStream.close();
        if (destBufferStream.getSize() > 0) {
            zipEntry.setSize(destBufferStream.getSize());
            zipEntry.setCrc(destBufferStream.getCrc32());
            if (compressionMethod != -1) {
                zipEntry.setMethod(compressionMethod);
            }
        }
        destinationStream.putArchiveEntry(zipEntry);
        if (destBufferStream.getSize() > 0) {
            destBufferStream.writeTo(destinationStream);
        }
        destBufferStream.clear();
        destinationStream.closeArchiveEntry();
    }

    public void preCopy(final ZipArchiveEntry origEntry) throws IOException {
        zipEntry = new ZipArchiveEntry(origEntry.getName());
        zipEntry.setComment(origEntry.getComment());
        compressionMethod = origEntry.getMethod();
        zipEntry.setTime(origEntry.getTime());
        destBufferStream = reconfigureTask.getDataBufferManager().createBuffer();
    }

    public void preCopy(String path) throws IOException {
        ZipArchiveEntry newEntry = new ZipArchiveEntry(path);
        compressionMethod = ZipEntry.DEFLATED;
        newEntry.setTime(System.currentTimeMillis());
        preCopy(newEntry);
    }

    public void close() throws IOException {
        destBufferStream.close();
        sourceStream.close();
    }

    public void finish() throws IOException {
        destinationStream.finish();
    }

    public Document getXmlDoc() {
        return xmlDoc;
    }

    public ZipArchiveInputStream getSourceStream() {
        return sourceStream;
    }

    public DataBuffer getDestBufferStream() {
        return destBufferStream;
    }

    public List<ReconfigureAction> getActions() {
        return actions;
    }

    public String getPath() {
        return path;
    }

    public enum State {
        NEW, READY, CLOSED
    }
}
