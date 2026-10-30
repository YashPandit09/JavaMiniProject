package com.rental.servlet;

import com.rental.dao.BookingDAO;
import com.rental.dao.CustomerDAO;
import com.rental.dao.VehicleDAO;
import com.rental.model.Booking;
import com.rental.model.Payment;
import com.rental.model.User;
import com.rental.model.Vehicle;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Home page after login: counts, money and today's work.
 */
@WebServlet("/DashboardSer")
public class DashboardSer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;                                            // not logged in: sent to login.html
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        Layout.top(out, "Dashboard", u, "dashboard");

        try {
            BookingDAO bdao = new BookingDAO();
            ArrayList<Vehicle> vehicles = new VehicleDAO().getAllVehicles();
            ArrayList<Booking> onRent = bdao.getActiveRentals();
            ArrayList<Booking> upcoming = bdao.getUpcomingBookings();
            int customers = new CustomerDAO().getAllCustomers().size();

            // ---- collections, lambda and streams ----
            Set<Integer> onRentIds = onRent.stream()
                    .map(b -> b.getVehicleId())
                    .collect(Collectors.toSet());
            long ready = vehicles.stream()
                    .filter(v -> v.isAvailable() && !onRentIds.contains(v.getVehicleId()))
                    .count();
            long maintenance = vehicles.stream().filter(v -> !v.isAvailable()).count();
            long overdue = onRent.stream().filter(b -> b.isOverdue()).count();

            LocalDate today = LocalDate.now();
            ArrayList<Payment> monthPayments =
                    bdao.getPayments(Date.valueOf(today.withDayOfMonth(1)), Date.valueOf(today));
            double monthRevenue = monthPayments.stream().mapToDouble(p -> p.getAmount()).sum();

            // ---- stat tiles ----
            out.println("<div class='stats'>");
            stat(out, "Revenue this month", Layout.money(monthRevenue), true);
            stat(out, "Ready to rent", String.valueOf(ready), false);
            stat(out, "On rent now", String.valueOf(onRent.size()), false);
            stat(out, "Overdue returns", String.valueOf(overdue), false);
            stat(out, "Upcoming bookings", String.valueOf(upcoming.size()), false);
            stat(out, "In maintenance", String.valueOf(maintenance), false);
            stat(out, "Vehicles in fleet", String.valueOf(vehicles.size()), false);
            stat(out, "Customers", String.valueOf(customers), false);
            out.println("</div>");

            out.println("<div class='card inline'>");
            out.println("<a class='btn' href='BookSer'>New booking</a>");
            out.println("<a class='btn light' href='ReturnSer'>Return a vehicle</a>");
            out.println("<a class='btn light' href='CustomerSer?action=add'>Register customer</a>");
            out.println("</div>");

            // ---- on rent now ----
            out.println("<div class='card'><h2>On rent now</h2><div class='table-wrap'><table>");
            out.println("<tr><th>Booking</th><th>Customer</th><th>Vehicle</th><th>Pick-up</th>"
                    + "<th>Due back</th><th>Status</th><th></th></tr>");
            if (onRent.isEmpty()) {
                out.println("<tr><td colspan='7' class='empty'>No vehicle is out on rent.</td></tr>");
            }
            for (Booking b : onRent) {
                out.println("<tr><td>#" + b.getBookingId() + "</td>"
                        + "<td>" + Layout.esc(b.getCustomerName()) + "</td>"
                        + "<td>" + Layout.esc(b.getRegNo()) + " <span class='muted'>"
                        + Layout.esc(b.getMakeModel()) + "</span></td>"
                        + "<td>" + Layout.date(b.getFromDate()) + "</td>"
                        + "<td>" + Layout.date(b.getToDate()) + "</td>"
                        + "<td>" + Layout.badge(b.isOverdue() ? "OVERDUE" : "BOOKED") + "</td>"
                        + "<td><form class='plain' action='ReturnSer' method='post'>"
                        + "<input type='hidden' name='vid' value='" + b.getVehicleId() + "'>"
                        + "<button class='small'>Return</button></form></td></tr>");
            }
            out.println("</table></div></div>");

            // ---- upcoming ----
            out.println("<div class='card'><h2>Upcoming pick-ups</h2><div class='table-wrap'><table>");
            out.println("<tr><th>Booking</th><th>Customer</th><th>Vehicle</th><th>Pick-up</th>"
                    + "<th>Return</th><th class='num'>Amount</th></tr>");
            if (upcoming.isEmpty()) {
                out.println("<tr><td colspan='6' class='empty'>No upcoming bookings.</td></tr>");
            }
            for (Booking b : upcoming) {
                out.println("<tr><td>#" + b.getBookingId() + "</td>"
                        + "<td>" + Layout.esc(b.getCustomerName()) + "</td>"
                        + "<td>" + Layout.esc(b.getRegNo()) + " <span class='muted'>"
                        + Layout.esc(b.getMakeModel()) + "</span></td>"
                        + "<td>" + Layout.date(b.getFromDate()) + "</td>"
                        + "<td>" + Layout.date(b.getToDate()) + "</td>"
                        + "<td class='num'>" + Layout.money(b.getTotalAmt()) + "</td></tr>");
            }
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
}
