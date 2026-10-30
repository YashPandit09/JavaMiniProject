package com.vehiclerental;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles user login and logout.
 * <ul>
 *   <li>GET  /login  — forwards to login.jsp</li>
 *   <li>POST /login  — verifies credentials, creates session</li>
 *   <li>GET  /logout — invalidates session, redirects to login</li>
 * </ul>
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ---- Logout ----
        if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login?loggedout=true");
            return;
        }

        // ---- Show login page ----
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email    = trim(request.getParameter("email"));
        String password = request.getParameter("password");

        if (email.isEmpty() || password == null || password.isEmpty()) {
            setError(request, response, "Email and password are required.");
            return;
        }

        String sql = "SELECT id, full_name, email, password_hash, role FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    if (PasswordUtil.verifyPassword(password, storedHash)) {
                        // --- Authentication succeeded ---

                        // Session fixation protection: invalidate old session, create new one
                        HttpSession oldSession = request.getSession(false);
                        if (oldSession != null) {
                            oldSession.invalidate();
                        }
                        HttpSession session = request.getSession(true);

                        session.setAttribute("userId",   rs.getInt("id"));
                        session.setAttribute("userName", rs.getString("full_name"));
                        session.setAttribute("userEmail", rs.getString("email"));
                        session.setAttribute("userRole", rs.getString("role"));
                        session.setMaxInactiveInterval(30 * 60); // 30 minutes

                        response.sendRedirect(request.getContextPath() + "/vehicles");
                        return;
                    }
                }
            }

            // Either the email was not found or the password didn't match
            setError(request, response, "Invalid email or password.");

        } catch (SQLException e) {
            log("Login failed", e);
            setError(request, response, "A system error occurred. Please try again later.");
        }
    }

    // ---- helpers -----------------------------------------------------------

    private void setError(HttpServletRequest request, HttpServletResponse response,
                          String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("email", request.getParameter("email"));
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
