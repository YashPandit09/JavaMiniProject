package com.rental.model;

import java.sql.Date;

/**
 * A payment taken when a vehicle is returned (one row of the payments table).
 */
public class Payment {

    private int paymentId;
    private int bookingId;
    private double amount;
    private Date paidOn;

    // filled from joined tables, only for display
    private String customerName;
    private String regNo;

    public Payment() {
    }

    public Payment(int paymentId, int bookingId, double amount, Date paidOn,
                   String customerName, String regNo) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paidOn = paidOn;
        this.customerName = customerName;
        this.regNo = regNo;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(Date paidOn) {
        this.paidOn = paidOn;
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
}
