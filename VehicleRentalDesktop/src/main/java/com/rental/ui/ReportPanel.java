package com.rental.ui;

import com.rental.dao.BookingDAO;
import com.rental.model.Payment;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
 * Reports: revenue by month / vehicle type, top vehicles, booking status
 * counts, payments in a date range. Mirrors the web app's ReportSer.
 */
public class ReportPanel extends JPanel {

    private final JPanel stats = new JPanel(new GridLayout(1, 4, 12, 12));
    private final BarChartPanel monthChart = new BarChartPanel();
    private final BarChartPanel typeChart = new BarChartPanel();

    private final DefaultTableModel topModel = new DefaultTableModel(
            new Object[]{"#", "Reg. no.", "Make / model", "Rentals", "Revenue"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JSpinner spFrom = dateSpinner(LocalDate.now().withDayOfMonth(1));
    private final JSpinner spTo = dateSpinner(LocalDate.now());
    private final DefaultTableModel paymentModel = new DefaultTableModel(
            new Object[]{"Payment", "Paid on", "Booking", "Customer", "Vehicle", "Amount"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JLabel lblTotal = new JLabel();

    public ReportPanel(MainFrame main) {
        setLayout(new BorderLayout());
        setBackground(UiUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(UiUtil.BG);

        JLabel title = new JLabel("Reports");
        title.setFont(UiUtil.bold(24));
        title.setAlignmentX(0);
        wrap.add(title);
        wrap.add(Box.createVerticalStrut(14));

        stats.setBackground(UiUtil.BG);
        stats.setAlignmentX(0);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        wrap.add(stats);
        wrap.add(Box.createVerticalStrut(14));

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setBackground(UiUtil.BG);
        charts.setAlignmentX(0);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        charts.add(Cards.titled("Revenue by month (last 6 months)", monthChart));
        charts.add(Cards.titled("Revenue by vehicle type", typeChart));
        wrap.add(charts);
        wrap.add(Box.createVerticalStrut(14));

        JTable topTable = new JTable(topModel);
        Tables.style(topTable);
        topTable.getColumnModel().getColumn(4).setCellRenderer(UiUtil.moneyRenderer());
        JPanel topCard = Cards.titled("Most rented vehicles", new JScrollPane(topTable));
        topCard.setAlignmentX(0);
        topCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        wrap.add(topCard);
        wrap.add(Box.createVerticalStrut(14));

        wrap.add(paymentsCard());

        add(new JScrollPane(wrap), BorderLayout.CENTER);
    }

    private JPanel paymentsCard() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(UiUtil.CARD);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(UiUtil.CARD);
        bar.add(new JLabel("From"));
        bar.add(spFrom);
        bar.add(new JLabel("To"));
        bar.add(spTo);
        bar.add(Buttons.primary("Show", e -> loadPayments()));
        content.add(bar, BorderLayout.NORTH);

        JTable table = new JTable(paymentModel);
        Tables.style(table);
        table.getColumnModel().getColumn(5).setCellRenderer(UiUtil.moneyRenderer());
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(UiUtil.CARD);
        lblTotal.setFont(UiUtil.bold(14));
        footer.add(lblTotal);
        content.add(footer, BorderLayout.SOUTH);

        JPanel card = Cards.titled("Payments received", content);
        card.setAlignmentX(0);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        return card;
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
        return new Date(((java.util.Date) sp.getValue()).getTime());
    }

    private void loadPayments() {
        Date from = spinnerDate(spFrom);
        Date to = spinnerDate(spTo);
        new SwingWorker<Void, Void>() {
            ArrayList<Payment> list;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    list = new BookingDAO().getPayments(from, to);
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(ReportPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                paymentModel.setRowCount(0);
                double total = 0;
                for (Payment p : list) {
                    paymentModel.addRow(new Object[]{"#" + p.getPaymentId(), UiUtil.date(p.getPaidOn()),
                        "#" + p.getBookingId(), p.getCustomerName(), p.getRegNo(), UiUtil.money(p.getAmount())});
                    total += p.getAmount();
                }
                lblTotal.setText("Total (" + list.size() + " payment(s)): " + UiUtil.money(total));
            }
        }.execute();
    }

    public void refresh() {
        new SwingWorker<Void, Void>() {
            double totalRevenue;
            LinkedHashMap<String, Integer> counts;
            LinkedHashMap<String, Double> byMonth;
            LinkedHashMap<String, Double> byType;
            ArrayList<String[]> topVehicles;
            SQLException failure;

            @Override
            protected Void doInBackground() {
                try {
                    BookingDAO dao = new BookingDAO();
                    totalRevenue = dao.getTotalRevenue();
                    counts = dao.getBookingCountByStatus();
                    byMonth = dao.getRevenueByMonth();
                    byType = dao.getRevenueByType();
                    topVehicles = dao.getTopVehicles(5);
                } catch (SQLException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    UiUtil.error(ReportPanel.this, "Database error: " + failure.getMessage());
                    return;
                }
                stats.removeAll();
                stats.add(Tiles.hero("Total revenue (all time)", UiUtil.money(totalRevenue)));
                stats.add(Tiles.plain("Active bookings", String.valueOf(counts.get("BOOKED"))));
                stats.add(Tiles.plain("Completed rentals", String.valueOf(counts.get("RETURNED"))));
                stats.add(Tiles.plain("Cancelled bookings", String.valueOf(counts.get("CANCELLED"))));
                stats.revalidate();
                stats.repaint();

                monthChart.setFormatter(UiUtil::money);
                monthChart.setData(byMonth);
                typeChart.setFormatter(UiUtil::money);
                typeChart.setData(byType);

                topModel.setRowCount(0);
                int rank = 1;
                for (String[] r : topVehicles) {
                    topModel.addRow(new Object[]{rank++, r[0], r[1], r[2], UiUtil.money(Double.parseDouble(r[3]))});
                }
            }
        }.execute();
        loadPayments();
    }
}
