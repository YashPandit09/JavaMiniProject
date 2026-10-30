package com.rental.model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One rental booking (one row of the bookings table).
 * Also carries the customer / vehicle names for display,
 * and the bill details after a return.
 */
public class Booking {

    private int bookingId;
    private int customerId;
    private int vehicleId;
    private Date fromDate;
    private Date toDate;
    private double totalAmt;
    private String status;        // BOOKED, RETURNED or CANCELLED

    // filled from joined tables, only for display
    private String customerName;
    private String regNo;
    private String makeModel;

    // filled by BookingDAO.returnVehicle() for the bill
    private double ratePerDay;
    private long days;
    private Date returnDate;

    public Booking() {
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public double getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(double totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getMakeModel() {
        return makeModel;
    }

    public void setMakeModel(String makeModel) {
        this.makeModel = makeModel;
    }

    public double getRatePerDay() {
        return ratePerDay;
    }

    public void setRatePerDay(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    /** Number of days that were booked (to_date - from_date). */
    public long getBookedDays() {
        return ChronoUnit.DAYS.between(fromDate.toLocalDate(), toDate.toLocalDate());
    }

    /** Still BOOKED but the return date has already passed. */
    public boolean isOverdue() {
        return "BOOKED".equals(status) && toDate.toLocalDate().isBefore(LocalDate.now());
    }

    /** BOOKED and the pick-up date has come, so the vehicle is out with the customer. */
    public boolean isOnRent() {
        return "BOOKED".equals(status) && !fromDate.toLocalDate().isAfter(LocalDate.now());
    }
}
