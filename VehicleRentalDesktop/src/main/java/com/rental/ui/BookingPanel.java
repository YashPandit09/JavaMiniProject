package com.rental.ui;

import com.rental.dao.BookingDAO;
import com.rental.dao.CustomerDAO;
import com.rental.dao.VehicleDAO;
import com.rental.exception.BookingException;
import com.rental.model.Booking;
import com.rental.model.Customer;
import com.rental.model.Vehicle;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Date;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * New booking: 1. search free vehicles for given dates, 2. book.
 * BookingDAO.book() does every check inside one JDBC transaction.
 * Mirrors the web app's BookSer (GET).
 */
public class BookingPanel extends JPanel {

    private final MainFrame main;

    // ---- search ----
    private final JSpinner spSearchFrom = dateSpinner(LocalDate.now());
    private final JSpinner spSearchTo = dateSpinner(LocalDate.now().plusDays(1));
    private final JComboBox<String> cbSearchType = new JComboBox<>(prepend("ALL", Vehicle.TYPES));
    private final DefaultTableModel freeModel = new DefaultTableModel(
            new Object[]{"Reg. no.", "Make / model", "Type", "Rate / day", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable freeTable = new JTable(freeModel);
    private ArrayList<Vehicle> freeVehicles = new ArrayList<>();

    // ---- book form ----
    private final JComboBox<Customer> cbCustomer = new JComboBox<>();
    private final JComboBox<Vehicle> cbVehicle = new JComboBox<>();
    private final JSpinner spFrom = dateSpinner(LocalDate.now());
    private final JSpinner spTo = dateSpinner(LocalDate.now().plusDays(1));

    public BookingPanel(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(UiUtil.BG);

        JLabel title = new JLabel("New booking");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        wrap.add(title);
        wrap.add(Box.createVerticalStrut(14));

        wrap.add(searchCard());
        wrap.add(Box.createVerticalStrut(14));
        wrap.add(bookCard());

        add(new JScrollPane(wrap), BorderLayout.CENTER);
    }

    private JPanel searchCard() {
        JPanel card = Cards.titled("1. Find free vehicles", buildSearchContent());
        card.setAlignmentX(0);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        return card;
    }

    private JPanel buildSearchContent() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(UiUtil.CARD);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(UiUtil.CARD);
        bar.add(new JLabel("Pick-up"));
        bar.add(spSearchFrom);
        bar.add(new JLabel("Return"));
        bar.add(spSearchTo);
        bar.add(new JLabel("Type"));
        bar.add(cbSearchType);
        bar.add(Buttons.primary("Search", e -> search()));
        content.add(bar, BorderLayout.NORTH);

        Tables.style(freeTable);
        freeTable.getColumnModel().getColumn(3).setCellRenderer(UiUtil.moneyRenderer());
        freeTable.getColumnModel().getColumn(4).setCellRenderer(UiUtil.moneyRenderer());
        content.add(new JScrollPane(freeTable), BorderLayout.CENTER);

        JPanel pick = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pick.setBackground(UiUtil.CARD);
        pick.add(Buttons.smallLight("Use selected vehicle below", e -> useSelectedFreeVehicle()));
        content.add(pick, BorderLayout.SOUTH);
        return content;
    }

    private JPanel bookCard() {
        JPanel content = new JPanel(new java.awt.GridBagLayout());
        content.setBackground(UiUtil.CARD);
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new java.awt.Insets(4, 8, 4, 8);
        c.anchor = java.awt.GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        content.add(new JLabel("Customer"), c);
        c.gridx = 1;
        cbCustomer.setPreferredSize(new Dimension(260, 28));
        content.add(cbCustomer, c);

        c.gridx = 2;
        content.add(new JLabel("Vehicle"), c);
        c.gridx = 3;
        cbVehicle.setPreferredSize(new Dimension(260, 28));
        content.add(cbVehicle, c);

        c.gridx = 0;
        c.gridy = 1;
        content.add(new JLabel("Pick-up"), c);
        c.gridx = 1;
        content.add(spFrom, c);
        c.gridx = 2;
        content.add(new JLabel("Return"), c);
        c.gridx = 3;
        content.add(spTo, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        content.add(Buttons.primary("Confirm booking", e -> book()), c);

        JPanel card = Cards.titled("2. Book", content);
        card.setAlignmentX(0);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        return card;
    }

    private static String[] prepend(String first, String[] rest) {
        String[] out = new String[rest.length + 1];
        out[0] = first;
        System.arraycopy(rest, 0, out, 1, rest.length);
        return out;
    }

    private static JSpinner dateSpinner(LocalDate initial) {
        java.util.Date d = java.sql.Date.valueOf(initial);
        SpinnerDateModel model = new SpinnerDateModel(d, null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "dd-MM-yyyy"));
        sp.setPreferredSize(new Dimension(120, 26));
        return sp;
    }

    private static Date spinnerDate(JSpinner sp) {
        java.util.Date d = (java.util.Date) sp.getValue();
        return new Date(d.getTime());
    }

    private void search() {
        Date from = spinnerDate(spSearchFrom);
        Date to = spinnerDate(spSearchTo);
        String type = (String) cbSearchType.getSelectedItem();
        try {
            BookingDAO.checkDates(from, to);
        } catch (BookingException e) {
            UiUtil.error(this, e.getMessage());
            return;
        }

        new SwingWorker<Void, Void>() {
            ArrayList<Vehicle> list;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    list = new VehicleDAO().findFreeVehicles(from, to, type);
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(BookingPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                freeVehicles = list;
                long days = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());
                freeModel.setRowCount(0);
                for (Vehicle v : list) {
                    freeModel.addRow(new Object[]{v.getRegNo(), v.getMakeModel(), v.getType(),
                        UiUtil.money(v.getRatePerDay()), UiUtil.money(days * v.getRatePerDay())});
                }
                if (list.isEmpty()) {
                    UiUtil.info(BookingPanel.this, "No free vehicles for these dates.");
                }
            }
        }.execute();
    }

