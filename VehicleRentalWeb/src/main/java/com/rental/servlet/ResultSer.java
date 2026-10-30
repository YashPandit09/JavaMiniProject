package com.rental.servlet;

import com.rental.model.Booking;
import com.rental.model.User;
import com.rental.util.Layout;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Shows the result of an action. Other servlets put the message in the request
 * with setAttribute() and forward() here.
 *
 * Request attributes read:
 *   msg      - success message (green)
 *   err      - error message (red)
 *   info     - extra line of detail (optional)
 *   bill     - Booking with bill details after a return (optional)
 *   back     - link for the button (default DashboardSer)
 *   backText - text on the button (optional)
 */
@WebServlet("/ResultSer")
public class ResultSer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        show(req, res);
    }

    // forward() from a POST arrives here
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        show(req, res);
    }

    private void show(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String msg = (String) req.getAttribute("msg");
        String err = (String) req.getAttribute("err");
        if (msg == null && err == null) {
            res.sendRedirect("DashboardSer");                  // opened directly: nothing to show
            return;
        }
        String info = (String) req.getAttribute("info");
        Booking bill = (Booking) req.getAttribute("bill");
        String back = (String) req.getAttribute("back");
        String backText = (String) req.getAttribute("backText");
        if (back == null) {
            back = "DashboardSer";
        }
        if (backText == null) {
            backText = (err == null) ? "Continue" : "Go back";
        }

        // the user may be null here (failed login)
        HttpSession session = req.getSession(false);
        User u = (session == null) ? null : (User) session.getAttribute("user");

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        Layout.top(out, err == null ? "Success" : "Something went wrong", u, "");

        out.println("<div class='card'>");
        Layout.message(out, msg, err);
        if (info != null) {
            out.println("<p>" + Layout.esc(info) + "</p>");
        }
        if (bill != null) {
            printBill(out, bill);
        }
        out.println("<a class='btn' href='" + Layout.esc(back) + "'>" + Layout.esc(backText) + "</a>");
        out.println("</div>");

        Layout.bottom(out);
    }

    private void printBill(PrintWriter out, Booking b) {
        out.println("<h2>Bill</h2>");
        out.println("<table class='bill'>");
        row(out, "Booking", "#" + b.getBookingId());
        row(out, "Customer", Layout.esc(b.getCustomerName()));
        row(out, "Vehicle", Layout.esc(b.getRegNo() + " - " + b.getMakeModel()));
        row(out, "Pick-up date", Layout.date(b.getFromDate()));
        row(out, "Booked return date", Layout.date(b.getToDate()));
        row(out, "Returned on", Layout.date(b.getReturnDate()));
        if (b.getReturnDate().after(b.getToDate())) {
            long late = b.getDays() - b.getBookedDays();
            row(out, "Late by", late + " day(s) - charged at the same daily rate");
        }
        row(out, "Days x rate", b.getDays() + " x " + Layout.money(b.getRatePerDay()));
        out.println("<tr class='total'><td>Amount paid</td><td>" + Layout.money(b.getTotalAmt()) + "</td></tr>");
        out.println("</table><br>");
    }

    private void row(PrintWriter out, String label, String value) {
        out.println("<tr><td>" + label + "</td><td>" + value + "</td></tr>");
    }
}
