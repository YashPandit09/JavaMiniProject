package com.vehiclerental;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists vehicles from the database with optional filtering.
 * <ul>
 *   <li>GET /vehicles — retrieves vehicles, optionally filtered by
 *       {@code vehicleType}, {@code pickupDate}, and {@code returnDate},
 *       then forwards to vehicles.jsp.</li>
 * </ul>
 *
 * When pickup and return dates are supplied, vehicles that have a
 * conflicting CONFIRMED booking in that date range are excluded.
 */
@WebServlet(name = "VehicleServlet", urlPatterns = {"/vehicles"})
public class VehicleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String vehicleType = trim(request.getParameter("vehicleType"));
        String pickupDate  = trim(request.getParameter("pickupDate"));
        String returnDate  = trim(request.getParameter("returnDate"));

        List<Map<String, Object>> vehicles = new ArrayList<>();

        /*
         * Build the SQL dynamically based on which filters are present.
         *
         * When dates are supplied we use a NOT EXISTS subquery to exclude
         * vehicles that have an overlapping CONFIRMED booking:
         *
         *   existing.pickup_date  < requested.return_date
         *   AND existing.return_date > requested.pickup_date
         */
        StringBuilder sql = new StringBuilder(
                "SELECT v.id, v.name, v.brand, v.model, v.vehicle_type, "
              + "v.registration_number, v.seating_capacity, v.price_per_day, "
              + "v.image_url, v.description, v.status "
              + "FROM vehicles v WHERE v.status = 'AVAILABLE'");

        List<Object> params = new ArrayList<>();

        // Filter by vehicle type
        if (!vehicleType.isEmpty()) {
            sql.append(" AND v.vehicle_type = ?");
            params.add(vehicleType);
        }

        // Filter by date availability
        boolean hasDates = !pickupDate.isEmpty() && !returnDate.isEmpty();
        if (hasDates) {
            sql.append(" AND NOT EXISTS ("
                    + "SELECT 1 FROM bookings b "
                    + "WHERE b.vehicle_id = v.id "
                    + "AND b.booking_status = 'CONFIRMED' "
                    + "AND b.pickup_date < ? "
                    + "AND b.return_date > ?)");
            params.add(java.sql.Date.valueOf(returnDate));   // < requested return
            params.add(java.sql.Date.valueOf(pickupDate));   // > requested pickup
        }

        sql.append(" ORDER BY v.vehicle_type, v.price_per_day");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    stmt.setString(i + 1, (String) p);
                } else if (p instanceof java.sql.Date) {
                    stmt.setDate(i + 1, (java.sql.Date) p);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> v = new HashMap<>();
                    v.put("id",                 rs.getInt("id"));
                    v.put("name",               rs.getString("name"));
                    v.put("brand",              rs.getString("brand"));
                    v.put("model",              rs.getString("model"));
                    v.put("vehicleType",        rs.getString("vehicle_type"));
                    v.put("registrationNumber", rs.getString("registration_number"));
                    v.put("seatingCapacity",    rs.getInt("seating_capacity"));
                    v.put("pricePerDay",        rs.getBigDecimal("price_per_day"));
                    v.put("imageUrl",           rs.getString("image_url"));
                    v.put("description",        rs.getString("description"));
                    v.put("status",             rs.getString("status"));
                    vehicles.add(v);
                }
            }

        } catch (IllegalArgumentException e) {
            // Invalid date format
            request.setAttribute("error", "Please enter valid dates in YYYY-MM-DD format.");
        } catch (SQLException e) {
            log("Vehicle listing failed", e);
            request.setAttribute("error", "Unable to load vehicles. Please try again later.");
        }

        request.setAttribute("vehicles",    vehicles);
        request.setAttribute("vehicleType", vehicleType);
        request.setAttribute("pickupDate",  pickupDate);
        request.setAttribute("returnDate",  returnDate);

        request.getRequestDispatcher("/vehicles.jsp").forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
