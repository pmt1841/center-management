package com.codegym.service.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application-local.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Không tìm thấy file application-local.properties");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static final String SUPABASE_URL = getProperty("supabase.url");
    public static final String SUPABASE_KEY = getProperty("supabase.key");
}