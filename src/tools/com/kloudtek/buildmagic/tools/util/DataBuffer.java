/*
 * Copyright (c) KloudTek Ltd 2012.
 */

package com.kloudtek.buildmagic.tools.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.CRC32;

/**
 * This stream acts as a temporary buffer that will persist the data to file system if it gets too large
 */
public class DataBuffer extends OutputStream {
    private int memSize = 10000;
    private int size;
    private File tmpFile;
    private OutputStream buffer;
    private Status status = Status.READY;
    private CRC32 crc = new CRC32();

    public DataBuffer(final int memSize) {
        this.memSize = memSize;
        buffer = new ByteArrayOutputStream(memSize);
    }

    public int getSize() {
        return size;
    }

    @Override
    public void write(final int b) throws IOException {
        preWrite(size + 1);
        buffer.write(b);
        crc.update(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        preWrite(size + len);
        buffer.write(b, off, len);
        size += len;
        crc.update(b, off, len);
    }

    public void writeTo(File file) throws IOException {
        prepareToCopy();
        final File old = new File(file + File.separator + ".old");
        if (!file.renameTo(old)) {
            throw new IOException("Unable to rename " + file.getAbsolutePath() + " to " + old);
        }
        final FileOutputStream os = new FileOutputStream(file);
        writeTo(os);
        os.close();
        if (!old.delete()) {
            old.deleteOnExit();
        }
    }

    /**
     * Write buffered data to a stream (buffer must be closed but not cleared)
     *
     * @param stream Output stream
     * @throws IOException If an I/O error occurs
     */
    public void writeTo(OutputStream stream) throws IOException {
        prepareToCopy();
        if (tmpFile != null) {
            final FileInputStream is = new FileInputStream(tmpFile);
            try {
                IOUtils.copy(is, stream);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            stream.write(((ByteArrayOutputStream) buffer).toByteArray());
        }
    }

    /**
     * Get an input stream to the stored data (buffer must be closed but not cleared)
     *
     * @return InputStream to the buffered data.
     * @throws IOException If an I/O error occurs while accessing the data
     */
    public InputStream createDataInputStream() throws IOException {
        prepareToCopy();
        if (tmpFile != null) {
            return new FileInputStream(tmpFile);
        } else {
            return new ByteArrayInputStream(((ByteArrayOutputStream) buffer).toByteArray());
        }
    }

    private void prepareToCopy() throws IOException {
        if (status != Status.CLOSED) {
            throw new IOException("Stream can only be copied after being closed, and before being cleared");
        }
    }

    private void preWrite(final int newSize) throws IOException {
        if (tmpFile == null && newSize > memSize) {
            tmpFile = File.createTempFile("bmbuff", ".tmp");
            FileOutputStream tmpFileOutputStream = new FileOutputStream(tmpFile);
            tmpFileOutputStream.write(((ByteArrayOutputStream) buffer).toByteArray());
            buffer = tmpFileOutputStream;
        }
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void close() throws IOException {
        if (status == Status.READY) {
            buffer.flush();
            buffer.close();
            status = Status.CLOSED;
        }
    }

    public void clear() throws IOException {
        close();
        if (status == Status.CLOSED) {
            if (tmpFile != null) {
                if (!tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                tmpFile = null;
            }
            buffer = null;
            status = Status.CLEARED;
        }
    }

    public long getCrc32() {
        return crc.getValue();
    }

    public enum Status {
        READY, CLOSED, CLEARED
    }
}
