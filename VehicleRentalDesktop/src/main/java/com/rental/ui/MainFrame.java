package com.rental.ui;

import com.rental.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Main window shown after login: a navy sidebar (like the web app's top bar)
 * and a card-layout content area, one card per module.
 */
public class MainFrame extends JFrame {

    private final User user;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel sidebar = new JPanel();

    private DashboardPanel dashboardPanel;
    private CustomerPanel customerPanel;
    private VehiclePanel vehiclePanel;
    private BookingPanel bookingPanel;
    private BookingListPanel bookingListPanel;
    private ReturnPanel returnPanel;
    private ReportPanel reportPanel;

    public MainFrame(User user) {
        super("Vehicle Rental Management System");
        this.user = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        buildSidebar();
        add(sidebar, BorderLayout.WEST);

        content.setBackground(UiUtil.BG);
        add(content, BorderLayout.CENTER);
        buildPanels();

        show("dashboard");
    }

    private void buildSidebar() {
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UiUtil.NAVY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(18, 0, 18, 0));

        JLabel brand = new JLabel("Vehicle Rental");
        brand.setForeground(Color.WHITE);
        brand.setFont(UiUtil.bold(18));
        brand.setAlignmentX(0.5f);
        sidebar.add(brand);

        JLabel sub = new JLabel("Management System");
        sub.setForeground(UiUtil.AMBER);
        sub.setFont(UiUtil.plain(12));
        sub.setAlignmentX(0.5f);
        sidebar.add(sub);

        sidebar.add(Box.createVerticalStrut(20));
        navButton("Dashboard", "dashboard", () -> dashboardPanel.refresh());
        navButton("Customers", "customers", () -> customerPanel.refresh());
        navButton("Vehicles", "vehicles", () -> vehiclePanel.refresh());
        navButton("New Booking", "book", () -> bookingPanel.refresh());
        navButton("Bookings", "bookings", () -> bookingListPanel.refresh());
        navButton("Return & Billing", "return", () -> returnPanel.refresh());
        navButton("Reports", "reports", () -> reportPanel.refresh());

        sidebar.add(Box.createVerticalGlue());

        JPanel who = new JPanel();
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        who.setBackground(UiUtil.NAVY);
        who.setAlignmentX(0.5f);
        JLabel name = new JLabel(user.getUsername());
        name.setForeground(Color.WHITE);
        name.setFont(UiUtil.bold(13));
        name.setAlignmentX(0.5f);
        JLabel role = new JLabel(user.getRole());
        role.setOpaque(true);
        role.setBackground(UiUtil.TEAL);
        role.setForeground(Color.WHITE);
        role.setFont(UiUtil.plain(11));
        role.setHorizontalAlignment(SwingConstants.CENTER);
        role.setBorder(new EmptyBorder(2, 8, 2, 8));
        role.setAlignmentX(0.5f);
        who.add(name);
        who.add(Box.createVerticalStrut(4));
        who.add(role);
        sidebar.add(who);

        sidebar.add(Box.createVerticalStrut(10));
        JButton logout = new JButton("Logout");
        logout.setForeground(UiUtil.AMBER);
        logout.setBackground(UiUtil.NAVY);
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setAlignmentX(0.5f);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        sidebar.add(logout);
    }

    private void navButton(String text, String card, Runnable onShow) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(220, 40));
        b.setAlignmentX(0.5f);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(8, 24, 8, 8));
        b.setForeground(new Color(0xcf, 0xd6, 0xe3));
        b.setBackground(UiUtil.NAVY);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            show(card);
            onShow.run();
        });
        sidebar.add(b);
    }

    private void buildPanels() {
        dashboardPanel = new DashboardPanel(this);
        customerPanel = new CustomerPanel(this);
        vehiclePanel = new VehiclePanel(this, user);
        bookingPanel = new BookingPanel(this);
        bookingListPanel = new BookingListPanel(this);
        returnPanel = new ReturnPanel(this);
        reportPanel = new ReportPanel(this);

        content.add(dashboardPanel, "dashboard");
        content.add(customerPanel, "customers");
        content.add(vehiclePanel, "vehicles");
        content.add(bookingPanel, "book");
        content.add(bookingListPanel, "bookings");
        content.add(returnPanel, "return");
        content.add(reportPanel, "reports");
    }

    /** Switches the visible card. Other panels call this to jump around
     *  (e.g. Dashboard's "New booking" button opens the booking screen). */
    public void show(String card) {
        cards.show(content, card);
        if (card.equals("dashboard")) {
            dashboardPanel.refresh();
        } else if (card.equals("customers")) {
            customerPanel.refresh();
        } else if (card.equals("vehicles")) {
            vehiclePanel.refresh();
        } else if (card.equals("book")) {
            bookingPanel.refresh();
        } else if (card.equals("bookings")) {
            bookingListPanel.refresh();
        } else if (card.equals("return")) {
            returnPanel.refresh();
        } else if (card.equals("reports")) {
            reportPanel.refresh();
        }
    }

    public User getUser() {
        return user;
    }
}
