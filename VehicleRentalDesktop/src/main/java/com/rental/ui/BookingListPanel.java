package com.rental.ui;

import com.rental.dao.BookingDAO;
import com.rental.exception.BookingException;
import com.rental.model.Booking;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
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
 * All bookings, filterable by status, with cancel.
 * Mirrors the web app's BookSer?action=list.
 */
public class BookingListPanel extends JPanel {

    private static final String[] STATUSES = {"ALL", "BOOKED", "RETURNED", "CANCELLED"};

    private final JComboBox<String> cbStatus = new JComboBox<>(STATUSES);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Booking", "Customer", "Vehicle", "Pick-up", "Return", "Amount", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private ArrayList<Booking> rows = new ArrayList<>();

    public BookingListPanel(MainFrame main) {
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtil.BG);

        JLabel title = new JLabel("Bookings");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        top.add(title);
        top.add(Box.createVerticalStrut(14));

        JPanel bar = Cards.plain();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setAlignmentX(0);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        bar.add(new JLabel("Show"));
        bar.add(cbStatus);
        bar.add(Buttons.primary("Filter", e -> refresh()));
        bar.add(Buttons.primary("+ New booking", e -> main.show("book")));
        top.add(bar);
        add(top, BorderLayout.NORTH);

        Tables.style(table);
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtil.moneyRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(UiUtil.statusRenderer());

        JPanel tableCard = Cards.plain();
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(UiUtil.CARD);
        actions.add(Buttons.danger("Cancel selected booking", e -> cancelSelected()));
        tableCard.add(actions, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= rows.size()) {
            UiUtil.error(this, "Select a booking first");
            return;
        }
        Booking b = rows.get(row);
        if (!UiUtil.confirm(this, "Cancel booking #" + b.getBookingId() + "?")) {
            return;
        }
        new SwingWorker<Void, Void>() {
            BookingException businessError;
            SQLException dbError;

            @Override
            protected Void doInBackground() {
                try {
                    new BookingDAO().cancel(b.getBookingId());
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
                    UiUtil.error(BookingListPanel.this, businessError.getMessage());
                } else if (dbError != null) {
                    UiUtil.error(BookingListPanel.this, "Database error: " + dbError.getMessage());
                } else {
                    UiUtil.info(BookingListPanel.this, "Booking #" + b.getBookingId() + " cancelled");
                    refresh();
                }
            }
        }.execute();
    }

    public void refresh() {
        String status = (String) cbStatus.getSelectedItem();
        new SwingWorker<Void, Void>() {
            ArrayList<Booking> list;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    list = new BookingDAO().getBookings(status);
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(BookingListPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                rows = list;
                model.setRowCount(0);
                for (Booking b : list) {
                    model.addRow(new Object[]{"#" + b.getBookingId(), b.getCustomerName(),
                        b.getRegNo() + "  " + b.getMakeModel(), UiUtil.date(b.getFromDate()),
                        UiUtil.date(b.getToDate()), UiUtil.money(b.getTotalAmt()),
                        b.isOverdue() ? "OVERDUE" : b.getStatus()});
                }
            }
        }.execute();
    }
}
