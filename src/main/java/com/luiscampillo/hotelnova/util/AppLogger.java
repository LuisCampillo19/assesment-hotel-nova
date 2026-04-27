package com.luiscampillo.hotelnova.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Initialises java.util.logging from src/main/resources/logging.properties
 * and hands out configured Logger instances.
 *
 * Usage in any class:
 *
 *   private static final Logger LOG = AppLogger.getLogger(MyClass.class);
 *
 * The static initialiser runs the first time the class is loaded, so the
 * configuration is applied before any log call from the rest of the app.
 */
public final class AppLogger {

    private static final String CONFIG_FILE = "logging.properties";

    static {
        loadConfiguration();
    }

    private AppLogger() {
        // Utility class - do not instantiate.
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    private static void loadConfiguration() {
        try (InputStream in = AppLogger.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                System.err.println("WARNING: " + CONFIG_FILE
                        + " not found on classpath. Using JVM defaults.");
                return;
            }
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            System.err.println("WARNING: failed to read " + CONFIG_FILE
                    + ": " + e.getMessage());
        }
    }
}
