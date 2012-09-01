/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.util;

import java.io.IOException;
import java.util.ArrayList;

public class DataBufferManager {
    private ArrayList<DataBuffer> buffers = new ArrayList<DataBuffer>();

    public DataBufferManager() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                clear();
            }
        });
    }

    public void clear() {
        for (DataBuffer buffer : buffers) {
            if (buffer.getStatus() != DataBuffer.Status.CLEARED) {
                try {
                    buffer.clear();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    public DataBuffer createBuffer() {
        return createBuffer(10240);
    }

    public DataBuffer createBuffer(int memSize) {
        DataBuffer buffer = new DataBuffer(memSize);
        buffers.add(buffer);
        return buffer;
    }
}
