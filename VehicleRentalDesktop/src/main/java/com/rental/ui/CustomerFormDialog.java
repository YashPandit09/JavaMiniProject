package com.rental.ui;

import com.rental.dao.CustomerDAO;
import com.rental.exception.BookingException;
import com.rental.model.Customer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * Register / edit a customer. Same validation as CustomerDAO (phone must be
 * 10 digits, licence 5-20 letters/digits) because it calls the same DAO.
 */
public class CustomerFormDialog extends JDialog {

    private final Customer editing;          // null when registering a new customer
    private final JTextField txtName = new JTextField(22);
    private final JTextField txtPhone = new JTextField(22);
    private final JTextField txtLicence = new JTextField(22);
    private boolean saved = false;

    public CustomerFormDialog(MainFrame owner, Customer existing) {
        super(owner, existing == null ? "Register customer" : "Edit customer", true);
        this.editing = existing;
        setSize(420, 300);
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
        form.add(label("Full name"), c);
        c.gridy = 1;
        form.add(txtName, c);

        c.gridy = 2;
        form.add(label("Phone (10 digits)"), c);
        c.gridy = 3;
        form.add(txtPhone, c);

        c.gridy = 4;
        form.add(label("Driving licence no. (needed to book)"), c);
        c.gridy = 5;
        form.add(txtLicence, c);

        c.gridy = 6;
        c.insets = new Insets(18, 0, 0, 0);
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.setBackground(UiUtil.CARD);
        buttons.add(Buttons.light("Cancel", e -> dispose()));
        buttons.add(Buttons.primary(existing == null ? "Register" : "Save changes", e -> save()));
        form.add(buttons, c);

        if (existing != null) {
            txtName.setText(existing.getName());
            txtPhone.setText(existing.getPhone());
            txtLicence.setText(existing.getLicenseNo());
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UiUtil.MUTED);
        l.setFont(UiUtil.plain(12));
        return l;
    }

    private void save() {
        Customer c = new Customer();
        c.setCustomerId(editing == null ? 0 : editing.getCustomerId());
        c.setName(txtName.getText());
        c.setPhone(txtPhone.getText());
        c.setLicenseNo(txtLicence.getText());

        new SwingWorker<Void, Void>() {
            BookingException businessError;
            SQLException dbError;

            @Override
            protected Void doInBackground() {
                CustomerDAO dao = new CustomerDAO();
                try {
                    if (editing == null) {
                        dao.addCustomer(c);
                    } else {
                        dao.updateCustomer(c);
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
                    UiUtil.error(CustomerFormDialog.this, businessError.getMessage());
                } else if (dbError != null) {
                    UiUtil.error(CustomerFormDialog.this, "Database error: " + dbError.getMessage());
                } else {
                    saved = true;
                    UiUtil.info(CustomerFormDialog.this,
                            (editing == null ? "Customer registered" : "Customer updated") + ": " + c.getName());
                    dispose();
                }
            }
        }.execute();
    }

    public boolean wasSaved() {
        return saved;
    }
}
