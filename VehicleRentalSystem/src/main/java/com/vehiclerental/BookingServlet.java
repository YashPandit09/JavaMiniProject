package com.vehiclerental;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles vehicle booking operations.
 * <ul>
 *   <li>GET  /booking              — shows the booking form for a selected vehicle</li>
 *   <li>POST /booking              — creates a new booking (with transaction)</li>
 *   <li>GET  /booking?action=mybookings — lists the logged-in user's bookings</li>
 *   <li>POST /booking?action=cancel — cancels a booking owned by the logged-in user</li>
 * </ul>
 *
 * <h3>Concurrency protection</h3>
 * The booking creation uses a database transaction with
 * {@code SERIALIZABLE} isolation level. Inside the transaction:
 * <ol>
 *   <li>Check for overlapping CONFIRMED bookings using {@code SELECT ... FOR UPDATE}</li>
 *   <li>If no conflict, insert the new booking</li>
 *   <li>Commit</li>
 * </ol>
 * {@code FOR UPDATE} acquires an exclusive lock on the matching rows in the
 * bookings table for the target vehicle, preventing a concurrent transaction
 * from inserting a conflicting booking before this one commits.
 * <p>
 * <strong>Limitation:</strong> {@code SELECT ... FOR UPDATE} only locks
 * <em>existing</em> rows. Two simultaneous first-ever bookings for the same
 * vehicle and overlapping dates could theoretically both see zero rows and
 * proceed. With {@code SERIALIZABLE} isolation, InnoDB uses gap locks that
 * cover the "no existing rows" case as well, providing strong protection.
 * For absolute guarantees beyond what the database offers, a unique constraint
 * on a separate availability table or an application-level distributed lock
 * would be needed — an over-engineering for a college demo.
 */
