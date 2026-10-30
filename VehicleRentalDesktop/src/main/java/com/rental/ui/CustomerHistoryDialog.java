package com.rental.ui;

import com.rental.dao.BookingDAO;
import com.rental.model.Booking;
import com.rental.model.Customer;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Shows one customer's full rental history. Mirrors CustomerSer's "history" page.
 */
public class CustomerHistoryDialog extends JDialog {

    public CustomerHistoryDialog(MainFrame owner, Customer c) {
        super(owner, "Rental history - " + c.getName(), true);
        setSize(820, 480);
        setLocationRelativeTo(owner);

        JPanel content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        content.setBackground(UiUtil.CARD);

        JLabel header = new JLabel("<html><b>" + c.getName() + "</b> &nbsp; " + c.getPhone()
                + " &nbsp; Licence: " + (c.hasLicence() ? c.getLicenseNo() : "NOT GIVEN") + "</html>");
        header.setFont(UiUtil.plain(14));
        content.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Booking", "Vehicle", "Pick-up", "Return", "Amount", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int cl) {
                return false;
            }
        };
        JTable table = new JTable(model);
        Tables.style(table);
        table.getColumnModel().getColumn(4).setCellRenderer(UiUtil.moneyRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtil.statusRenderer());
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            ArrayList<Booking> list;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    list = new BookingDAO().getBookingsByCustomer(c.getCustomerId());
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(CustomerHistoryDialog.this, "Database error: " + failure.getMessage());
                    return;
                }
                for (Booking b : list) {
                    model.addRow(new Object[]{"#" + b.getBookingId(), b.getRegNo() + "  " + b.getMakeModel(),
                        UiUtil.date(b.getFromDate()), UiUtil.date(b.getToDate()), UiUtil.money(b.getTotalAmt()),
                        b.isOverdue() ? "OVERDUE" : b.getStatus()});
                }
                if (list.isEmpty()) {
                    JLabel empty = new JLabel("No rentals yet.", javax.swing.SwingConstants.CENTER);
                    empty.setForeground(UiUtil.MUTED);
                    content.add(empty, BorderLayout.SOUTH);
                }
            }
        }.execute();
    }
}
