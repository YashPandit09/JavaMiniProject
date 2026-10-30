package com.rental.servlet;

import com.rental.dao.UserDAO;
import com.rental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Login (POST from login.html) and logout (GET).
 */
@WebServlet("/AuthSer")
public class AuthSer extends HttpServlet {

    // ---------- login ----------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String id = req.getParameter("txtid");
        String pwd = req.getParameter("txtpwd");

        try {
            // users WHERE username=? AND password=?
            User u = new UserDAO().login(id, pwd);
            if (u != null) {
                req.getSession().setAttribute("user", u);     // remember who logged in
                res.sendRedirect("DashboardSer");
            } else {
                req.setAttribute("err", "Invalid username / password");
                req.setAttribute("back", "login.html");
                req.setAttribute("backText", "Try again");
                req.getRequestDispatcher("ResultSer").forward(req, res);
            }
        } catch (SQLException e) {
            req.setAttribute("err", "Database error: " + e.getMessage());
            req.setAttribute("back", "login.html");
            req.getRequestDispatcher("ResultSer").forward(req, res);
        }
    }

    // ---------- logout ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();                              // forget the user
        }
        res.sendRedirect("login.html");
    }
}
