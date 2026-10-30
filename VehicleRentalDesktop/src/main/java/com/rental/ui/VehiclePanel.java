package com.rental.ui;

import com.rental.dao.VehicleDAO;
import com.rental.model.User;
import com.rental.model.Vehicle;
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
 * Vehicles: list with type / status filters (everyone), add / edit (ADMIN only).
 * Mirrors the web app's VehicleSer.
 */
public class VehiclePanel extends JPanel {

    private static final String ADMIN_ONLY = "Only ADMIN can add or edit vehicles";

    private final MainFrame main;
    private final User user;
    private final JComboBox<String> cbType = new JComboBox<>(prepend("ALL", Vehicle.TYPES));
    private final JComboBox<String> cbStatus = new JComboBox<>(prepend("ALL", Vehicle.STATUSES));
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Reg. no.", "Make / model", "Type", "Rate / day", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private ArrayList<Vehicle> rows = new ArrayList<>();

    public VehiclePanel(MainFrame main, User user) {
        this.main = main;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtil.BG);

        JLabel title = new JLabel("Vehicles");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        top.add(title);
        top.add(Box.createVerticalStrut(14));

        JPanel bar = Cards.plain();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setAlignmentX(0);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        bar.add(new JLabel("Type"));
        bar.add(cbType);
        bar.add(new JLabel("Status"));
        bar.add(cbStatus);
        bar.add(Buttons.primary("Filter", e -> refresh()));
        bar.add(Buttons.primary("+ Add vehicle", e -> openForm(null)));
        top.add(bar);
        add(top, BorderLayout.NORTH);

        Tables.style(table);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(4).setCellRenderer(UiUtil.moneyRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtil.statusRenderer());

        JPanel tableCard = Cards.plain();
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(UiUtil.CARD);
        actions.add(Buttons.smallLight("Edit selected", e -> editSelected()));
        JLabel note = new JLabel("  Only ADMIN can add or edit vehicles.");
        note.setForeground(UiUtil.MUTED);
        actions.add(note);
        tableCard.add(actions, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private static String[] prepend(String first, String[] rest) {
        String[] out = new String[rest.length + 1];
        out[0] = first;
        System.arraycopy(rest, 0, out, 1, rest.length);
        return out;
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= rows.size()) {
            UiUtil.error(this, "Select a vehicle first");
            return;
        }
        openForm(rows.get(row));
    }

    private void openForm(Vehicle existing) {
        if (!user.isAdmin()) {
            UiUtil.error(this, ADMIN_ONLY);
            return;
        }
        VehicleFormDialog dlg = new VehicleFormDialog(main, existing);
        dlg.setVisible(true);
        if (dlg.wasSaved()) {
            refresh();
        }
    }

    public void refresh() {
        String type = (String) cbType.getSelectedItem();
        String status = (String) cbStatus.getSelectedItem();
        new SwingWorker<Void, Void>() {
            ArrayList<Vehicle> list;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    list = new VehicleDAO().getVehicles(type, status);
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(VehiclePanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                rows = list;
                model.setRowCount(0);
                for (Vehicle v : list) {
                    model.addRow(new Object[]{"#" + v.getVehicleId(), v.getRegNo(), v.getMakeModel(),
                        v.getType(), UiUtil.money(v.getRatePerDay()), v.getStatus()});
                }
            }
        }.execute();
    }
}
