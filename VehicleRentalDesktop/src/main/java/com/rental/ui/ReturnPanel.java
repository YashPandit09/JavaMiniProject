package com.rental.ui;

import com.rental.dao.BookingDAO;
import com.rental.dao.VehicleDAO;
import com.rental.exception.BookingException;
import com.rental.model.Booking;
import com.rental.model.Vehicle;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Return & Billing: close the rental of a vehicle, bill = days x rate, save the payment.
 * Mirrors the web app's ReturnSer.
 */
public class ReturnPanel extends JPanel {

    private final JComboBox<Vehicle> cbVehicle = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Booking", "Customer", "Vehicle", "Pick-up", "Due back", "Bill if returned today", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private ArrayList<Booking> rows = new ArrayList<>();

    public ReturnPanel(MainFrame main) {
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtil.BG);

        JLabel title = new JLabel("Return & Billing");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        top.add(title);
        top.add(Box.createVerticalStrut(14));

        JPanel bar = Cards.plain();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setAlignmentX(0);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        bar.add(new JLabel("Vehicle"));
        cbVehicle.setPreferredSize(new Dimension(300, 26));
        bar.add(cbVehicle);
        bar.add(Buttons.primary("Return & bill", e -> returnVehicle()));
        JLabel note = new JLabel("  Bill = days from pick-up to today (at least 1) x rate per day.");
        note.setForeground(UiUtil.MUTED);
        bar.add(note);
        top.add(bar);
        add(top, BorderLayout.NORTH);

        Tables.style(table);
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtil.moneyRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(UiUtil.statusRenderer());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectRowInCombo();
                }
            }
        });
        add(Cards.titled("Out on rent now - double-click a row to select its vehicle above", new JScrollPane(table)),
                BorderLayout.CENTER);
    }

    private void selectRowInCombo() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= rows.size()) {
            return;
        }
        int vehicleId = rows.get(row).getVehicleId();
        for (int i = 0; i < cbVehicle.getItemCount(); i++) {
            if (cbVehicle.getItemAt(i).getVehicleId() == vehicleId) {
                cbVehicle.setSelectedIndex(i);
                break;
            }
        }
    }

    private void returnVehicle() {
        Vehicle v = (Vehicle) cbVehicle.getSelectedItem();
        if (v == null) {
            UiUtil.error(this, "Please choose a vehicle");
            return;
        }
        new SwingWorker<Void, Void>() {
            Booking bill;
            BookingException businessError;
            SQLException dbError;

            @Override
            protected Void doInBackground() {
                try {
                    bill = new BookingDAO().returnVehicle(v.getVehicleId());
                } catch (BookingException e) {
                    businessError = e;
                } catch (SQLException e) {
                    dbError = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (businessError != null) {
                    UiUtil.error(ReturnPanel.this, businessError.getMessage());
                } else if (dbError != null) {
                    UiUtil.error(ReturnPanel.this, "Database error: " + dbError.getMessage());
                } else {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Vehicle ").append(bill.getRegNo()).append(" returned\n\n");
                    msg.append("Booking #").append(bill.getBookingId()).append("\n");
                    msg.append("Customer: ").append(bill.getCustomerName()).append("\n");
                    msg.append("Pick-up: ").append(UiUtil.date(bill.getFromDate())).append("\n");
                    msg.append("Returned on: ").append(UiUtil.date(bill.getReturnDate())).append("\n");
                    if (bill.getReturnDate().after(bill.getToDate())) {
                        long late = bill.getDays() - bill.getBookedDays();
                        msg.append("Late by: ").append(late).append(" day(s)\n");
                    }
                    msg.append(bill.getDays()).append(" x ").append(UiUtil.money(bill.getRatePerDay())).append("\n");
                    msg.append("Amount paid: ").append(UiUtil.money(bill.getTotalAmt()));
                    UiUtil.info(ReturnPanel.this, msg.toString());
                    refresh();
                }
            }
        }.execute();
    }

    public void refresh() {
        new SwingWorker<Void, Void>() {
            ArrayList<Vehicle> vehicles;
            ArrayList<Booking> onRent;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    vehicles = new VehicleDAO().getAllVehicles();
                    onRent = new BookingDAO().getActiveRentals();
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(ReturnPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                cbVehicle.removeAllItems();
                for (Vehicle v : vehicles) {
                    cbVehicle.addItem(v);
                }

                rows = onRent;
                model.setRowCount(0);
                for (Booking b : onRent) {
                    long days = Math.max(1, ChronoUnit.DAYS.between(b.getFromDate().toLocalDate(), LocalDate.now()));
                    model.addRow(new Object[]{"#" + b.getBookingId(), b.getCustomerName(),
                        b.getRegNo() + "  " + b.getMakeModel(), UiUtil.date(b.getFromDate()),
                        UiUtil.date(b.getToDate()), UiUtil.money(days * b.getRatePerDay()),
                        b.isOverdue() ? "OVERDUE" : "BOOKED"});
                }
            }
        }.execute();
    }
}
