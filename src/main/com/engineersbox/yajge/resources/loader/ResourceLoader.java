package com.engineersbox.yajge.resources.loader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ResourceLoader {
    private static final Logger LOGGER = LogManager.getLogger(ResourceLoader.class);

    private ResourceLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static String loadAsString(final String fileName) {
        String contents = null;
        try  {
            contents = FileUtils.readFileToString(new File(fileName), StandardCharsets.UTF_8.name());
        } catch (final IOException e) {
            LOGGER.error("Could not read file {}", fileName, e);
        }
        return contents;
    }

    public static String loadResourceAsString(final String fileName) {
        final ClassLoader classLoader = ResourceLoader.class.getClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException(String.format(
                        "Could not find resource: %s",
                        fileName
                )); // TODO: Implement an exception for this
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            LOGGER.error("Could not open resource {} as a stream", fileName, e);
        }
        return null;
    }

    public static List<String> loadAsStringLines(final String fileName) {
        final List<String> lines = new ArrayList<>();
        try (final LineIterator lineIterator = FileUtils.lineIterator(new File(fileName), StandardCharsets.UTF_8.name())) {
            while (lineIterator.hasNext()) {
                lines.add(lineIterator.nextLine());
            }
        } catch (final IOException e) {
            LOGGER.error("Could not read resource file {}", fileName, e);
        }
        return lines;
    }
}