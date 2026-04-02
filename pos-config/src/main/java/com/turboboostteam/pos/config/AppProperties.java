package com.turboboostteam.pos.config;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppProperties {

    private static final Logger log = LoggerFactory.getLogger(AppProperties.class);
    private static AppProperties instance;
    private FileBasedConfiguration config;

    private AppProperties() {
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                            PropertiesConfiguration.class)
                            .configure(params.fileBased()
                                    .setFileName("pos.properties"));
            config = builder.getConfiguration();
            log.info("pos.properties loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load pos.properties", e);
        }
    }

    public static AppProperties getInstance() {
        if (instance == null) instance = new AppProperties();
        return instance;
    }

    public String get(String key) {
        return config.getString(key, "");
    }

    public int getInt(String key, int defaultVal) {
        return config.getInt(key, defaultVal);
    }
}