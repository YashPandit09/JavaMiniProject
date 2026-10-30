package com.rental.ui;

import com.rental.dao.CustomerDAO;
import com.rental.model.Customer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Customers: search / list, register, edit, rental history.
 * Mirrors the web app's CustomerSer.
 */
public class CustomerPanel extends JPanel {

    private final MainFrame main;
    private final JTextField txtSearch = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Phone", "Licence no."}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private ArrayList<Customer> rows = new ArrayList<>();

    public CustomerPanel(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtil.BG);

        JLabel title = new JLabel("Customers");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        top.add(title);
        top.add(Box.createVerticalStrut(14));

        JPanel searchBar = Cards.plain();
        searchBar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        searchBar.setAlignmentX(0);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        searchBar.add(new JLabel("Search by name, phone or licence:"));
        searchBar.add(txtSearch);
        searchBar.add(Buttons.primary("Search", e -> refresh()));
        searchBar.add(Buttons.light("Show all", e -> {
            txtSearch.setText("");
            refresh();
        }));
        searchBar.add(Buttons.primary("+ Register customer", e -> openForm(null)));
        top.add(searchBar);

        add(top, BorderLayout.NORTH);

        Tables.style(table);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel tableCard = Cards.plain();
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        actions.setBackground(UiUtil.CARD);
        actions.add(Buttons.smallLight("Edit selected", e -> editSelected()));
        actions.add(Buttons.smallLight("View history", e -> historySelected()));
        tableCard.add(actions, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private Customer selected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= rows.size()) {
            UiUtil.error(this, "Select a customer first");
            return null;
        }
        return rows.get(row);
    }

    private void editSelected() {
        Customer c = selected();
        if (c != null) {
            openForm(c);
        }
    }

    private void historySelected() {
        Customer c = selected();
        if (c != null) {
            new CustomerHistoryDialog(main, c).setVisible(true);
        }
    }

    private void openForm(Customer existing) {
        CustomerFormDialog dlg = new CustomerFormDialog(main, existing);
        dlg.setVisible(true);
        if (dlg.wasSaved()) {
            refresh();
        }
    }

    public void refresh() {
        String q = txtSearch.getText();
        new SwingWorker<Void, Void>() {
            ArrayList<Customer> list;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    list = new CustomerDAO().searchCustomers(q);
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(CustomerPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                rows = list;
                model.setRowCount(0);
                for (Customer c : list) {
                    model.addRow(new Object[]{"#" + c.getCustomerId(), c.getName(), c.getPhone(),
                        c.hasLicence() ? c.getLicenseNo() : "NO LICENCE"});
                }
            }
        }.execute();
    }
}
