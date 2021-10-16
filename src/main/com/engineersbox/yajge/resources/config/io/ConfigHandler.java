package com.engineersbox.yajge.resources.config.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

public class ConfigHandler {

    private static final Logger LOGGER = LogManager.getLogger(ConfigHandler.class);

    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(final String fileName,
                                  final Class<T> configClass) {
        final ClassLoader classLoader = ConfigHandler.class.getClassLoader();
        Object unmarshalledObj = null;
        try (final InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            final JAXBContext context = JAXBContext.newInstance(configClass);
            if (inputStream == null) {
                throw new RuntimeException(String.format(
                        "Could not find resource: %s",
                        fileName
                )); // TODO: Implement an exception for this
            }
            unmarshalledObj = context.createUnmarshaller().unmarshal(inputStream);
        } catch (final JAXBException | IOException e) {
            LOGGER.error(e);
        }
        if (unmarshalledObj == null) {
            return null;
        }
        if (unmarshalledObj.getClass().isAssignableFrom(configClass)) {
            return (T) unmarshalledObj;
        }
        return null;
    }

}
