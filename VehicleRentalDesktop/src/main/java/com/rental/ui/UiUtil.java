package com.rental.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Small helpers shared by every screen: the colour palette (same as the
 * web app's CSS), formatting, and little Swing conveniences.
 */
public final class UiUtil {

    private UiUtil() {
    }

    // ---- colours - same palette as src/main/webapp/css/style.css ----
    public static final Color NAVY = new Color(0x11, 0x1c, 0x2e);
    public static final Color TEAL = new Color(0x1a, 0x6b, 0x6b);
    public static final Color TEAL_DARK = new Color(0x12, 0x50, 0x4f);
    public static final Color BAR = new Color(0x00, 0x89, 0x7b);
    public static final Color AMBER = new Color(0xf5, 0xa6, 0x23);
    public static final Color BG = new Color(0xf5, 0xf2, 0xeb);
    public static final Color CARD = Color.WHITE;
    public static final Color LINE = new Color(0xe3, 0xde, 0xd3);
    public static final Color TEXT = new Color(0x1d, 0x24, 0x33);
    public static final Color MUTED = new Color(0x5b, 0x64, 0x75);
    public static final Color GREEN_BG = new Color(0xe3, 0xf4, 0xe8);
    public static final Color GREEN_FG = new Color(0x0a, 0x6b, 0x2c);
    public static final Color RED_BG = new Color(0xfd, 0xe7, 0xe5);
    public static final Color RED_FG = new Color(0xb4, 0x23, 0x18);
    public static final Color AMBER_BG = new Color(0xfd, 0xf0, 0xd5);
    public static final Color AMBER_FG = new Color(0x8a, 0x53, 0x00);
    public static final Color BLUE_BG = new Color(0xe3, 0xed, 0xfa);
    public static final Color BLUE_FG = new Color(0x1d, 0x56, 0xa8);
    public static final Color GREY_BG = new Color(0xec, 0xec, 0xec);
    public static final Color GREY_FG = new Color(0x4b, 0x55, 0x63);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static Font bold(float size) {
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    public static Font plain(float size) {
        return new Font("Segoe UI", Font.PLAIN, (int) size);
    }

    /** 1500.0 -> "Rs. 1,500.00" */
    public static String money(double amount) {
        return String.format("Rs. %,.2f", amount);
    }

    /** 2026-10-26 -> "26 Oct 2026" */
    public static String date(Date d) {
        return d == null ? "" : d.toLocalDate().format(DATE_FMT);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Colours a status cell the same way as the web app's badges. */
    public static Color[] statusColors(String status) {
        if (status == null) {
            return new Color[]{CARD, TEXT};
        }
        switch (status) {
            case "AVAILABLE":
            case "RETURNED_OK":
                return new Color[]{GREEN_BG, GREEN_FG};
            case "MAINTENANCE":
            case "OVERDUE":
                return new Color[]{AMBER_BG, AMBER_FG};
            case "BOOKED":
                return new Color[]{BLUE_BG, BLUE_FG};
            case "RETURNED":
                return new Color[]{GREY_BG, GREY_FG};
            case "CANCELLED":
            case "NO LICENCE":
                return new Color[]{RED_BG, RED_FG};
            default:
                return new Color[]{CARD, TEXT};
        }
    }

    /** A table cell renderer that paints status text like the web app's badges. */
    public static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = String.valueOf(value);
                Color[] colors = statusColors(status);
                setOpaque(true);
                setBackground(isSelected ? table.getSelectionBackground() : colors[0]);
                setForeground(isSelected ? table.getSelectionForeground() : colors[1]);
                setFont(bold(12));
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(2, 6, 2, 6));
                return c;
            }
        };
    }

    public static DefaultTableCellRenderer moneyRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(RIGHT);
                setBorder(new EmptyBorder(0, 0, 0, 8));
                return c;
            }
        };
    }

    public static javax.swing.border.Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                new EmptyBorder(14, 16, 14, 16));
    }
}
