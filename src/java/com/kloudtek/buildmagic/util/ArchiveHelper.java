/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.util;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.types.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Helper class to assist with archives
 */
public class ArchiveHelper {
    public static TarArchiveEntry createTarArchiveEntry(final String filename, final String ownerUser, final String ownerGroup, Integer filemode) {
        TarArchiveEntry dataEntry = new TarArchiveEntry(filename);
        if (ownerUser != null) {
            dataEntry.setUserName(ownerUser);
        }
        if (ownerGroup != null) {
            dataEntry.setGroupName(ownerGroup);
        }
        return dataEntry;
    }


    public static void writeDataBufferToAr(ArArchiveOutputStream debFile, DataBuffer dataBuffer, String archiveName) throws IOException {
        final ArArchiveEntry entry = new ArArchiveEntry(archiveName, dataBuffer.getSize());
        debFile.putArchiveEntry(entry);
        dataBuffer.writeTo(debFile);
        debFile.closeArchiveEntry();
        dataBuffer.clear();
    }

    public static void writeTarEntry(TarArchiveOutputStream archiveOutputStream, InputStream data, long size, String filename,
                                     final String ownerUser, final String ownerGroup, final Integer filemode) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename, ownerUser, ownerGroup, filemode);
        dataEntry.setSize(size);
        if( filemode != null ) {
            dataEntry.setMode(filemode);
        }
        archiveOutputStream.putArchiveEntry(dataEntry);
        IOUtils.copy(data, archiveOutputStream);
        archiveOutputStream.closeArchiveEntry();
    }

    public static void writeTarEntry(TarArchiveOutputStream archiveOutputStream, Resource rs, String filename,
                                     final String owner, final String ownerGroup, Integer filemode) throws IOException {
        final InputStream inputStream = rs.getInputStream();
        writeTarEntry(archiveOutputStream, inputStream, rs.getSize(), filename, owner, ownerGroup, filemode);
        inputStream.close();
    }

    public static void createTarDir(TarArchiveOutputStream data, String filename, final String ownerUser, final String ownerGroup, Integer filemode) throws IOException {
        TarArchiveEntry dataEntry = createTarArchiveEntry(filename, ownerUser, ownerGroup, filemode);
        data.putArchiveEntry(dataEntry);
        data.closeArchiveEntry();
    }

    public static void createTarParentDirs(HashSet<String> dataParentDirs, TarArchiveOutputStream data, String filename
            , final String ownerUser, final String ownerGroup, final Integer filemode ) throws IOException {
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
                createTarDir(data, dir, ownerUser, ownerGroup, filemode);
                dataParentDirs.add(dir);
            }
        }
    }
}
