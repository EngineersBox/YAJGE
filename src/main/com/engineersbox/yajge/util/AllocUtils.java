package com.engineersbox.yajge.util;

import org.lwjgl.system.MemoryUtil;

public class AllocUtils {

    public static void freeAll(final java.nio.Buffer... buffers) {
        for (final java.nio.Buffer bufferPtr : buffers) {
            if (bufferPtr != null) {
                MemoryUtil.memFree(bufferPtr);
            }
        }
    }

}
