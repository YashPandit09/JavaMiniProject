package com.rental.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The one place that opens a JDBC connection to rental_db.
 * Every DAO calls DBConnect.dbCon().
 *
 * >>> Change USER and PASSWORD below to your own MySQL login. <<<
 */
public class DBConnect {

    private static final String URL = "jdbc:mysql://localhost:3306/rental_db";
    private static final String USER = "root";
    private static final String PASSWORD = "yashloq";

    public static Connection dbCon() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");      // load the MySQL driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found - add mysql-connector-j to the project");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
