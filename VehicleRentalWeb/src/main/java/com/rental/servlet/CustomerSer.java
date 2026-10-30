package com.rental.servlet;

import com.rental.dao.BookingDAO;
import com.rental.dao.CustomerDAO;
import com.rental.exception.BookingException;
import com.rental.model.Booking;
import com.rental.model.Customer;
import com.rental.model.User;
import com.rental.util.Layout;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Customers: list + search, register, edit, rental history.
 *   GET  CustomerSer                  list (search with ?q=)
 *   GET  CustomerSer?action=add       empty form
 *   GET  CustomerSer?action=edit&id=  filled form
 *   GET  CustomerSer?action=history&id=
 *   POST CustomerSer  action=add | update
 */
@WebServlet("/CustomerSer")
public class CustomerSer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;
        }
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            switch (action) {
                case "add":
                    Layout.top(out, "Register customer", u, "customers");
                    showForm(out, new Customer());
                    break;
                case "edit":
                    Customer c = new CustomerDAO().getCustomer(Layout.toInt(req.getParameter("id")));
                    Layout.top(out, "Edit customer", u, "customers");
                    if (c == null) {
                        Layout.message(out, null, "Customer not found");
                    } else {
                        showForm(out, c);
                    }
                    break;
                case "history":
                    showHistory(out, u, Layout.toInt(req.getParameter("id")));
                    break;
                default:
                    showList(out, u, req.getParameter("q"));
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

        // read the form
        Customer c = new Customer();
        c.setCustomerId(Layout.toInt(req.getParameter("id")));
        c.setName(req.getParameter("txtname"));
        c.setPhone(req.getParameter("txtphone"));
        c.setLicenseNo(req.getParameter("txtlicence"));

        CustomerDAO dao = new CustomerDAO();
        try {
            if ("update".equals(req.getParameter("action"))) {
                dao.updateCustomer(c);
                req.setAttribute("msg", "Customer #" + c.getCustomerId() + " updated");
            } else {
                int id = dao.addCustomer(c);
                req.setAttribute("msg", "Customer #" + id + " registered");
            }
            req.setAttribute("info", c.getName() + "  |  " + c.getPhone()
                    + "  |  Licence: " + (c.hasLicence() ? c.getLicenseNo() : "not given (cannot book yet)"));
            req.setAttribute("back", "CustomerSer");
            req.setAttribute("backText", "Back to customers");
        } catch (BookingException e) {
            req.setAttribute("err", e.getMessage());
            req.setAttribute("back", "javascript:history.back()");   // the browser keeps the typed values
        } catch (SQLException e) {
            req.setAttribute("err", "Database error: " + e.getMessage());
            req.setAttribute("back", "CustomerSer");
        }
        req.getRequestDispatcher("ResultSer").forward(req, res);
    }

    // ---------------------------------------------------------------
    private void showList(PrintWriter out, User u, String q) throws SQLException {
        Layout.top(out, "Customers", u, "customers");
        ArrayList<Customer> list = new CustomerDAO().searchCustomers(q);

        out.println("<div class='card'><form class='inline' action='CustomerSer' method='get'>");
        out.println("<div style='flex:1'><label for='q'>Search by name, phone or licence</label>"
                + "<input id='q' name='q' value='" + Layout.esc(q) + "'></div>");
        out.println("<button>Search</button>");
        out.println("<a class='btn light' href='CustomerSer'>Show all</a>");
        out.println("<a class='btn' href='CustomerSer?action=add'>+ Register customer</a>");
        out.println("</form></div>");

        out.println("<div class='card'><div class='table-wrap'><table>");
        out.println("<tr><th>ID</th><th>Name</th><th>Phone</th><th>Licence no.</th><th></th></tr>");
        if (list.isEmpty()) {
            out.println("<tr><td colspan='5' class='empty'>No customers found.</td></tr>");
        }
        for (Customer c : list) {
            out.println("<tr><td>#" + c.getCustomerId() + "</td>"
                    + "<td>" + Layout.esc(c.getName()) + "</td>"
                    + "<td>" + Layout.esc(c.getPhone()) + "</td>"
                    + "<td>" + (c.hasLicence() ? Layout.esc(c.getLicenseNo())
                            : "<span class='badge cancelled'>NO LICENCE</span>") + "</td>"
                    + "<td class='right'><a class='btn small light' href='CustomerSer?action=history&id="
                    + c.getCustomerId() + "'>History</a> "
                    + "<a class='btn small light' href='CustomerSer?action=edit&id="
                    + c.getCustomerId() + "'>Edit</a></td></tr>");
        }
        out.println("</table></div>");
        out.println("<p class='muted'>" + list.size() + " customer(s)</p></div>");
    }

    private void showForm(PrintWriter out, Customer c) {
        boolean edit = c.getCustomerId() > 0;
        out.println("<div class='card'><form class='grid two' action='CustomerSer' method='post'>");
        out.println("<input type='hidden' name='action' value='" + (edit ? "update" : "add") + "'>");
        out.println("<input type='hidden' name='id' value='" + c.getCustomerId() + "'>");
        out.println("<div class='full'><label for='txtname'>Full name</label>"
                + "<input id='txtname' name='txtname' maxlength='60' required value='"
                + Layout.esc(c.getName()) + "'></div>");
        out.println("<div><label for='txtphone'>Phone (10 digits)</label>"
                + "<input id='txtphone' name='txtphone' inputmode='numeric' required value='"
                + Layout.esc(c.getPhone()) + "'></div>");
        out.println("<div><label for='txtlicence'>Driving licence no. (needed to book)</label>"
                + "<input id='txtlicence' name='txtlicence' maxlength='20' value='"
                + Layout.esc(c.getLicenseNo()) + "'></div>");
        out.println("<div class='full inline'><button>" + (edit ? "Save changes" : "Register customer")
                + "</button><a class='btn light' href='CustomerSer'>Cancel</a></div>");
        out.println("</form></div>");
    }

    private void showHistory(PrintWriter out, User u, int id) throws SQLException {
        Customer c = new CustomerDAO().getCustomer(id);
        Layout.top(out, "Rental history", u, "customers");
        if (c == null) {
            Layout.message(out, null, "Customer not found");
            return;
        }
        ArrayList<Booking> list = new BookingDAO().getBookingsByCustomer(id);
        long rentals = list.stream().filter(b -> !b.getStatus().equals("CANCELLED")).count();
        double spent = list.stream()
                .filter(b -> b.getStatus().equals("RETURNED"))
                .mapToDouble(b -> b.getTotalAmt())
                .sum();

        out.println("<div class='stats'>");
        out.println("<div class='stat hero'><div class='label'>Customer #" + c.getCustomerId()
                + "</div><div class='value'>" + Layout.esc(c.getName()) + "</div></div>");
        out.println("<div class='stat'><div class='label'>Phone / Licence</div><div class='value' style='font-size:16px'>"
                + Layout.esc(c.getPhone()) + "<br>"
                + (c.hasLicence() ? Layout.esc(c.getLicenseNo()) : "<span class='badge cancelled'>NO LICENCE</span>")
                + "</div></div>");
        out.println("<div class='stat'><div class='label'>Rentals</div><div class='value'>" + rentals + "</div></div>");
        out.println("<div class='stat'><div class='label'>Total paid</div><div class='value'>"
                + Layout.money(spent) + "</div></div>");
        out.println("</div>");

        out.println("<div class='card'><div class='table-wrap'><table>");
        out.println("<tr><th>Booking</th><th>Vehicle</th><th>Pick-up</th><th>Return</th>"
                + "<th class='num'>Amount</th><th>Status</th></tr>");
        if (list.isEmpty()) {
            out.println("<tr><td colspan='6' class='empty'>No rentals yet.</td></tr>");
        }
        for (Booking b : list) {
            out.println("<tr><td>#" + b.getBookingId() + "</td>"
                    + "<td>" + Layout.esc(b.getRegNo()) + " <span class='muted'>"
                    + Layout.esc(b.getMakeModel()) + "</span></td>"
                    + "<td>" + Layout.date(b.getFromDate()) + "</td>"
                    + "<td>" + Layout.date(b.getToDate()) + "</td>"
                    + "<td class='num'>" + Layout.money(b.getTotalAmt()) + "</td>"
                    + "<td>" + Layout.badge(b.isOverdue() ? "OVERDUE" : b.getStatus()) + "</td></tr>");
        }
        out.println("</table></div>");
        out.println("<br><a class='btn light' href='CustomerSer'>Back to customers</a> "
                + "<a class='btn light' href='CustomerSer?action=edit&id=" + id + "'>Edit customer</a></div>");
    }
}
