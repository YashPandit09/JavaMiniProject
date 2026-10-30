package com.rental.ui;

import com.rental.dao.BookingDAO;
import com.rental.dao.CustomerDAO;
import com.rental.dao.VehicleDAO;
import com.rental.model.Booking;
import com.rental.model.Payment;
import com.rental.model.Vehicle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Home screen after login: stat tiles, "on rent now" and "upcoming pick-ups".
 * Mirrors the web app's DashboardSer.
 */
public class DashboardPanel extends JPanel {

    private final MainFrame main;
    private final JPanel stats = new JPanel(new GridLayout(2, 4, 12, 12));
    private final DefaultTableModel onRentModel = new DefaultTableModel(
            new Object[]{"Booking", "Customer", "Vehicle", "Pick-up", "Due back", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel upcomingModel = new DefaultTableModel(
            new Object[]{"Booking", "Customer", "Vehicle", "Pick-up", "Return", "Amount"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public DashboardPanel(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtil.BG);

        JLabel title = new JLabel("Dashboard");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        top.add(title);
        top.add(Box.createVerticalStrut(14));

        stats.setBackground(UiUtil.BG);
        stats.setAlignmentX(0);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        top.add(stats);
        top.add(Box.createVerticalStrut(14));

        JPanel actions = new JPanel();
        actions.setBackground(UiUtil.BG);
        actions.setAlignmentX(0);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setBorder(UiUtil.cardBorder());
        actions.setOpaque(true);
        actions.setBackground(UiUtil.CARD);
        actions.add(Buttons.primary("New booking", e -> main.show("book")));
        actions.add(Box.createHorizontalStrut(10));
        actions.add(Buttons.light("Return a vehicle", e -> main.show("return")));
        actions.add(Box.createHorizontalStrut(10));
        actions.add(Buttons.light("Register customer", e -> main.show("customers")));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        top.add(actions);

        add(top, BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 14));
        tables.setBackground(UiUtil.BG);
        tables.setBorder(new EmptyBorder(14, 0, 0, 0));
        tables.add(Cards.titled("On rent now", onRentTable()));
        tables.add(Cards.titled("Upcoming pick-ups", upcomingTable()));
        add(tables, BorderLayout.CENTER);
    }

    private JScrollPane onRentTable() {
        JTable t = new JTable(onRentModel);
        Tables.style(t);
        t.getColumnModel().getColumn(5).setCellRenderer(UiUtil.statusRenderer());
        return new JScrollPane(t);
    }

    private JScrollPane upcomingTable() {
        JTable t = new JTable(upcomingModel);
        Tables.style(t);
        t.getColumnModel().getColumn(5).setCellRenderer(UiUtil.moneyRenderer());
        return new JScrollPane(t);
    }

    public void refresh() {
        new SwingWorker<Void, Void>() {
            ArrayList<Vehicle> vehicles;
            ArrayList<Booking> onRent;
            ArrayList<Booking> upcoming;
            int customers;
            double monthRevenue;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    BookingDAO bdao = new BookingDAO();
                    vehicles = new VehicleDAO().getAllVehicles();
                    onRent = bdao.getActiveRentals();
                    upcoming = bdao.getUpcomingBookings();
                    customers = new CustomerDAO().getAllCustomers().size();
                    LocalDate today = LocalDate.now();
                    ArrayList<Payment> monthPayments =
                            bdao.getPayments(Date.valueOf(today.withDayOfMonth(1)), Date.valueOf(today));
                    monthRevenue = monthPayments.stream().mapToDouble(Payment::getAmount).sum();
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(DashboardPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                Set<Integer> onRentIds = onRent.stream().map(Booking::getVehicleId).collect(Collectors.toSet());
                long ready = vehicles.stream().filter(v -> v.isAvailable() && !onRentIds.contains(v.getVehicleId())).count();
                long maintenance = vehicles.stream().filter(v -> !v.isAvailable()).count();
                long overdue = onRent.stream().filter(Booking::isOverdue).count();

                stats.removeAll();
                stats.add(Tiles.hero("Revenue this month", UiUtil.money(monthRevenue)));
                stats.add(Tiles.plain("Ready to rent", String.valueOf(ready)));
                stats.add(Tiles.plain("On rent now", String.valueOf(onRent.size())));
                stats.add(Tiles.plain("Overdue returns", String.valueOf(overdue)));
                stats.add(Tiles.plain("Upcoming bookings", String.valueOf(upcoming.size())));
                stats.add(Tiles.plain("In maintenance", String.valueOf(maintenance)));
                stats.add(Tiles.plain("Vehicles in fleet", String.valueOf(vehicles.size())));
                stats.add(Tiles.plain("Customers", String.valueOf(customers)));
                stats.revalidate();
                stats.repaint();

                onRentModel.setRowCount(0);
                for (Booking b : onRent) {
                    onRentModel.addRow(new Object[]{"#" + b.getBookingId(), b.getCustomerName(),
                        b.getRegNo() + "  " + b.getMakeModel(), UiUtil.date(b.getFromDate()),
                        UiUtil.date(b.getToDate()), b.isOverdue() ? "OVERDUE" : "BOOKED"});
                }
                upcomingModel.setRowCount(0);
                for (Booking b : upcoming) {
                    upcomingModel.addRow(new Object[]{"#" + b.getBookingId(), b.getCustomerName(),
                        b.getRegNo() + "  " + b.getMakeModel(), UiUtil.date(b.getFromDate()),
                        UiUtil.date(b.getToDate()), UiUtil.money(b.getTotalAmt())});
                }
            }
        }.execute();
    }
}
