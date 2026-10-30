package com.rental.dao;

import com.rental.exception.BookingException;
import com.rental.model.Booking;
import com.rental.model.Payment;
import com.rental.util.DBConnect;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Data access for bookings and payments:
 * book, cancel, return (all three are JDBC transactions), lists and reports.
 */
public class BookingDAO {

    private static final String SELECT_JOIN =
            "select b.*, c.name, v.reg_no, v.make_model, v.rate_per_day from bookings b"
            + " join customers c on b.customer_id = c.customer_id"
            + " join vehicles v on b.vehicle_id = v.vehicle_id";

    // =================================================================
    //  Date rules - used by book() and by the "find free vehicles" search
    // =================================================================
    public static void checkDates(Date from, Date to) throws BookingException {
        if (from == null || to == null) {
            throw new BookingException("Please enter both pick-up and return dates");
        }
        LocalDate pickUp = from.toLocalDate();
        LocalDate drop = to.toLocalDate();
        if (pickUp.isBefore(LocalDate.now())) {
            throw new BookingException("Pick-up date must be today or later");
        }
        if (!drop.isAfter(pickUp)) {
            throw new BookingException("Return date must be after pick-up date");
        }
        if (ChronoUnit.DAYS.between(pickUp, drop) > 90) {
            throw new BookingException("A booking can be for at most 90 days");
        }
    }

