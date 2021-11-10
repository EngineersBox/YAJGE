package com.engineersbox.yajge.util;

import java.util.Arrays;

public class ArrayUtils {

    private ArrayUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static float[] createFilledArray(final int length,
                                            final float defaultValue) {
        final float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    public static int[] createFilledArray(final int length,
                                          final int defaultValue) {
        final int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

}
