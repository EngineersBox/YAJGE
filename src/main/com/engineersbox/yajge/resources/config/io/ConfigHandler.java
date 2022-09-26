package com.engineersbox.yajge.resources.config.io;

import com.engineersbox.yajge.resources.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public abstract class ConfigHandler {
    private static final String CONFIG_FILE_PARAMETER = "yajge.config";
    public static final Config CONFIG;

    static {
        final File configFile = new File(System.getProperty(CONFIG_FILE_PARAMETER));
        final com.typesafe.config.Config typesafeConfig = ConfigFactory.parseFile(configFile).resolve();
        CONFIG = new Config(typesafeConfig);
    }
}
