package com.rental.ui;

import com.rental.dao.VehicleDAO;
import com.rental.exception.BookingException;
import com.rental.model.Vehicle;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * Add / edit a vehicle (ADMIN only - the panel already checked that before opening this).
 * Same validation as VehicleDAO because it calls the same DAO.
 */
public class VehicleFormDialog extends JDialog {

    private final Vehicle editing;           // null when adding a new vehicle
    private final JTextField txtReg = new JTextField(20);
    private final JTextField txtModel = new JTextField(20);
    private final JComboBox<String> cbType = new JComboBox<>(Vehicle.TYPES);
    private final JTextField txtRate = new JTextField(20);
    private final JComboBox<String> cbStatus = new JComboBox<>(Vehicle.STATUSES);
    private boolean saved = false;

    public VehicleFormDialog(MainFrame owner, Vehicle existing) {
        super(owner, existing == null ? "Add vehicle" : "Edit vehicle", true);
        this.editing = existing;
        setSize(420, 400);
        setLocationRelativeTo(owner);

        JPanel form = (JPanel) getContentPane();
        form.setLayout(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(UiUtil.CARD);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 2, 0);

        c.gridy = 0;
        form.add(label("Registration no. (e.g. MH12AB1234)"), c);
        c.gridy = 1;
        form.add(txtReg, c);

        c.gridy = 2;
        form.add(label("Make / model"), c);
        c.gridy = 3;
        form.add(txtModel, c);

        c.gridy = 4;
        form.add(label("Type"), c);
        c.gridy = 5;
        form.add(cbType, c);

        c.gridy = 6;
        form.add(label("Rate per day (Rs.)"), c);
        c.gridy = 7;
        form.add(txtRate, c);

        c.gridy = 8;
        form.add(label("Status"), c);
        c.gridy = 9;
        form.add(cbStatus, c);

        c.gridy = 10;
        c.insets = new Insets(18, 0, 0, 0);
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(UiUtil.CARD);
        buttons.add(Buttons.light("Cancel", e -> dispose()));
        buttons.add(Buttons.primary(existing == null ? "Add vehicle" : "Save changes", e -> save()));
        form.add(buttons, c);

        if (existing != null) {
            txtReg.setText(existing.getRegNo());
            txtModel.setText(existing.getMakeModel());
            cbType.setSelectedItem(existing.getType());
            txtRate.setText(String.format("%.2f", existing.getRatePerDay()));
            cbStatus.setSelectedItem(existing.getStatus());
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UiUtil.MUTED);
        l.setFont(UiUtil.plain(12));
        return l;
    }

    private void save() {
        double rate;
        try {
            rate = Double.parseDouble(txtRate.getText().trim());
        } catch (NumberFormatException e) {
            UiUtil.error(this, "Rate per day must be a number");
            return;
        }

        Vehicle v = new Vehicle();
        v.setVehicleId(editing == null ? 0 : editing.getVehicleId());
        v.setRegNo(txtReg.getText());
        v.setMakeModel(txtModel.getText());
        v.setType((String) cbType.getSelectedItem());
        v.setRatePerDay(rate);
        v.setStatus((String) cbStatus.getSelectedItem());

        new SwingWorker<Void, Void>() {
            BookingException businessError;
            SQLException dbError;

            @Override
            protected Void doInBackground() {
                VehicleDAO dao = new VehicleDAO();
                try {
                    if (editing == null) {
                        dao.addVehicle(v);
                    } else {
                        dao.updateVehicle(v);
                    }
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
                    UiUtil.error(VehicleFormDialog.this, businessError.getMessage());
                } else if (dbError != null) {
                    UiUtil.error(VehicleFormDialog.this, "Database error: " + dbError.getMessage());
                } else {
                    saved = true;
                    UiUtil.info(VehicleFormDialog.this,
                            (editing == null ? "Vehicle added" : "Vehicle updated") + ": " + v.getRegNo());
                    dispose();
                }
            }
        }.execute();
    }

    public boolean wasSaved() {
        return saved;
    }
}
