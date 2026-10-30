package com.vehiclerental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC connection factory for the Vehicle Rental System.
 *
 * <h3>Configuration</h3>
 * The database URL, username, and password are read from environment variables:
 * <ul>
 *   <li>{@code VEHICLE_RENTAL_DB_URL}  — JDBC URL (default: {@code jdbc:mysql://localhost:3306/vehicle_rental_db})</li>
 *   <li>{@code VEHICLE_RENTAL_DB_USER} — MySQL user (default: {@code root})</li>
 *   <li>{@code VEHICLE_RENTAL_DB_PASS} — MySQL password (default: empty string)</li>
 * </ul>
 *
 * <h3>Setting environment variables</h3>
 * <strong>Option A — System-wide (Windows):</strong>
 * <pre>
 *   setx VEHICLE_RENTAL_DB_URL  "jdbc:mysql://localhost:3306/vehicle_rental_db"
 *   setx VEHICLE_RENTAL_DB_USER "root"
 *   setx VEHICLE_RENTAL_DB_PASS "yourpassword"
 * </pre>
 * <strong>Option B — Tomcat startup script ({@code catalina.bat} or {@code setenv.bat}):</strong>
 * <pre>
 *   set VEHICLE_RENTAL_DB_URL=jdbc:mysql://localhost:3306/vehicle_rental_db
 *   set VEHICLE_RENTAL_DB_USER=root
 *   set VEHICLE_RENTAL_DB_PASS=yourpassword
 * </pre>
 * <strong>Option C — Edit the defaults below (quick demo only; do not commit real passwords).</strong>
 */
public class DBConnection {

    // -- Defaults used when environment variables are not set ----------------
    private static final String DEFAULT_URL  = "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";   // <-- change for quick demo

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        // Load the MySQL JDBC driver class
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found. "
                    + "Ensure mysql-connector-j is in WEB-INF/lib.", e);
        }

        // Read configuration from environment variables, falling back to defaults
        String envUrl  = System.getenv("VEHICLE_RENTAL_DB_URL");
        String envUser = System.getenv("VEHICLE_RENTAL_DB_USER");
        String envPass = System.getenv("VEHICLE_RENTAL_DB_PASS");

        URL  = (envUrl  != null && !envUrl.isBlank())  ? envUrl  : DEFAULT_URL;
        USER = (envUser != null && !envUser.isBlank()) ? envUser : DEFAULT_USER;
        PASS = (envPass != null)                       ? envPass : DEFAULT_PASS;
    }

    /**
     * Returns a new JDBC connection to the vehicle_rental_db database.
     * Callers are responsible for closing this connection — use try-with-resources.
     *
     * @return a live {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Prevent instantiation
    private DBConnection() { }
}
