package com.rental.model;

/**
 * A vehicle in the rental fleet (one row of the vehicles table).
 */
public class Vehicle {

    public static final String[] TYPES = {"CAR", "SUV", "BIKE", "VAN"};
    public static final String[] STATUSES = {"AVAILABLE", "MAINTENANCE"};

    private int vehicleId;
    private String regNo;
    private String makeModel;
    private String type;          // CAR, SUV, BIKE or VAN
    private double ratePerDay;
    private String status;        // AVAILABLE or MAINTENANCE

    public Vehicle() {
    }

    public Vehicle(int vehicleId, String regNo, String makeModel, String type,
                   double ratePerDay, String status) {
        this.vehicleId = vehicleId;
        this.regNo = regNo;
        this.makeModel = makeModel;
        this.type = type;
        this.ratePerDay = ratePerDay;
        this.status = status;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getRatePerDay() {
        return ratePerDay;
    }

    public void setRatePerDay(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** true when the vehicle is not under maintenance. */
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    /** Text shown in drop-down lists. */
    @Override
    public String toString() {
        String text = regNo + "  " + makeModel + "  (" + type + ", Rs. "
                + String.format("%.0f", ratePerDay) + "/day)";
        if (!isAvailable()) {
            text = text + "  [" + status + "]";
        }
        return text;
    }
}
