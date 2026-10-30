package com.rental.dao;

import com.rental.exception.BookingException;
import com.rental.model.Vehicle;
import com.rental.util.DBConnect;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Data access for the vehicles table: add, edit, list, find free vehicles.
 */
public class VehicleDAO {

    /** Registration number in capitals without spaces or dashes, e.g. MH12AB1234. */
    private void clean(Vehicle v) {
        if (v.getRegNo() != null) {
            v.setRegNo(v.getRegNo().trim().toUpperCase().replace(" ", "").replace("-", ""));
        }
        if (v.getMakeModel() != null) {
            v.setMakeModel(v.getMakeModel().trim());
        }
    }

    /** Checks the form data and throws BookingException with a clear message. */
    private void validate(Vehicle v) throws BookingException {
        if (v.getRegNo() == null || v.getRegNo().isEmpty()) {
            throw new BookingException("Registration number is required");
        }
        if (!v.getRegNo().matches("[A-Z0-9]{4,15}")) {
            throw new BookingException("Registration number must be 4 to 15 letters or digits");
        }
        if (v.getMakeModel() == null || v.getMakeModel().isEmpty()) {
            throw new BookingException("Make / model is required");
        }
        if (v.getMakeModel().length() > 50) {
            throw new BookingException("Make / model can have at most 50 characters");
        }
        if (!Arrays.asList(Vehicle.TYPES).contains(v.getType())) {
            throw new BookingException("Choose a vehicle type: CAR, SUV, BIKE or VAN");
        }
        if (v.getRatePerDay() <= 0 || v.getRatePerDay() > 99999) {
            throw new BookingException("Rate per day must be between 1 and 99999");
        }
        if (!Arrays.asList(Vehicle.STATUSES).contains(v.getStatus())) {
            throw new BookingException("Choose a status: AVAILABLE or MAINTENANCE");
        }
    }

    /** Saves a new vehicle and returns the new vehicle_id. */
    public int addVehicle(Vehicle v) throws SQLException, BookingException {
        clean(v);
        validate(v);
        String sql = "insert into vehicles(reg_no, make_model, type, rate_per_day, status) values(?,?,?,?,?)";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getRegNo());
            ps.setString(2, v.getMakeModel());
            ps.setString(3, v.getType());
            ps.setDouble(4, v.getRatePerDay());
            ps.setString(5, v.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new BookingException("Registration number " + v.getRegNo() + " already exists");
        }
    }

    /** Updates all details of an existing vehicle. */
    public void updateVehicle(Vehicle v) throws SQLException, BookingException {
        clean(v);
        validate(v);
        String sql = "update vehicles set reg_no=?, make_model=?, type=?, rate_per_day=?, status=?"
                + " where vehicle_id=?";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getRegNo());
            ps.setString(2, v.getMakeModel());
            ps.setString(3, v.getType());
            ps.setDouble(4, v.getRatePerDay());
            ps.setString(5, v.getStatus());
            ps.setInt(6, v.getVehicleId());
            if (ps.executeUpdate() == 0) {
                throw new BookingException("Vehicle #" + v.getVehicleId() + " not found");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new BookingException("Registration number " + v.getRegNo() + " already exists");
        }
    }

    /** Returns one vehicle, or null if the id does not exist. */
    public Vehicle getVehicle(int id) throws SQLException {
        String sql = "select * from vehicles where vehicle_id=?";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return read(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<Vehicle> getAllVehicles() throws SQLException {
        return getVehicles("ALL", "ALL");
    }

    /** Lists vehicles filtered by type and status ("ALL" = no filter). */
    public ArrayList<Vehicle> getVehicles(String type, String status) throws SQLException {
        ArrayList<Vehicle> list = new ArrayList<>();
        String sql = "select * from vehicles where 1=1";
        if (type != null && !type.equals("ALL")) {
            sql = sql + " and type=?";
        }
        if (status != null && !status.equals("ALL")) {
            sql = sql + " and status=?";
        }
        sql = sql + " order by type, reg_no";

        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            if (type != null && !type.equals("ALL")) {
                ps.setString(i++, type);
            }
            if (status != null && !status.equals("ALL")) {
                ps.setString(i++, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(read(rs));
                }
            }
        }
        return list;
    }

    /**
     * Vehicles that are AVAILABLE and have no BOOKED row overlapping the dates.
     * Same overlap rule as BookingDAO.book().
     */
    public ArrayList<Vehicle> findFreeVehicles(Date from, Date to, String type) throws SQLException {
        ArrayList<Vehicle> list = new ArrayList<>();
        String sql = "select * from vehicles v where v.status='AVAILABLE'"
                + " and not exists (select 1 from bookings b where b.vehicle_id = v.vehicle_id"
                + " and b.status='BOOKED' and b.from_date <= ? and greatest(b.to_date, ?) >= ?)";
        if (type != null && !type.equals("ALL")) {
            sql = sql + " and v.type=?";
        }
        sql = sql + " order by v.rate_per_day";

        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, to);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setDate(3, from);
            if (type != null && !type.equals("ALL")) {
                ps.setString(4, type);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(read(rs));
                }
            }
        }
        return list;
    }

    /** Copies the current ResultSet row into a Vehicle object. */
    private Vehicle read(ResultSet rs) throws SQLException {
        return new Vehicle(rs.getInt("vehicle_id"), rs.getString("reg_no"), rs.getString("make_model"),
                rs.getString("type"), rs.getDouble("rate_per_day"), rs.getString("status"));
    }
}
