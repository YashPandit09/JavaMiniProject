package com.rental.dao;

import com.rental.exception.BookingException;
import com.rental.model.Customer;
import com.rental.util.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Data access for the customers table: register, edit, search.
 */
public class CustomerDAO {

    /** Trims the text and makes the licence upper case. A blank licence becomes null. */
    private void clean(Customer c) {
        if (c.getName() != null) {
            c.setName(c.getName().trim());
        }
        if (c.getPhone() != null) {
            c.setPhone(c.getPhone().trim());
        }
        String lic = c.getLicenseNo();
        if (lic == null || lic.isBlank()) {
            c.setLicenseNo(null);
        } else {
            c.setLicenseNo(lic.trim().toUpperCase().replace(" ", "").replace("-", ""));
        }
    }

    /** Checks the form data and throws BookingException with a clear message. */
    private void validate(Customer c) throws BookingException {
        if (c.getName() == null || c.getName().isEmpty()) {
            throw new BookingException("Customer name is required");
        }
        if (c.getName().length() > 60) {
            throw new BookingException("Customer name can have at most 60 characters");
        }
        if (c.getPhone() == null || !c.getPhone().matches("\\d{10}")) {
            throw new BookingException("Phone must be a 10-digit number");
        }
        if (c.hasLicence() && !c.getLicenseNo().matches("[A-Z0-9]{5,20}")) {
            throw new BookingException("Licence number must be 5 to 20 letters or digits");
        }
    }

    /** Saves a new customer and returns the new customer_id. */
    public int addCustomer(Customer c) throws SQLException, BookingException {
        clean(c);
        validate(c);
        String sql = "insert into customers(name, phone, license_no) values(?,?,?)";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            if (c.hasLicence()) {
                ps.setString(3, c.getLicenseNo());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new BookingException("Licence number " + c.getLicenseNo() + " is already registered");
        }
    }

    /** Updates name, phone and licence of an existing customer. */
    public void updateCustomer(Customer c) throws SQLException, BookingException {
        clean(c);
        validate(c);
        String sql = "update customers set name=?, phone=?, license_no=? where customer_id=?";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            if (c.hasLicence()) {
                ps.setString(3, c.getLicenseNo());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setInt(4, c.getCustomerId());
            if (ps.executeUpdate() == 0) {
                throw new BookingException("Customer #" + c.getCustomerId() + " not found");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new BookingException("Licence number " + c.getLicenseNo() + " is already registered");
        }
    }

    /** Returns one customer, or null if the id does not exist. */
    public Customer getCustomer(int id) throws SQLException {
        String sql = "select * from customers where customer_id=?";
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

    /** Searches by name, phone or licence. An empty keyword returns everyone. */
    public ArrayList<Customer> searchCustomers(String keyword) throws SQLException {
        ArrayList<Customer> list = new ArrayList<>();
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "select * from customers where name like ? or phone like ? or license_no like ?"
                + " order by name";
        try (Connection con = DBConnect.dbCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(read(rs));
                }
            }
        }
        return list;
    }

    public ArrayList<Customer> getAllCustomers() throws SQLException {
        return searchCustomers("");
    }

    /** Copies the current ResultSet row into a Customer object. */
    private Customer read(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("customer_id"), rs.getString("name"),
                rs.getString("phone"), rs.getString("license_no"));
    }
}