    private void useSelectedFreeVehicle() {
        int row = freeTable.getSelectedRow();
        if (row < 0 || row >= freeVehicles.size()) {
            UiUtil.error(this, "Select a vehicle in the table above first");
            return;
        }
        Vehicle v = freeVehicles.get(row);
        spFrom.setValue(spSearchFrom.getValue());
        spTo.setValue(spSearchTo.getValue());
        for (int i = 0; i < cbVehicle.getItemCount(); i++) {
            if (cbVehicle.getItemAt(i).getVehicleId() == v.getVehicleId()) {
                cbVehicle.setSelectedIndex(i);
                break;
            }
        }
    }

    private void book() {
        Customer c = (Customer) cbCustomer.getSelectedItem();
        Vehicle v = (Vehicle) cbVehicle.getSelectedItem();
        if (c == null || v == null) {
            UiUtil.error(this, "Please choose a customer and a vehicle");
            return;
        }
        Date from = spinnerDate(spFrom);
        Date to = spinnerDate(spTo);

        new SwingWorker<Void, Void>() {
            int bookingId;
            Booking booked;
            BookingException businessError;
            SQLException dbError;

            @Override
            protected Void doInBackground() {
                BookingDAO dao = new BookingDAO();
                try {
                    bookingId = dao.book(c.getCustomerId(), v.getVehicleId(), from, to);
                    booked = dao.getBooking(bookingId);
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
                    UiUtil.error(BookingPanel.this, businessError.getMessage());
                } else if (dbError != null) {
                    UiUtil.error(BookingPanel.this, "Database error: " + dbError.getMessage());
                } else {
                    UiUtil.info(BookingPanel.this, "Booking #" + bookingId + " confirmed\n"
                            + booked.getCustomerName() + "  |  " + booked.getRegNo() + " " + booked.getMakeModel()
                            + "\n" + UiUtil.date(booked.getFromDate()) + " to " + UiUtil.date(booked.getToDate())
                            + "  (" + booked.getBookedDays() + " days)\nEstimated amount "
                            + UiUtil.money(booked.getTotalAmt()));
                    main.show("bookings");
                }
            }
        }.execute();
    }

    public void refresh() {
        new SwingWorker<Void, Void>() {
            ArrayList<Customer> customers;
            ArrayList<Vehicle> vehicles;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    customers = new CustomerDAO().getAllCustomers();
                    vehicles = new VehicleDAO().getAllVehicles();
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(BookingPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                cbCustomer.removeAllItems();
                for (Customer c : customers) {
                    cbCustomer.addItem(c);
                }
                cbVehicle.removeAllItems();
                for (Vehicle v : vehicles) {
                    cbVehicle.addItem(v);
                }
            }
        }.execute();
    }
}
