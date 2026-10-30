package com.rental.servlet;

import com.rental.dao.BookingDAO;
import com.rental.dao.VehicleDAO;
import com.rental.exception.BookingException;
import com.rental.model.Booking;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Return & Billing: close the rental of a vehicle, bill = days x rate, save the payment.
 *   GET  ReturnSer         form + vehicles out on rent
 *   POST ReturnSer  vid    BookingDAO.returnVehicle()
 */
@WebServlet("/ReturnSer")
public class ReturnSer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;
        }
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        Layout.top(out, "Return & Billing", u, "return");

        try {
            ArrayList<Vehicle> vehicles = new VehicleDAO().getAllVehicles();
            ArrayList<Booking> onRent = new BookingDAO().getActiveRentals();

            out.println("<div class='card'><h2>Return a vehicle</h2>");
            out.println("<form class='inline' action='ReturnSer' method='post'>");
            out.println("<div style='flex:1'><label for='vid'>Vehicle</label><select id='vid' name='vid' required>"
                    + "<option value=''>-- choose vehicle --</option>");
            for (Vehicle v : vehicles) {
                out.println("<option value='" + v.getVehicleId() + "'>"
                        + Layout.esc(v.getRegNo() + "  " + v.getMakeModel()) + "</option>");
            }
            out.println("</select></div><button>Return &amp; bill</button></form>");
            out.println("<p class='muted'>Bill = days from pick-up to today (at least 1) x rate per day. "
                    + "The payment is saved in the same transaction.</p></div>");

            out.println("<div class='card'><h2>Out on rent now</h2><div class='table-wrap'><table>");
            out.println("<tr><th>Booking</th><th>Customer</th><th>Vehicle</th><th>Pick-up</th><th>Due back</th>"
                    + "<th class='num'>Bill if returned today</th><th>Status</th><th></th></tr>");
            if (onRent.isEmpty()) {
                out.println("<tr><td colspan='8' class='empty'>No vehicle is out on rent.</td></tr>");
            }
            for (Booking b : onRent) {
                long days = Math.max(1, ChronoUnit.DAYS.between(b.getFromDate().toLocalDate(), LocalDate.now()));
                out.println("<tr><td>#" + b.getBookingId() + "</td>"
                        + "<td>" + Layout.esc(b.getCustomerName()) + "</td>"
                        + "<td><b>" + Layout.esc(b.getRegNo()) + "</b> <span class='muted'>"
                        + Layout.esc(b.getMakeModel()) + "</span></td>"
                        + "<td>" + Layout.date(b.getFromDate()) + "</td>"
                        + "<td>" + Layout.date(b.getToDate()) + "</td>"
                        + "<td class='num'>" + days + " x " + Layout.money(b.getRatePerDay()) + " = <b>"
                        + Layout.money(days * b.getRatePerDay()) + "</b></td>"
                        + "<td>" + Layout.badge(b.isOverdue() ? "OVERDUE" : "BOOKED") + "</td>"
                        + "<td><form class='plain' action='ReturnSer' method='post'>"
                        + "<input type='hidden' name='vid' value='" + b.getVehicleId() + "'>"
                        + "<button class='small'>Return</button></form></td></tr>");
            }
            out.println("</table></div></div>");
        } catch (SQLException e) {
            Layout.message(out, null, "Database error: " + e.getMessage());
        }
        Layout.bottom(out);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;
        }
        int vid = Layout.toInt(req.getParameter("vid"));
        try {
            if (vid <= 0) {
                throw new BookingException("Please choose a vehicle");
            }
            Booking bill = new BookingDAO().returnVehicle(vid);
            req.setAttribute("msg", "Vehicle " + bill.getRegNo() + " returned - booking #"
                    + bill.getBookingId() + " closed, payment of " + Layout.money(bill.getTotalAmt()) + " saved");
            req.setAttribute("bill", bill);
        } catch (BookingException e) {
            req.setAttribute("err", e.getMessage());
        } catch (SQLException e) {
            req.setAttribute("err", "Database error: " + e.getMessage());
        }
        req.setAttribute("back", "ReturnSer");
        req.setAttribute("backText", "Back to Return & Billing");
        req.getRequestDispatcher("ResultSer").forward(req, res);
    }
}
