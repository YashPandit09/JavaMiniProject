package com.rental.model;

/**
 * A customer who rents vehicles (one row of the customers table).
 */
public class Customer {

    private int customerId;
    private String name;
    private String phone;
    private String licenseNo;     // can be null: such a customer cannot book

    public Customer() {
    }

    public Customer(int customerId, String name, String phone, String licenseNo) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.licenseNo = licenseNo;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public boolean hasLicence() {
        return licenseNo != null && !licenseNo.isBlank();
    }

    /** Text shown in drop-down lists. */
    @Override
    public String toString() {
        return "#" + customerId + "  " + name + "  (" + phone + ")";
    }
}
