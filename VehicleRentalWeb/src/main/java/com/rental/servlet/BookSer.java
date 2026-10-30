package com.rental.servlet;

import com.rental.dao.BookingDAO;
import com.rental.dao.CustomerDAO;
import com.rental.dao.VehicleDAO;
import com.rental.exception.BookingException;
import com.rental.model.Booking;
import com.rental.model.Customer;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Booking a vehicle, the bookings list and cancelling.
 *   GET  BookSer                          search free vehicles + booking form
 *   GET  BookSer?action=list&status=      all bookings
 *   POST BookSer  (cid, vid, from, to)    book   -> BookingDAO.book()
 *   POST BookSer  action=cancel, bid      cancel -> BookingDAO.cancel()
 */
@WebServlet("/BookSer")
public class BookSer extends HttpServlet {

    private static final String[] BOOKING_STATUSES = {"BOOKED", "RETURNED", "CANCELLED"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;
        }
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            if ("list".equals(req.getParameter("action"))) {
                showList(out, u, req.getParameter("status"));
            } else {
                showBookingPage(out, u, req);
            }
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
        if ("cancel".equals(req.getParameter("action"))) {
            cancel(req);
        } else {
            book(req);
        }
        req.getRequestDispatcher("ResultSer").forward(req, res);
    }

    // ---------------------------------------------------------------
    //  BOOK: data passing with setAttribute() + forward()
    // ---------------------------------------------------------------
    private void book(HttpServletRequest req) {
        int cid = Layout.toInt(req.getParameter("cid"));
        int vid = Layout.toInt(req.getParameter("vid"));
        Date from = Layout.toDate(req.getParameter("from"));
        Date to = Layout.toDate(req.getParameter("to"));

        BookingDAO dao = new BookingDAO();
        try {
            if (cid <= 0 || vid <= 0) {
                throw new BookingException("Please choose a customer and a vehicle");
            }
            int id = dao.book(cid, vid, from, to);
            String msg = "Booking #" + id + " confirmed";
            req.setAttribute("msg", msg);

            Booking b = dao.getBooking(id);
            req.setAttribute("info", b.getCustomerName() + " | " + b.getRegNo() + " " + b.getMakeModel()
                    + " | " + Layout.date(b.getFromDate()) + " to " + Layout.date(b.getToDate())
                    + " (" + b.getBookedDays() + " days) | Estimated amount " + Layout.money(b.getTotalAmt()));
            req.setAttribute("back", "BookSer?action=list");
            req.setAttribute("backText", "View bookings");
        } catch (BookingException e) {
            req.setAttribute("err", e.getMessage());
            req.setAttribute("back", "javascript:history.back()");
        } catch (SQLException e) {
            req.setAttribute("err", "Database error: " + e.getMessage());
            req.setAttribute("back", "BookSer");
        }
    }

    private void cancel(HttpServletRequest req) {
        int bid = Layout.toInt(req.getParameter("bid"));
        try {
            if (bid <= 0) {
                throw new BookingException("Enter a valid booking number");
            }
            new BookingDAO().cancel(bid);
            req.setAttribute("msg", "Booking #" + bid + " cancelled");
        } catch (BookingException e) {
            req.setAttribute("err", e.getMessage());
        } catch (SQLException e) {
            req.setAttribute("err", "Database error: " + e.getMessage());
        }
        req.setAttribute("back", "BookSer?action=list");
        req.setAttribute("backText", "Back to bookings");
    }

    // ---------------------------------------------------------------
    //  Page: 1. find free vehicles   2. booking form
    // ---------------------------------------------------------------
    private void showBookingPage(PrintWriter out, User u, HttpServletRequest req) throws SQLException {
        Layout.top(out, "New booking", u, "book");

        String type = req.getParameter("type");
        if (type == null) {
            type = "ALL";
        }
        String fromText = req.getParameter("from");
        String toText = req.getParameter("to");
        boolean searched = fromText != null && toText != null;
        if (fromText == null) {
            fromText = LocalDate.now().toString();
        }
        if (toText == null) {
            toText = LocalDate.now().plusDays(1).toString();
        }
        int selectedVid = Layout.toInt(req.getParameter("vid"));
        int selectedCid = Layout.toInt(req.getParameter("cid"));

        // ---- 1. search ----
        out.println("<div class='card'><h2>1. Find free vehicles</h2>");
        out.println("<form class='inline' action='BookSer' method='get'>");
        out.println("<div><label for='sfrom'>Pick-up date</label><input type='date' id='sfrom' name='from' value='"
                + Layout.esc(fromText) + "' required></div>");
        out.println("<div><label for='sto'>Return date</label><input type='date' id='sto' name='to' value='"
                + Layout.esc(toText) + "' required></div>");
        out.println("<div><label for='stype'>Type</label><select id='stype' name='type'><option>ALL</option>"
                + Layout.options(Vehicle.TYPES, type) + "</select></div>");
        out.println("<button>Search</button></form>");

        if (searched) {
            Date from = Layout.toDate(fromText);
            Date to = Layout.toDate(toText);
            try {
                BookingDAO.checkDates(from, to);
                ArrayList<Vehicle> free = new VehicleDAO().findFreeVehicles(from, to, type);
                long days = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());

                out.println("<br><div class='table-wrap'><table>");
                out.println("<tr><th>Reg. no.</th><th>Make / model</th><th>Type</th><th class='num'>Rate / day</th>"
                        + "<th class='num'>Total for " + days + " day(s)</th><th></th></tr>");
                if (free.isEmpty()) {
                    out.println("<tr><td colspan='6' class='empty'>No free vehicles for these dates.</td></tr>");
                }
                for (Vehicle v : free) {
                    out.println("<tr><td><b>" + Layout.esc(v.getRegNo()) + "</b></td>"
                            + "<td>" + Layout.esc(v.getMakeModel()) + "</td>"
                            + "<td>" + Layout.esc(v.getType()) + "</td>"
                            + "<td class='num'>" + Layout.money(v.getRatePerDay()) + "</td>"
                            + "<td class='num'>" + Layout.money(days * v.getRatePerDay()) + "</td>"
                            + "<td class='right'><a class='btn small' href='BookSer?from=" + from + "&to=" + to
                            + "&type=" + Layout.esc(type) + "&vid=" + v.getVehicleId() + "#book'>Select</a></td></tr>");
                }
                out.println("</table></div><p class='muted'>" + free.size() + " free vehicle(s)</p>");
            } catch (BookingException e) {
                out.println("<br>");
                Layout.message(out, null, e.getMessage());
            }
        }
        out.println("</div>");

        // ---- 2. booking form ----
        ArrayList<Customer> customers = new CustomerDAO().getAllCustomers();
        ArrayList<Vehicle> vehicles = new VehicleDAO().getAllVehicles();

        out.println("<div class='card' id='book'><h2>2. Book</h2>");
        out.println("<form class='grid two' action='BookSer' method='post'>");
        out.println("<div><label for='cid'>Customer</label><select id='cid' name='cid' required>"
                + "<option value=''>-- choose customer --</option>");
        for (Customer c : customers) {
            out.println("<option value='" + c.getCustomerId() + "'"
                    + (c.getCustomerId() == selectedCid ? " selected" : "") + ">"
                    + Layout.esc(c.toString()) + (c.hasLicence() ? "" : "  [no licence]") + "</option>");
        }
        out.println("</select></div>");

        out.println("<div><label for='vid'>Vehicle</label><select id='vid' name='vid' required>"
                + "<option value=''>-- choose vehicle --</option>");
        for (Vehicle v : vehicles) {
            out.println("<option value='" + v.getVehicleId() + "'"
                    + (v.getVehicleId() == selectedVid ? " selected" : "") + ">"
                    + Layout.esc(v.toString()) + "</option>");
        }
        out.println("</select></div>");

        out.println("<div><label for='from'>Pick-up date</label><input type='date' id='from' name='from' value='"
                + Layout.esc(fromText) + "' required></div>");
        out.println("<div><label for='to'>Return date</label><input type='date' id='to' name='to' value='"
                + Layout.esc(toText) + "' required></div>");
        out.println("<div class='full inline'><button>Confirm booking</button>"
                + "<span class='muted'>All checks run inside one JDBC transaction. "
                + "Not registered yet? <a href='CustomerSer?action=add'>Register customer</a></span></div>");
        out.println("</form></div>");
    }

    // ---------------------------------------------------------------
    //  Page: all bookings + cancel
    // ---------------------------------------------------------------
    private void showList(PrintWriter out, User u, String status) throws SQLException {
        if (status == null) {
            status = "ALL";
        }
        Layout.top(out, "Bookings", u, "bookings");
        ArrayList<Booking> list = new BookingDAO().getBookings(status);

        out.println("<div class='row'>");
        out.println("<div class='card'><form class='inline' action='BookSer' method='get'>"
                + "<input type='hidden' name='action' value='list'>"
                + "<div style='flex:1'><label for='status'>Show</label><select id='status' name='status'>"
                + "<option>ALL</option>" + Layout.options(BOOKING_STATUSES, status) + "</select></div>"
                + "<button>Filter</button><a class='btn' href='BookSer'>+ New booking</a></form></div>");
        out.println("<div class='card'><form class='inline' action='BookSer' method='post'>"
                + "<input type='hidden' name='action' value='cancel'>"
                + "<div style='flex:1'><label for='bid'>Cancel booking no.</label>"
                + "<input id='bid' name='bid' inputmode='numeric' required></div>"
                + "<button class='danger'>Cancel booking</button></form></div>");
        out.println("</div>");

        out.println("<div class='card'><div class='table-wrap'><table>");
        out.println("<tr><th>Booking</th><th>Customer</th><th>Vehicle</th><th>Pick-up</th><th>Return</th>"
                + "<th class='num'>Amount</th><th>Status</th><th></th></tr>");
        if (list.isEmpty()) {
            out.println("<tr><td colspan='8' class='empty'>No bookings.</td></tr>");
        }
        for (Booking b : list) {
            String cancelBtn = "";
            if (b.getStatus().equals("BOOKED")) {
                cancelBtn = "<form class='plain' action='BookSer' method='post' "
                        + "onsubmit=\"return confirm('Cancel booking #" + b.getBookingId() + "?')\">"
                        + "<input type='hidden' name='action' value='cancel'>"
                        + "<input type='hidden' name='bid' value='" + b.getBookingId() + "'>"
                        + "<button class='small danger'>Cancel</button></form>";
            }
            out.println("<tr><td>#" + b.getBookingId() + "</td>"
                    + "<td>" + Layout.esc(b.getCustomerName()) + "</td>"
                    + "<td>" + Layout.esc(b.getRegNo()) + " <span class='muted'>"
                    + Layout.esc(b.getMakeModel()) + "</span></td>"
                    + "<td>" + Layout.date(b.getFromDate()) + "</td>"
                    + "<td>" + Layout.date(b.getToDate()) + "</td>"
                    + "<td class='num'>" + Layout.money(b.getTotalAmt()) + "</td>"
                    + "<td>" + Layout.badge(b.isOverdue() ? "OVERDUE" : b.getStatus()) + "</td>"
                    + "<td class='right'>" + cancelBtn + "</td></tr>");
        }
        out.println("</table></div>");
        out.println("<p class='muted'>" + list.size() + " booking(s). Amount is the estimate for BOOKED, "
                + "the amount paid for RETURNED.</p></div>");
    }
}
