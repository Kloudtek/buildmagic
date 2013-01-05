/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.util;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Helper class to assist with archives
 */
public class ArchiveHelper {
    public static TarArchiveEntry createTarArchiveEntry(final String filename, final String ownerUser, final String ownerGroup, Integer filemode, Byte linkFlag) {
        TarArchiveEntry dataEntry;
        if (linkFlag != null) {
            dataEntry = new TarArchiveEntry(filename, linkFlag);
        } else {
            dataEntry = new TarArchiveEntry(filename);
        }
        if (filemode != null) {
            dataEntry.setMode(filemode);
        }
        if (ownerUser != null) {
            dataEntry.setUserName(ownerUser);
        }
        if (ownerGroup != null) {
            dataEntry.setGroupName(ownerGroup);
        }
        return dataEntry;
    }

    public static void write(ArArchiveOutputStream archiveStream, byte[] data, String filename) throws IOException {
        final ArArchiveEntry entry = new ArArchiveEntry(filename, data.length);
        archiveStream.putArchiveEntry(entry);
        archiveStream.write(data);
        archiveStream.closeArchiveEntry();
    }

    public static void write(ArArchiveOutputStream archiveStream, DataBuffer dataBuffer, String filename) throws IOException {
        final ArArchiveEntry entry = new ArArchiveEntry(filename, dataBuffer.getSize());
        archiveStream.putArchiveEntry(entry);
        dataBuffer.writeTo(archiveStream);
        archiveStream.closeArchiveEntry();
    }

    public static void write(TarArchiveOutputStream archiveStream, byte[] data, String filename) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename, null, null, null, null);
        dataEntry.setSize(data.length);
        archiveStream.putArchiveEntry(dataEntry);
        archiveStream.write(data);
        archiveStream.closeArchiveEntry();
    }

    public static void write(TarArchiveOutputStream archiveStream, DataBuffer dataBuffer, String filename) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename, null, null, null, null);
        dataEntry.setSize(dataBuffer.getSize());
        archiveStream.putArchiveEntry(dataEntry);
        dataBuffer.writeTo(archiveStream);
        archiveStream.closeArchiveEntry();
    }

    public static void write(TarArchiveOutputStream archiveStream, InputStream data, long size, String filename,
                             final String ownerUser, final String ownerGroup, final Integer filemode) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename, ownerUser, ownerGroup, filemode, null);
        dataEntry.setSize(size);
        if (filemode != null) {
            dataEntry.setMode(filemode);
        }
        archiveStream.putArchiveEntry(dataEntry);
        IOUtils.copy(data, archiveStream);
        archiveStream.closeArchiveEntry();
    }

    public static void createDir(TarArchiveOutputStream data, String filename, final String ownerUser, final String ownerGroup, Integer filemode) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename + "/", ownerUser, ownerGroup, filemode, TarConstants.LF_DIR);
        data.putArchiveEntry(dataEntry);
        data.closeArchiveEntry();
    }

    public static void createSymlink(TarArchiveOutputStream archiveOutputStream, String filename, String target) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename, null, null, null, TarConstants.LF_SYMLINK);
        dataEntry.setLinkName(target);
        dataEntry.setSize(0);
        archiveOutputStream.putArchiveEntry(dataEntry);
        archiveOutputStream.closeArchiveEntry();
    }

    public static void createParentDirs(HashSet<String> dataParentDirs, TarArchiveOutputStream data, String filename
            , final String ownerUser, final String ownerGroup, final Integer filemode) throws IOException {
        StringTokenizer tok = new StringTokenizer(filename, File.separator);
        StringBuilder parentDir = new StringBuilder();
        while (tok.hasMoreTokens()) {
            parentDir.append(tok.nextToken());
            parentDir.append(File.separator);
            if (!tok.hasMoreTokens()) {
                // last one, skip
                return;
            }
            final String dir = parentDir.toString();
            if (!dataParentDirs.contains(dir)) {
                createDir(data, dir, ownerUser, ownerGroup, filemode);
                dataParentDirs.add(dir);
            }
        }
    }
}
