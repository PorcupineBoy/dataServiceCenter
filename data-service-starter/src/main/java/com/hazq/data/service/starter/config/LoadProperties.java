package com.hazq.data.service.starter.config;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author fengzheng
 * @version 1.0
 * @date 2021/4/8 15:14
 */

@Slf4j
public class LoadProperties {
    private static final String SQL_STATISTICS_PROPERTIES = "es.properties";
    //private static final Logger log = LoggerFactory.getLogger(LoadProperties.class);
    private static Properties properties = new Properties();

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(String filePath) throws IOException {

        InputStream resourceAsStream = null;
        try {
            //log.info("configPath is {}", filePath);
            resourceAsStream = new BufferedInputStream(new FileInputStream(filePath));
            properties.load(resourceAsStream);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

    }

    public static void setProperties() throws IOException {

        InputStream resourceAsStream = null;
        resourceAsStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(SQL_STATISTICS_PROPERTIES);

        properties.load(resourceAsStream);

    }
}
