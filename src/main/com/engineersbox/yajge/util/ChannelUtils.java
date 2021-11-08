package com.engineersbox.yajge.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ChannelUtils {

    private static final Logger LOGGER = LogManager.getLogger(ChannelUtils.class);

    private ChannelUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static int readAll(final ReadableByteChannel channel,
                              final ByteBuffer buffer) {
        if (channel == null || !channel.isOpen()) {
            return -1;
        }
        try {
            int readCount = 0;
            while(channel.read(buffer) != -1) {
                readCount++;
            }
            return readCount;
        } catch (final IOException e) {
            LOGGER.error(e);
        }
        return -1;
    }

}
