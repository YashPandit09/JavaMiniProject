package com.rental.util;

import com.rental.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.format.DateTimeFormatter;

/**
 * Small helper used by every servlet:
 * login check, common page header / footer, and formatting.
 */
public class Layout {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Returns the logged-in user from the HttpSession.
     * If nobody is logged in, redirects to login.html and returns null.
     */
    public static User checkLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);            // false = do not create a new one
        User u = (session == null) ? null : (User) session.getAttribute("user");
        if (u == null) {
            res.sendRedirect("login.html");
        }
        return u;
    }

    /** Prints the top of every page: head, CSS link and navigation bar. */
    public static void top(PrintWriter out, String title, User u, String active) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head><meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<title>" + esc(title) + " | Vehicle Rental</title>");
        out.println("<link rel='stylesheet' href='css/style.css'>");
        out.println("</head><body>");

        if (u != null) {
            out.println("<header class='topbar'>");
            out.println("<a class='brand' href='DashboardSer'>Vehicle Rental <span>Management System</span></a>");
            out.println("<nav>");
            navLink(out, "DashboardSer", "Dashboard", active.equals("dashboard"));
            navLink(out, "CustomerSer", "Customers", active.equals("customers"));
            navLink(out, "VehicleSer", "Vehicles", active.equals("vehicles"));
            navLink(out, "BookSer", "New Booking", active.equals("book"));
            navLink(out, "BookSer?action=list", "Bookings", active.equals("bookings"));
            navLink(out, "ReturnSer", "Return &amp; Billing", active.equals("return"));
            navLink(out, "ReportSer", "Reports", active.equals("reports"));
            out.println("</nav>");
            out.println("<div class='who'>" + esc(u.getUsername())
                    + " <span class='role'>" + esc(u.getRole()) + "</span>"
                    + " <a href='AuthSer'>Logout</a></div>");
            out.println("</header>");
        }
        out.println("<main>");
        out.println("<h1>" + esc(title) + "</h1>");
    }

    private static void navLink(PrintWriter out, String href, String text, boolean active) {
        out.println("<a href='" + href + "'" + (active ? " class='active'" : "") + ">" + text + "</a>");
    }

    /** Prints the bottom of every page. */
    public static void bottom(PrintWriter out) {
        out.println("</main>");
        out.println("<footer>Online Vehicle Rental Management System &middot; AJP Mini Project</footer>");
        out.println("</body></html>");
    }

    /** Green success box or red error box. */
    public static void message(PrintWriter out, String msg, String err) {
        if (msg != null) {
            out.println("<div class='alert ok'>" + esc(msg) + "</div>");
        }
        if (err != null) {
            out.println("<div class='alert err'>" + esc(err) + "</div>");
        }
    }

    /** Coloured label for a status such as BOOKED or MAINTENANCE. */
    public static String badge(String status) {
        return "<span class='badge " + status.toLowerCase() + "'>" + esc(status) + "</span>";
    }

    /** &lt;option&gt; tags for a drop-down, with the selected value marked. */
    public static String options(String[] values, String selected) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            sb.append("<option value='").append(esc(v)).append("'")
              .append(v.equals(selected) ? " selected" : "")
              .append(">").append(esc(v)).append("</option>");
        }
        return sb.toString();
    }

    /** Escapes &lt; &gt; &amp; and quotes so typed text cannot break the page (stops XSS). */
    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /** 1500.0 -> "Rs. 1,500.00" */
    public static String money(double amount) {
        return String.format("Rs. %,.2f", amount);
    }

    /** 2026-10-26 -> "26 Oct 2026" */
    public static String date(Date d) {
        return d == null ? "" : d.toLocalDate().format(DATE_FMT);
    }

    /** Reads a number from the form, or returns -1 when it is missing or not a number. */
    public static int toInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    /** Reads a yyyy-mm-dd date from the form, or returns null when it is missing or wrong. */
    public static Date toDate(String s) {
        try {
            return Date.valueOf(s.trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
