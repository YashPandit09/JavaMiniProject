package com.vehiclerental;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles user registration.
 * <ul>
 *   <li>GET  /register — forwards to register.jsp</li>
 *   <li>POST /register — validates input, checks for duplicate email,
 *       hashes the password with PBKDF2, inserts a new user, and redirects
 *       to login.jsp on success.</li>
 * </ul>
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Collect form fields
        String fullName       = trim(request.getParameter("fullName"));
        String email          = trim(request.getParameter("email"));
        String phone          = trim(request.getParameter("phone"));
        String password       = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // ---------- server-side validation ----------

        if (isBlank(fullName) || isBlank(email) || isBlank(phone)
                || isBlank(password) || isBlank(confirmPassword)) {
            setError(request, response, "All fields are required.");
            return;
        }

        if (fullName.length() > 100) {
            setError(request, response, "Full name must be 100 characters or fewer.");
            return;
        }

        // Basic email format check
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            setError(request, response, "Please enter a valid email address.");
            return;
        }

        if (!phone.matches("^[0-9]{10,15}$")) {
            setError(request, response, "Phone number must be 10–15 digits.");
            return;
        }

        if (password.length() < 6) {
            setError(request, response, "Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            setError(request, response, "Passwords do not match.");
            return;
        }

        // ---------- database operations ----------

        try (Connection conn = DBConnection.getConnection()) {

            // Check for duplicate email
            String checkSql = "SELECT id FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        setError(request, response, "An account with this email already exists.");
                        return;
                    }
                }
            }

            // Hash the password
            String passwordHash = PasswordUtil.hashPassword(password);

            // Insert the new user (role defaults to CUSTOMER)
            String insertSql = "INSERT INTO users (full_name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, 'CUSTOMER')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, fullName);
                insertStmt.setString(2, email);
                insertStmt.setString(3, passwordHash);
                insertStmt.setString(4, phone);
                insertStmt.executeUpdate();
            }

            // Redirect to login with success message
            response.sendRedirect(request.getContextPath() + "/login?registered=true");

        } catch (SQLException e) {
            log("Registration failed", e);
            setError(request, response, "A system error occurred. Please try again later.");
        }
    }

    // ---- helpers -----------------------------------------------------------

    private void setError(HttpServletRequest request, HttpServletResponse response,
                          String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        // Preserve entered values so the user doesn't retype everything
        request.setAttribute("fullName", request.getParameter("fullName"));
        request.setAttribute("email",    request.getParameter("email"));
        request.setAttribute("phone",    request.getParameter("phone"));
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
