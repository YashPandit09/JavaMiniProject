package com.rental.servlet;

import com.rental.dao.VehicleDAO;
import com.rental.exception.BookingException;
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
import java.util.ArrayList;

/**
 * Vehicles: list with filters (everyone), add / edit (ADMIN only).
 *   GET  VehicleSer?type=&status=      list
 *   GET  VehicleSer?action=add         empty form   (ADMIN)
 *   GET  VehicleSer?action=edit&id=    filled form  (ADMIN)
 *   POST VehicleSer  action=add | update           (ADMIN)
 */
@WebServlet("/VehicleSer")
public class VehicleSer extends HttpServlet {

    private static final String ADMIN_ONLY = "Only ADMIN can add or edit vehicles";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User u = Layout.checkLogin(req, res);
        if (u == null) {
            return;
        }
        String action = req.getParameter("action");

        // add / edit pages are for ADMIN only
        if (("add".equals(action) || "edit".equals(action)) && !u.isAdmin()) {
            req.setAttribute("err", ADMIN_ONLY);
            req.setAttribute("back", "VehicleSer");
            req.setAttribute("backText", "Back to vehicles");
            req.getRequestDispatcher("ResultSer").forward(req, res);
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {
            if ("add".equals(action)) {
                Layout.top(out, "Add vehicle", u, "vehicles");
                showForm(out, new Vehicle(0, "", "", "CAR", 0, "AVAILABLE"));
            } else if ("edit".equals(action)) {
                Vehicle v = new VehicleDAO().getVehicle(Layout.toInt(req.getParameter("id")));
                Layout.top(out, "Edit vehicle", u, "vehicles");
                if (v == null) {
                    Layout.message(out, null, "Vehicle not found");
                } else {
                    showForm(out, v);
                }
            } else {
                showList(out, u, req.getParameter("type"), req.getParameter("status"));
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
        if (!u.isAdmin()) {                                    // check again on save
            req.setAttribute("err", ADMIN_ONLY);
            req.setAttribute("back", "VehicleSer");
            req.getRequestDispatcher("ResultSer").forward(req, res);
            return;
        }

        // read the form
        Vehicle v = new Vehicle();
        v.setVehicleId(Layout.toInt(req.getParameter("id")));
        v.setRegNo(req.getParameter("txtreg"));
        v.setMakeModel(req.getParameter("txtmodel"));
        v.setType(req.getParameter("txttype"));
        v.setStatus(req.getParameter("txtstatus"));

        VehicleDAO dao = new VehicleDAO();
        try {
            v.setRatePerDay(Double.parseDouble(req.getParameter("txtrate").trim()));
            if ("update".equals(req.getParameter("action"))) {
                dao.updateVehicle(v);
                req.setAttribute("msg", "Vehicle " + v.getRegNo() + " updated");
            } else {
                int id = dao.addVehicle(v);
                req.setAttribute("msg", "Vehicle " + v.getRegNo() + " added (ID #" + id + ")");
            }
            req.setAttribute("info", v.getMakeModel() + "  |  " + v.getType() + "  |  "
                    + Layout.money(v.getRatePerDay()) + " per day  |  " + v.getStatus());
            req.setAttribute("back", "VehicleSer");
            req.setAttribute("backText", "Back to vehicles");
        } catch (NumberFormatException | NullPointerException e) {
            req.setAttribute("err", "Rate per day must be a number");
            req.setAttribute("back", "javascript:history.back()");
        } catch (BookingException e) {
            req.setAttribute("err", e.getMessage());
            req.setAttribute("back", "javascript:history.back()");
        } catch (SQLException e) {
            req.setAttribute("err", "Database error: " + e.getMessage());
            req.setAttribute("back", "VehicleSer");
        }
        req.getRequestDispatcher("ResultSer").forward(req, res);
    }

    // ---------------------------------------------------------------
    private void showList(PrintWriter out, User u, String type, String status) throws SQLException {
        if (type == null) {
            type = "ALL";
        }
        if (status == null) {
            status = "ALL";
        }
        Layout.top(out, "Vehicles", u, "vehicles");
        ArrayList<Vehicle> list = new VehicleDAO().getVehicles(type, status);

        out.println("<div class='card'><form class='inline' action='VehicleSer' method='get'>");
        out.println("<div><label for='type'>Type</label><select id='type' name='type'>"
                + "<option>ALL</option>" + Layout.options(Vehicle.TYPES, type) + "</select></div>");
        out.println("<div><label for='status'>Status</label><select id='status' name='status'>"
                + "<option>ALL</option>" + Layout.options(Vehicle.STATUSES, status) + "</select></div>");
        out.println("<button>Filter</button>");
        out.println("<a class='btn' href='VehicleSer?action=add'>+ Add vehicle</a>");
        out.println("</form></div>");

        out.println("<div class='card'><div class='table-wrap'><table>");
        out.println("<tr><th>ID</th><th>Reg. no.</th><th>Make / model</th><th>Type</th>"
                + "<th class='num'>Rate / day</th><th>Status</th><th></th></tr>");
        if (list.isEmpty()) {
            out.println("<tr><td colspan='7' class='empty'>No vehicles match.</td></tr>");
        }
        for (Vehicle v : list) {
            out.println("<tr><td>#" + v.getVehicleId() + "</td>"
                    + "<td><b>" + Layout.esc(v.getRegNo()) + "</b></td>"
                    + "<td>" + Layout.esc(v.getMakeModel()) + "</td>"
                    + "<td>" + Layout.esc(v.getType()) + "</td>"
                    + "<td class='num'>" + Layout.money(v.getRatePerDay()) + "</td>"
                    + "<td>" + Layout.badge(v.getStatus()) + "</td>"
                    + "<td class='right'><a class='btn small light' href='VehicleSer?action=edit&id="
                    + v.getVehicleId() + "'>Edit</a></td></tr>");
        }
        out.println("</table></div>");
        out.println("<p class='muted'>" + list.size() + " vehicle(s). Only ADMIN can add or edit vehicles.</p></div>");
    }

    private void showForm(PrintWriter out, Vehicle v) {
        boolean edit = v.getVehicleId() > 0;
        out.println("<div class='card'><form class='grid two' action='VehicleSer' method='post'>");
        out.println("<input type='hidden' name='action' value='" + (edit ? "update" : "add") + "'>");
        out.println("<input type='hidden' name='id' value='" + v.getVehicleId() + "'>");
        out.println("<div><label for='txtreg'>Registration no. (e.g. MH12AB1234)</label>"
                + "<input id='txtreg' name='txtreg' maxlength='15' required value='"
                + Layout.esc(v.getRegNo()) + "'></div>");
        out.println("<div><label for='txtmodel'>Make / model</label>"
                + "<input id='txtmodel' name='txtmodel' maxlength='50' required value='"
                + Layout.esc(v.getMakeModel()) + "'></div>");
        out.println("<div><label for='txttype'>Type</label><select id='txttype' name='txttype'>"
                + Layout.options(Vehicle.TYPES, v.getType()) + "</select></div>");
        out.println("<div><label for='txtrate'>Rate per day (Rs.)</label>"
                + "<input id='txtrate' name='txtrate' required value='"
                + (v.getRatePerDay() > 0 ? String.format("%.2f", v.getRatePerDay()) : "") + "'></div>");
        out.println("<div><label for='txtstatus'>Status</label><select id='txtstatus' name='txtstatus'>"
                + Layout.options(Vehicle.STATUSES, v.getStatus()) + "</select></div>");
        out.println("<div class='full inline'><button>" + (edit ? "Save changes" : "Add vehicle")
                + "</button><a class='btn light' href='VehicleSer'>Cancel</a></div>");
        out.println("</form></div>");
    }
}
