package com.rental.servlet;

import com.rental.dao.BookingDAO;
import com.rental.model.Payment;
import com.rental.model.User;
import com.rental.util.Layout;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reports: revenue by month and by vehicle type, top vehicles,
 * booking status counts and payments between two dates.
 */
@WebServlet("/ReportSer")
public class ReportSer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;
        }
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        Layout.top(out, "Reports", u, "reports");

        // payments date range: default = this month
        Date from = Layout.toDate(req.getParameter("from"));
        Date to = Layout.toDate(req.getParameter("to"));
        if (from == null) {
            from = Date.valueOf(LocalDate.now().withDayOfMonth(1));
        }
        if (to == null) {
            to = Date.valueOf(LocalDate.now());
        }

        try {
            BookingDAO dao = new BookingDAO();
            LinkedHashMap<String, Integer> counts = dao.getBookingCountByStatus();

            out.println("<div class='stats'>");
            stat(out, "Total revenue (all time)", Layout.money(dao.getTotalRevenue()), true);
            stat(out, "Active bookings", String.valueOf(counts.get("BOOKED")), false);
            stat(out, "Completed rentals", String.valueOf(counts.get("RETURNED")), false);
            stat(out, "Cancelled bookings", String.valueOf(counts.get("CANCELLED")), false);
            out.println("</div>");

            out.println("<div class='row'>");
            barTable(out, "Revenue by month (last 6 months)", "Month", dao.getRevenueByMonth());
            barTable(out, "Revenue by vehicle type", "Type", dao.getRevenueByType());
            out.println("</div>");

            // ---- top vehicles ----
            out.println("<div class='card'><h2>Most rented vehicles</h2><div class='table-wrap'><table>");
            out.println("<tr><th>#</th><th>Reg. no.</th><th>Make / model</th><th class='num'>Rentals</th>"
                    + "<th class='num'>Revenue</th></tr>");
            int rank = 1;
            for (String[] r : dao.getTopVehicles(5)) {
                out.println("<tr><td>" + rank++ + "</td><td><b>" + Layout.esc(r[0]) + "</b></td><td>"
                        + Layout.esc(r[1]) + "</td><td class='num'>" + r[2] + "</td><td class='num'>"
                        + Layout.money(Double.parseDouble(r[3])) + "</td></tr>");
            }
            out.println("</table></div></div>");

            // ---- payments in a date range ----
            ArrayList<Payment> payments = dao.getPayments(from, to);
            double total = payments.stream().mapToDouble(p -> p.getAmount()).sum();

            out.println("<div class='card'><h2>Payments received</h2>");
            out.println("<form class='inline' action='ReportSer' method='get'>"
                    + "<div><label for='from'>From</label><input type='date' id='from' name='from' value='" + from + "'></div>"
                    + "<div><label for='to'>To</label><input type='date' id='to' name='to' value='" + to + "'></div>"
                    + "<button>Show</button></form><br>");
            out.println("<div class='table-wrap'><table>");
            out.println("<tr><th>Payment</th><th>Paid on</th><th>Booking</th><th>Customer</th><th>Vehicle</th>"
                    + "<th class='num'>Amount</th></tr>");
            if (payments.isEmpty()) {
                out.println("<tr><td colspan='6' class='empty'>No payments in this period.</td></tr>");
            }
            for (Payment p : payments) {
                out.println("<tr><td>#" + p.getPaymentId() + "</td><td>" + Layout.date(p.getPaidOn()) + "</td>"
                        + "<td>#" + p.getBookingId() + "</td><td>" + Layout.esc(p.getCustomerName()) + "</td>"
                        + "<td>" + Layout.esc(p.getRegNo()) + "</td>"
                        + "<td class='num'>" + Layout.money(p.getAmount()) + "</td></tr>");
            }
            out.println("<tr><td colspan='5'><b>Total (" + payments.size() + " payment(s))</b></td>"
                    + "<td class='num'><b>" + Layout.money(total) + "</b></td></tr>");
            out.println("</table></div></div>");

        } catch (SQLException e) {
            Layout.message(out, null, "Database error: " + e.getMessage());
        }
        Layout.bottom(out);
    }

    private void stat(PrintWriter out, String label, String value, boolean hero) {
        out.println("<div class='stat" + (hero ? " hero" : "") + "'><div class='label'>" + label
                + "</div><div class='value'>" + value + "</div></div>");
    }

    /** A table with one bar per row; bar length is the value compared to the largest value. */
    private void barTable(PrintWriter out, String title, String keyName, LinkedHashMap<String, Double> data) {
        double max = 0;
        for (double v : data.values()) {
            max = Math.max(max, v);
        }
        out.println("<div class='card'><h2>" + title + "</h2><div class='table-wrap'><table>");
        out.println("<tr><th>" + keyName + "</th><th class='bar-cell'></th><th class='num'>Revenue</th></tr>");
        if (data.isEmpty()) {
            out.println("<tr><td colspan='3' class='empty'>No payments yet.</td></tr>");
        }
        for (Map.Entry<String, Double> e : data.entrySet()) {
            String bar = "";
            if (e.getValue() > 0) {
                long pct = Math.round(e.getValue() * 100 / max);
                bar = "<div class='bar' style='width:" + pct + "%' title='"
                        + Layout.esc(e.getKey()) + ": " + Layout.money(e.getValue()) + "'></div>";
            }
            out.println("<tr><td>" + Layout.esc(e.getKey()) + "</td><td class='bar-cell'>" + bar + "</td>"
                    + "<td class='num'>" + Layout.money(e.getValue()) + "</td></tr>");
        }
        out.println("</table></div></div>");
    }
}
