package com.rental.ui;

import com.rental.dao.UserDAO;
import com.rental.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * Staff login screen - the first window shown.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtId = new JTextField(18);
    private final JPasswordField txtPwd = new JPasswordField(18);
    private final JButton btnLogin = new JButton("Login");
    private final JLabel lblError = new JLabel(" ");

    public LoginFrame() {
        super("Vehicle Rental - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(UiUtil.NAVY);
        setLayout(new GridBagLayout());
        add(buildCard());

        getRootPane().setDefaultButton(btnLogin);
        btnLogin.addActionListener(e -> login());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UiUtil.CARD);
        card.setBorder(new EmptyBorder(28, 28, 28, 28));
        card.setPreferredSize(new Dimension(360, 400));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        JLabel title = new JLabel("Vehicle Rental");
        title.setFont(UiUtil.bold(22));
        c.gridy = 0;
        card.add(title, c);

        JLabel sub = new JLabel("Staff login");
        sub.setForeground(UiUtil.MUTED);
        c.gridy = 1;
        c.insets = new Insets(2, 0, 18, 0);
        card.add(sub, c);

        c.insets = new Insets(10, 0, 2, 0);
        JLabel lblId = new JLabel("Username");
        lblId.setForeground(UiUtil.MUTED);
        c.gridy = 2;
        card.add(lblId, c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 2, 0);
        card.add(txtId, c);

        c.gridy = 4;
        c.insets = new Insets(10, 0, 2, 0);
        JLabel lblPwd = new JLabel("Password");
        lblPwd.setForeground(UiUtil.MUTED);
        card.add(lblPwd, c);

        c.gridy = 5;
        c.insets = new Insets(0, 0, 2, 0);
        card.add(txtPwd, c);

        c.gridy = 6;
        c.insets = new Insets(16, 0, 4, 0);
        btnLogin.setBackground(UiUtil.TEAL);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(UiUtil.bold(14));
        btnLogin.setFocusPainted(false);
        card.add(btnLogin, c);

        c.gridy = 7;
        lblError.setForeground(UiUtil.RED_FG);
        lblError.setFont(UiUtil.plain(12));
        card.add(lblError, c);

        c.gridy = 8;
        c.insets = new Insets(18, 0, 0, 0);
        JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hint.setBackground(UiUtil.BG);
        hint.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel hintText = new JLabel("<html>Demo logins: <b>admin</b> / admin123 (ADMIN)<br><b>staff</b> / staff123 (STAFF)</html>");
        hintText.setForeground(UiUtil.MUTED);
        hintText.setFont(UiUtil.plain(12));
        hint.add(hintText);
        card.add(hint, c);

        return card;
    }

    private void login() {
        String id = txtId.getText().trim();
        String pwd = new String(txtPwd.getPassword());
        if (id.isEmpty() || pwd.isEmpty()) {
            lblError.setText("Enter both username and password");
            return;
        }
        btnLogin.setEnabled(false);
        btnLogin.setText("Checking...");
        lblError.setText(" ");

        // keep the DB call off the Swing event thread
        new SwingWorker<User, Void>() {
            SQLException failure;

            @Override
            protected User doInBackground() {
                try {
                    return new UserDAO().login(id, pwd);
                } catch (SQLException e) {
                    failure = e;
                    return null;
                }
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                if (failure != null) {
                    lblError.setText("Database error: " + failure.getMessage());
                    return;
                }
                try {
                    User u = get();
                    if (u == null) {
                        lblError.setText("Invalid username / password");
                    } else {
                        new MainFrame(u).setVisible(true);
                        dispose();
                    }
                } catch (Exception ex) {
                    lblError.setText("Unexpected error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
