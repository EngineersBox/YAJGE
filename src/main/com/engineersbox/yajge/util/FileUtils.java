package com.engineersbox.yajge.util;

import java.io.File;

public class FileUtils {

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean fileExists(final String fileName) {
        final File file = new File(fileName);
        return file.exists() && file.isFile();
    }
}