@WebServlet(name = "BookingServlet", urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    // ---- GET dispatcher ----------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = trim(request.getParameter("action"));

        if ("mybookings".equals(action)) {
            showMyBookings(request, response);
        } else {
            showBookingForm(request, response);
        }
    }

    // ---- POST dispatcher ---------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = trim(request.getParameter("action"));

        if ("cancel".equals(action)) {
            cancelBooking(request, response);
        } else {
            createBooking(request, response);
        }
    }

    // ========================================================================
    //  Show booking form (GET /booking?vehicleId=...)
    // ========================================================================

    private void showBookingForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Must be logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirected=true");
            return;
        }

        String vehicleIdStr = request.getParameter("vehicleId");
        if (vehicleIdStr == null || vehicleIdStr.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/vehicles");
            return;
        }

        int vehicleId;
        try {
            vehicleId = Integer.parseInt(vehicleIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/vehicles");
            return;
        }

        // Load vehicle from DB
        String sql = "SELECT id, name, brand, model, vehicle_type, registration_number, "
                   + "seating_capacity, price_per_day, image_url, description, status "
                   + "FROM vehicles WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> vehicle = new HashMap<>();
                    vehicle.put("id",               rs.getInt("id"));
                    vehicle.put("name",             rs.getString("name"));
                    vehicle.put("brand",            rs.getString("brand"));
                    vehicle.put("model",            rs.getString("model"));
                    vehicle.put("vehicleType",      rs.getString("vehicle_type"));
                    vehicle.put("registrationNumber", rs.getString("registration_number"));
                    vehicle.put("seatingCapacity",  rs.getInt("seating_capacity"));
                    vehicle.put("pricePerDay",      rs.getBigDecimal("price_per_day"));
                    vehicle.put("imageUrl",         rs.getString("image_url"));
                    vehicle.put("description",      rs.getString("description"));
                    vehicle.put("status",           rs.getString("status"));
                    request.setAttribute("vehicle", vehicle);
                } else {
                    request.setAttribute("error", "Vehicle not found.");
                }
            }
        } catch (SQLException e) {
            log("Failed to load vehicle for booking", e);
            request.setAttribute("error", "Unable to load vehicle details.");
        }

        // Pass pre-selected dates if they came from the search
        request.setAttribute("pickupDate",  trim(request.getParameter("pickupDate")));
        request.setAttribute("returnDate",  trim(request.getParameter("returnDate")));

        request.getRequestDispatcher("/booking.jsp").forward(request, response);
    }

    // ========================================================================
    //  Create booking (POST /booking)
    // ========================================================================

    private void createBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ---- Auth check ----
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirected=true");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        // ---- Collect & validate parameters ----
        String vehicleIdStr = request.getParameter("vehicleId");
        String pickupStr    = trim(request.getParameter("pickupDate"));
        String returnStr    = trim(request.getParameter("returnDate"));

        if (vehicleIdStr == null || pickupStr.isEmpty() || returnStr.isEmpty()) {
            request.setAttribute("error", "Vehicle, pickup date, and return date are required.");
            showBookingForm(request, response);
            return;
        }

        int vehicleId;
        try {
            vehicleId = Integer.parseInt(vehicleIdStr);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid vehicle.");
            showBookingForm(request, response);
            return;
        }

        LocalDate pickupDate, returnDate;
        try {
            pickupDate = LocalDate.parse(pickupStr);
            returnDate = LocalDate.parse(returnStr);
        } catch (Exception e) {
            request.setAttribute("error", "Invalid date format. Use YYYY-MM-DD.");
            showBookingForm(request, response);
            return;
        }

        if (pickupDate.isBefore(LocalDate.now())) {
            request.setAttribute("error", "Pickup date cannot be in the past.");
            showBookingForm(request, response);
            return;
        }

        if (!returnDate.isAfter(pickupDate)) {
            request.setAttribute("error", "Return date must be after pickup date.");
            showBookingForm(request, response);
            return;
        }

        // ---- Transactional booking creation ----

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // 1. Load vehicle and verify it's AVAILABLE
            BigDecimal pricePerDay;
            String vehicleName;
            try (PreparedStatement vstmt = conn.prepareStatement(
                    "SELECT name, price_per_day, status FROM vehicles WHERE id = ?")) {
                vstmt.setInt(1, vehicleId);
                try (ResultSet vrs = vstmt.executeQuery()) {
                    if (!vrs.next()) {
                        conn.rollback();
                        request.setAttribute("error", "Vehicle not found.");
                        showBookingForm(request, response);
                        return;
                    }
                    if (!"AVAILABLE".equals(vrs.getString("status"))) {
                        conn.rollback();
                        request.setAttribute("error", "This vehicle is currently unavailable.");
                        showBookingForm(request, response);
                        return;
                    }
                    pricePerDay = vrs.getBigDecimal("price_per_day");
                    vehicleName = vrs.getString("name");
                }
            }

            // 2. Check for overlapping bookings (FOR UPDATE to lock rows)
            String overlapSql = "SELECT id FROM bookings "
                    + "WHERE vehicle_id = ? AND booking_status = 'CONFIRMED' "
                    + "AND pickup_date < ? AND return_date > ? "
                    + "FOR UPDATE";
            try (PreparedStatement ostmt = conn.prepareStatement(overlapSql)) {
                ostmt.setInt(1, vehicleId);
                ostmt.setDate(2, Date.valueOf(returnDate));
                ostmt.setDate(3, Date.valueOf(pickupDate));
                try (ResultSet ors = ostmt.executeQuery()) {
                    if (ors.next()) {
                        conn.rollback();
                        request.setAttribute("error",
                                "This vehicle is already booked for the selected dates. Please choose different dates.");
                        showBookingForm(request, response);
                        return;
                    }
                }
            }

            // 3. Calculate totals on the server
            long totalDays = ChronoUnit.DAYS.between(pickupDate, returnDate);
            BigDecimal totalAmount = pricePerDay.multiply(BigDecimal.valueOf(totalDays));

            // 4. Insert booking
            String insertSql = "INSERT INTO bookings (user_id, vehicle_id, pickup_date, return_date, "
                    + "total_days, total_amount, booking_status) VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')";
            int bookingId;
            try (PreparedStatement istmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                istmt.setInt(1, userId);
                istmt.setInt(2, vehicleId);
                istmt.setDate(3, Date.valueOf(pickupDate));
                istmt.setDate(4, Date.valueOf(returnDate));
                istmt.setInt(5, (int) totalDays);
                istmt.setBigDecimal(6, totalAmount);
                istmt.executeUpdate();

                try (ResultSet keys = istmt.getGeneratedKeys()) {
                    keys.next();
                    bookingId = keys.getInt(1);
                }
            }

            conn.commit();

            // 5. Set confirmation attributes
            Map<String, Object> confirmation = new HashMap<>();
            confirmation.put("bookingId",   bookingId);
            confirmation.put("vehicleName", vehicleName);
            confirmation.put("pickupDate",  pickupDate.toString());
            confirmation.put("returnDate",  returnDate.toString());
            confirmation.put("totalDays",   totalDays);
            confirmation.put("totalAmount", totalAmount);
            request.setAttribute("confirmation", confirmation);

            request.getRequestDispatcher("/booking.jsp").forward(request, response);

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) { }
            }
            log("Booking creation failed", e);
            request.setAttribute("error", "Unable to complete the booking. Please try again.");
            showBookingForm(request, response);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) { }
            }
        }
    }

    // ========================================================================
    //  My bookings (GET /booking?action=mybookings)
    // ========================================================================

    private void showMyBookings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirected=true");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        List<Map<String, Object>> bookings = new ArrayList<>();

        String sql = "SELECT b.id, b.pickup_date, b.return_date, b.total_days, b.total_amount, "
                   + "b.booking_status, b.created_at, v.name AS vehicle_name, v.brand, v.model, "
                   + "v.image_url "
                   + "FROM bookings b JOIN vehicles v ON b.vehicle_id = v.id "
                   + "WHERE b.user_id = ? ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> b = new HashMap<>();
                    b.put("id",            rs.getInt("id"));
                    b.put("pickupDate",    rs.getDate("pickup_date").toLocalDate().toString());
                    b.put("returnDate",    rs.getDate("return_date").toLocalDate().toString());
                    b.put("totalDays",     rs.getInt("total_days"));
                    b.put("totalAmount",   rs.getBigDecimal("total_amount"));
                    b.put("bookingStatus", rs.getString("booking_status"));
                    b.put("createdAt",     rs.getTimestamp("created_at").toString());
                    b.put("vehicleName",   rs.getString("vehicle_name"));
                    b.put("brand",         rs.getString("brand"));
                    b.put("model",         rs.getString("model"));
                    b.put("imageUrl",      rs.getString("image_url"));

                    // Can cancel only if CONFIRMED and pickup date has not passed
                    LocalDate pickup = rs.getDate("pickup_date").toLocalDate();
                    boolean canCancel = "CONFIRMED".equals(rs.getString("booking_status"))
                                        && !pickup.isBefore(LocalDate.now());
                    b.put("canCancel", canCancel);

                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            log("Failed to load bookings", e);
            request.setAttribute("error", "Unable to load your bookings.");
        }

        request.setAttribute("bookings", bookings);
        request.setAttribute("showMyBookings", true);
        request.getRequestDispatcher("/booking.jsp").forward(request, response);
    }

    // ========================================================================
    //  Cancel booking (POST /booking?action=cancel)
    // ========================================================================

    private void cancelBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirected=true");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        String bookingIdStr = request.getParameter("bookingId");
        if (bookingIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/booking?action=mybookings");
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(bookingIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/booking?action=mybookings");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Verify ownership, status, and pickup date
            String checkSql = "SELECT user_id, booking_status, pickup_date FROM bookings WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, bookingId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        request.setAttribute("error", "Booking not found.");
                        showMyBookings(request, response);
                        return;
                    }

                    // Authorization: booking must belong to this user
                    if (rs.getInt("user_id") != userId) {
                        request.setAttribute("error", "You are not authorized to cancel this booking.");
                        showMyBookings(request, response);
                        return;
                    }

                    if (!"CONFIRMED".equals(rs.getString("booking_status"))) {
                        request.setAttribute("error", "This booking is already cancelled.");
                        showMyBookings(request, response);
                        return;
                    }

                    LocalDate pickup = rs.getDate("pickup_date").toLocalDate();
                    if (pickup.isBefore(LocalDate.now())) {
                        request.setAttribute("error", "Cannot cancel a booking whose pickup date has already passed.");
                        showMyBookings(request, response);
                        return;
                    }
                }
            }

            // Perform cancellation
            String cancelSql = "UPDATE bookings SET booking_status = 'CANCELLED' WHERE id = ? AND user_id = ?";
            try (PreparedStatement cancelStmt = conn.prepareStatement(cancelSql)) {
                cancelStmt.setInt(1, bookingId);
                cancelStmt.setInt(2, userId);
                cancelStmt.executeUpdate();
            }

            request.setAttribute("success", "Booking #" + bookingId + " has been cancelled successfully.");
            showMyBookings(request, response);

        } catch (SQLException e) {
            log("Booking cancellation failed", e);
            request.setAttribute("error", "Unable to cancel the booking. Please try again.");
            showMyBookings(request, response);
        }
    }

    // ---- helpers -----------------------------------------------------------

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
