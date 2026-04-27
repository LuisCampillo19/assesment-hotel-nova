package com.luiscampillo.hotelnova.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Centralised access to the two configuration files:
 *   - app.properties        (business rules, view choice, paths)
 *   - database.properties   (JDBC connection details)
 *
 * Both files live in src/main/resources and are loaded once at startup
 * via the classpath. If a required key is missing the application fails
 * fast at construction time instead of crashing later inside a service.
 *
 * Singleton because every layer needs read-only access to the same values.
 */
public final class AppConfig {

    private static final String APP_PROPERTIES_FILE = "app.properties";
    private static final String DB_PROPERTIES_FILE  = "database.properties";

    private static AppConfig instance;

    private final Properties appProps;
    private final Properties dbProps;

    private AppConfig() {
        this.appProps = loadProperties(APP_PROPERTIES_FILE);
        this.dbProps  = loadProperties(DB_PROPERTIES_FILE);
    }

    /**
     * Thread-safe lazy initialization (double-checked locking).
     * In practice the config is read at startup so contention is unlikely,
     * but the locking keeps the contract correct.
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    // -- App properties -----------------------------------------------------

    public String getAppName() {
        return required(appProps, "app.name");
    }

    public String getAppVersion() {
        return required(appProps, "app.version");
    }

    /** "console" or "swing" - controls which View implementation is wired. */
    public String getViewType() {
        return required(appProps, "view.type").toLowerCase();
    }

    /**
     * IVA rate as a decimal (0.19 = 19%). Returned as BigDecimal so the
     * service layer can multiply against monetary amounts without losing
     * precision to floating-point arithmetic.
     */
    public BigDecimal getIva() {
        return new BigDecimal(required(appProps, "business.iva"));
    }

    public String getReportsDirectory() {
        return required(appProps, "reports.directory");
    }

    public String getLogFile() {
        return required(appProps, "logging.file");
    }

    // -- Database properties ------------------------------------------------

    public String getDbUrl()      { return required(dbProps, "db.url"); }
    public String getDbUser()     { return required(dbProps, "db.user"); }
    public String getDbPassword() { return required(dbProps, "db.password"); }
    public String getDbDriver()   { return required(dbProps, "db.driver"); }

    // -- Internals ----------------------------------------------------------

    private Properties loadProperties(String filename) {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Configuration file not found on classpath: " + filename);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read configuration file: " + filename, e);
        }
        return props;
    }

    private String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required configuration key is missing or empty: " + key);
        }
        return value.trim();
    }
}
