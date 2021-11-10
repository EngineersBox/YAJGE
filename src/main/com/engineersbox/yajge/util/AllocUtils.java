package com.engineersbox.yajge.util;

import org.lwjgl.system.MemoryUtil;

public class AllocUtils {

    private AllocUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void freeAll(final java.nio.Buffer... buffers) {
        for (final java.nio.Buffer bufferPtr : buffers) {
            if (bufferPtr != null) {
                MemoryUtil.memFree(bufferPtr);
            }
        }
    }

}
