package com.luiscampillo.hotelnova.db;

import com.luiscampillo.hotelnova.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC connections to PostgreSQL (Neon).
 *
 * Each call to getConnection() returns a NEW java.sql.Connection. The caller
 * is responsible for closing it - always via try-with-resources.
 *
 * For multi-statement transactions, use getConnection(false) so the caller
 * controls commit() / rollback() explicitly. The DAO layer relies on this
 * for the reservation creation and check-out flows.
 *
 * Note: in production this class would be replaced by a connection pool
 * (HikariCP, c3p0). For an academic project DriverManager is enough.
 */
public final class ConnectionManager {

    private static ConnectionManager instance;

    private final AppConfig config;

    private ConnectionManager() {
        this.config = AppConfig.getInstance();
        loadDriver();
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionManager.class) {
                if (instance == null) {
                    instance = new ConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Returns a connection in auto-commit mode (every statement is its
     * own transaction). Use this for single-statement reads and writes.
     */
    public Connection getConnection() throws SQLException {
        return getConnection(true);
    }

    /**
     * Returns a connection with the requested auto-commit flag.
     * Pass false when the operation spans multiple SQL statements that
     * must succeed or fail together. The caller must invoke commit()
     * or rollback() before closing.
     */
    public Connection getConnection(boolean autoCommit) throws SQLException {
        Connection conn = DriverManager.getConnection(
                config.getDbUrl(),
                config.getDbUser(),
                config.getDbPassword());
        conn.setAutoCommit(autoCommit);
        return conn;
    }

    private void loadDriver() {
        try {
            Class.forName(config.getDbDriver());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "JDBC driver not found on classpath: " + config.getDbDriver()
                            + ". Check pom.xml dependencies.", e);
        }
    }
}
