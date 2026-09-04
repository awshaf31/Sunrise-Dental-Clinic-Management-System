package lk.icbt.clinic.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hands out JDBC connections to the MySQL database.
 * <p>
 * There is no connection pool here deliberately -- this is a small
 * coursework system, and a hand-rolled pool would be exactly the kind of
 * infrastructure code a framework (like Spring's HikariCP auto-configuration)
 * normally provides for you. A new {@link Connection} per request is
 * adequate at this scale and keeps the persistence layer honest about what
 * "no framework" actually costs.
 * <p>
 * Configuration is read from an environment variable, or from
 * {@code local.properties} if that is not set -- see {@link DbConfig} for
 * why both exist. Either way, no credential is ever committed to source
 * control:
 * <pre>
 *   DB_URL       jdbc:mysql://localhost:3306/sunrise_dental
 *   DB_USERNAME  clinic_app
 *   DB_PASSWORD  (the password set in db/00-setup.sql)
 * </pre>
 */
public final class DbConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/sunrise_dental"
                    + "?useSSL=false&serverTimezone=Asia/Colombo"
                    // MySQL 8+/9's caching_sha2_password auth plugin refuses
                    // to send the password over a non-SSL connection unless
                    // this is set -- without it, every connection attempt
                    // fails with "Public Key Retrieval is not allowed".
                    + "&allowPublicKeyRetrieval=true";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found on the classpath: " + e.getMessage());
        }
    }

    private DbConnection() {
    }

    public static Connection get() throws SQLException {
        String url = orDefault(DbConfig.get("DB_URL"), DEFAULT_URL);
        String user = orDefault(DbConfig.get("DB_USERNAME"), "clinic_app");
        String password = DbConfig.get("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new SQLException(
                    "DB_PASSWORD is not set. Either export it as an environment variable, "
                            + "or create local.properties next to pom.xml -- see DbConfig.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
