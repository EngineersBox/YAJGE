package com.engineersbox.yajge.resources;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    private static final Logger LOGGER = LogManager.getLogger(ResourceLoader.class);

    private ResourceLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static String load(final String fileName) throws Exception {
        final ClassLoader classLoader = ResourceLoader.class.getClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException(String.format(
                        "Could not find resource: %s",
                        fileName
                ));
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            LOGGER.fatal(e);
        }
        return null;
    }
}
