package com.rental.dao;

import com.rental.model.User;
import com.rental.util.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data access for the users table (staff logins).
 */
public class UserDAO {

    /**
     * Returns the User when username and password match, otherwise null.
     */
    public User login(String username, String password) throws SQLException {
        String sql = "select user_id, username, password, role from users where username=? and password=?";

        // try-with-resources closes the connection and statement automatically
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                // MySQL compares text ignoring case, so compare the password again in Java
                if (rs.next() && rs.getString("password").equals(password)) {
                    return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("role"));
                }
            }
        }
        return null;
    }
}