    // =================================================================
    //  BOOK - one JDBC transaction (commit / rollback)
    // =================================================================
    /**
     * Checks every rule and saves the booking. Returns the new booking_id.
     * If any rule fails, everything is rolled back and a BookingException is thrown.
     */
    public int book(int cid, int vid, Date from, Date to) throws SQLException, BookingException {
        checkDates(from, to);                       // pick-up today or later, return after pick-up

        Connection con = DBConnect.dbCon();
        try {
            con.setAutoCommit(false);               // begin transaction

            // lock the vehicle row (for update): if two staff book the same vehicle
            // at the same moment, the second one waits here until the first commits
            PreparedStatement ps1 = con.prepareStatement(
                    "select status, rate_per_day from vehicles where vehicle_id=? for update");
            ps1.setInt(1, vid);
            ResultSet rs1 = ps1.executeQuery();
            if (!rs1.next()) {
                throw new BookingException("Vehicle not found");
            }
            String vehicleStatus = rs1.getString("status");
            double rate = rs1.getDouble("rate_per_day");

            // customer exists and has a licence number
            PreparedStatement ps2 = con.prepareStatement(
                    "select license_no from customers where customer_id=?");
            ps2.setInt(1, cid);
            ResultSet rs2 = ps2.executeQuery();
            if (!rs2.next()) {
                throw new BookingException("Customer not found");
            }
            String licence = rs2.getString("license_no");
            if (licence == null || licence.isBlank()) {
                throw new BookingException("Customer has no licence number - add it before booking");
            }

            // vehicle is AVAILABLE (not in MAINTENANCE)
            if (!"AVAILABLE".equals(vehicleStatus)) {
                throw new BookingException("Vehicle not available - it is under " + vehicleStatus);
            }

            // no BOOKED row overlaps these dates:
            //   overlap if existing from_date <= new to  AND  existing to_date >= new from
            //   (an overdue vehicle that is not back yet counts as busy until today)
            PreparedStatement ps3 = con.prepareStatement(
                    "select count(*) from bookings where vehicle_id=? and status='BOOKED'"
                    + " and from_date <= ? and greatest(to_date, ?) >= ?");
            ps3.setInt(1, vid);
            ps3.setDate(2, to);
            ps3.setDate(3, Date.valueOf(LocalDate.now()));
            ps3.setDate(4, from);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            if (rs3.getInt(1) > 0) {
                throw new BookingException("Vehicle not available - it is already booked for these dates");
            }

            // all checks passed: save the booking
            long days = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());
            double total = days * rate;

            PreparedStatement ps = con.prepareStatement(
                    "insert into bookings(customer_id, vehicle_id, from_date, to_date, total_amt, status)"
                    + " values(?,?,?,?,?,'BOOKED')", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, cid);
            ps.setInt(2, vid);
            ps.setDate(3, from);
            ps.setDate(4, to);
            ps.setDouble(5, total);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int bookingId = keys.getInt(1);

            con.commit();                           // save
            return bookingId;
        } catch (SQLException | BookingException e) {
            con.rollback();                         // undo
            throw e;
        } finally {
            con.close();
        }
    }

    // =================================================================
    //  CANCEL - one JDBC transaction
    // =================================================================
    public void cancel(int bookingId) throws SQLException, BookingException {
        Connection con = DBConnect.dbCon();
        try {
            con.setAutoCommit(false);

            PreparedStatement ps1 = con.prepareStatement(
                    "select status, from_date from bookings where booking_id=? for update");
            ps1.setInt(1, bookingId);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) {
                throw new BookingException("Booking #" + bookingId + " not found");
            }
            String status = rs.getString("status");
            if (!"BOOKED".equals(status)) {
                throw new BookingException("Booking is already " + status);
            }
            if (rs.getDate("from_date").toLocalDate().isBefore(LocalDate.now())) {
                throw new BookingException("Rental has already started - use Return & Billing instead");
            }

            PreparedStatement ps2 = con.prepareStatement(
                    "update bookings set status='CANCELLED' where booking_id=?");
            ps2.setInt(1, bookingId);
            ps2.executeUpdate();

            con.commit();
        } catch (SQLException | BookingException e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }

    // =================================================================
    //  RETURN & BILLING - one JDBC transaction
    // =================================================================
    /**
     * Closes the rental that is out on this vehicle.
     * Bill = days x rate (days from pick-up to today, at least 1).
     * Saves the payment and returns the Booking with the bill details.
     */
    public Booking returnVehicle(int vid) throws SQLException, BookingException {
        LocalDate today = LocalDate.now();
        Connection con = DBConnect.dbCon();
        try {
            con.setAutoCommit(false);

            // the booking that is out on rent now (pick-up date has come)
            PreparedStatement ps1 = con.prepareStatement(SELECT_JOIN
                    + " where b.vehicle_id=? and b.status='BOOKED' and b.from_date <= ?"
                    + " order by b.from_date limit 1 for update");
            ps1.setInt(1, vid);
            ps1.setDate(2, Date.valueOf(today));
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) {
                throw new BookingException("No active booking for this vehicle");
            }
            Booking b = read(rs);

            // bill: days x rate
            long days = ChronoUnit.DAYS.between(b.getFromDate().toLocalDate(), today);
            if (days < 1) {
                days = 1;                           // minimum charge is one day
            }
            double amount = days * b.getRatePerDay();

            PreparedStatement ps2 = con.prepareStatement(
                    "update bookings set status='RETURNED', total_amt=? where booking_id=?");
            ps2.setDouble(1, amount);
            ps2.setInt(2, b.getBookingId());
            ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                    "insert into payments(booking_id, amount, paid_on) values(?,?,?)");
            ps3.setInt(1, b.getBookingId());
            ps3.setDouble(2, amount);
            ps3.setDate(3, Date.valueOf(today));
            ps3.executeUpdate();

            con.commit();

            b.setStatus("RETURNED");
            b.setDays(days);
            b.setTotalAmt(amount);
            b.setReturnDate(Date.valueOf(today));
            return b;
        } catch (SQLException | BookingException e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }

    // =================================================================
    //  LISTS
    // =================================================================

    /** One booking with customer and vehicle names, or null if the id does not exist. */
    public Booking getBooking(int id) throws SQLException {
        String sql = SELECT_JOIN + " where b.booking_id=?";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ArrayList<Booking> list = readAll(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    /** All bookings with this status ("ALL" = every booking), newest first. */
    public ArrayList<Booking> getBookings(String status) throws SQLException {
        String sql = SELECT_JOIN;
        if (status != null && !status.equals("ALL")) {
            sql = sql + " where b.status=?";
        }
        sql = sql + " order by b.booking_id desc";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (status != null && !status.equals("ALL")) {
                ps.setString(1, status);
            }
            return readAll(ps);
        }
    }

    /** Rental history of one customer, newest first. */
    public ArrayList<Booking> getBookingsByCustomer(int cid) throws SQLException {
        String sql = SELECT_JOIN + " where b.customer_id=? order by b.from_date desc";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            return readAll(ps);
        }
    }

    /** Vehicles out on rent now (BOOKED and pick-up date has come), earliest due first. */
    public ArrayList<Booking> getActiveRentals() throws SQLException {
        String sql = SELECT_JOIN + " where b.status='BOOKED' and b.from_date <= ? order by b.to_date";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            return readAll(ps);
        }
    }

    /** BOOKED rentals that start after today. */
    public ArrayList<Booking> getUpcomingBookings() throws SQLException {
        String sql = SELECT_JOIN + " where b.status='BOOKED' and b.from_date > ? order by b.from_date";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            return readAll(ps);
        }
    }

    // =================================================================
    //  REPORTS
    // =================================================================

    /** Payments received between two dates (both included). */
    public ArrayList<Payment> getPayments(Date from, Date to) throws SQLException {
        ArrayList<Payment> list = new ArrayList<>();
        String sql = "select p.*, c.name, v.reg_no from payments p"
                + " join bookings b on p.booking_id = b.booking_id"
                + " join customers c on b.customer_id = c.customer_id"
                + " join vehicles v on b.vehicle_id = v.vehicle_id"
                + " where p.paid_on between ? and ? order by p.paid_on desc, p.payment_id desc";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Payment(rs.getInt("payment_id"), rs.getInt("booking_id"),
                            rs.getDouble("amount"), rs.getDate("paid_on"),
                            rs.getString("name"), rs.getString("reg_no")));
                }
            }
        }
        return list;
    }

    /** Total of all payments ever received. */
    public double getTotalRevenue() throws SQLException {
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement("select coalesce(sum(amount), 0) from payments");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    /** Revenue of the last 6 months, oldest first, e.g. "Oct 2026" -> 26800.0 */
    public LinkedHashMap<String, Double> getRevenueByMonth() throws SQLException {
        // amounts from the database, keyed by "2026-10"
        LinkedHashMap<String, Double> fromDb = new LinkedHashMap<>();
        YearMonth first = YearMonth.now().minusMonths(5);
        String sql = "select date_format(paid_on, '%Y-%m') as ym, sum(amount) as total from payments"
                + " where paid_on >= ? group by ym";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(first.atDay(1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fromDb.put(rs.getString("ym"), rs.getDouble("total"));
                }
            }
        }

        // one entry for every month, 0 when there was no payment
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        DateTimeFormatter label = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 0; i < 6; i++) {
            YearMonth ym = first.plusMonths(i);
            result.put(ym.format(label), fromDb.getOrDefault(ym.toString(), 0.0));
        }
        return result;
    }

    /** Revenue per vehicle type, highest first. */
    public LinkedHashMap<String, Double> getRevenueByType() throws SQLException {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        String sql = "select v.type, sum(p.amount) as total from payments p"
                + " join bookings b on p.booking_id = b.booking_id"
                + " join vehicles v on b.vehicle_id = v.vehicle_id"
                + " group by v.type order by total desc";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("type"), rs.getDouble("total"));
            }
        }
        return map;
    }

    /** Number of bookings for each status. */
    public LinkedHashMap<String, Integer> getBookingCountByStatus() throws SQLException {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("BOOKED", 0);
        map.put("RETURNED", 0);
        map.put("CANCELLED", 0);
        String sql = "select status, count(*) as n from bookings group by status";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("n"));
            }
        }
        return map;
    }

    /**
     * Most-rented vehicles (cancelled bookings not counted).
     * Each row: {reg_no, make_model, number of rentals, revenue}
     */
    public ArrayList<String[]> getTopVehicles(int limit) throws SQLException {
        ArrayList<String[]> rows = new ArrayList<>();
        String sql = "select v.reg_no, v.make_model, count(*) as trips, coalesce(sum(p.amount), 0) as revenue"
                + " from bookings b"
                + " join vehicles v on b.vehicle_id = v.vehicle_id"
                + " left join payments p on p.booking_id = b.booking_id"
                + " where b.status <> 'CANCELLED'"
                + " group by v.vehicle_id, v.reg_no, v.make_model"
                + " order by trips desc, revenue desc limit ?";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{rs.getString("reg_no"), rs.getString("make_model"),
                        String.valueOf(rs.getInt("trips")), String.valueOf(rs.getDouble("revenue"))});
                }
            }
        }
        return rows;
    }

    // =================================================================
    //  helpers
    // =================================================================
    private ArrayList<Booking> readAll(PreparedStatement ps) throws SQLException {
        ArrayList<Booking> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(read(rs));
            }
        }
        return list;
    }

    /** Copies the current row of a SELECT_JOIN query into a Booking object. */
    private Booking read(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setVehicleId(rs.getInt("vehicle_id"));
        b.setFromDate(rs.getDate("from_date"));
        b.setToDate(rs.getDate("to_date"));
        b.setTotalAmt(rs.getDouble("total_amt"));
        b.setStatus(rs.getString("status"));
        b.setCustomerName(rs.getString("name"));
        b.setRegNo(rs.getString("reg_no"));
        b.setMakeModel(rs.getString("make_model"));
        b.setRatePerDay(rs.getDouble("rate_per_day"));
        return b;
    }
}
