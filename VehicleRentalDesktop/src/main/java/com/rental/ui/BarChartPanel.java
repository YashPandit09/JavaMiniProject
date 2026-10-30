package com.rental.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * A plain horizontal bar chart drawn by hand (paintComponent) - no charting
 * library needed. One bar per entry; label on the left, value on the right.
 * Same teal colour and layout idea as the web app's report bars.
 */
public class BarChartPanel extends JPanel {

    private LinkedHashMap<String, Double> data = new LinkedHashMap<>();
    private java.util.function.DoubleFunction<String> formatter = v -> String.format("%.0f", v);
    private static final int ROW_HEIGHT = 30;
    private static final int LABEL_WIDTH = 90;
    private static final int VALUE_WIDTH = 110;

    public void setData(LinkedHashMap<String, Double> data) {
        this.data = data;
        setPreferredSize(new Dimension(400, Math.max(60, data.size() * ROW_HEIGHT + 10)));
        revalidate();
        repaint();
    }

    public void setFormatter(java.util.function.DoubleFunction<String> formatter) {
        this.formatter = formatter;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) {
            g.setColor(UiUtil.MUTED);
            g.drawString("No data yet.", 10, 20);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(UiUtil.plain(12));
        FontMetrics fm = g2.getFontMetrics();

        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (max <= 0) {
            max = 1;
        }

        int barAreaWidth = getWidth() - LABEL_WIDTH - VALUE_WIDTH - 10;
        int y = 4;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            int barHeight = 16;
            int barY = y + (ROW_HEIGHT - barHeight) / 2;

            g2.setColor(UiUtil.TEXT);
            g2.drawString(e.getKey(), 0, y + ROW_HEIGHT / 2 + fm.getAscent() / 2 - 2);

            int w = (int) Math.round(barAreaWidth * (e.getValue() / max));
            g2.setColor(UiUtil.BAR);
            g2.fillRoundRect(LABEL_WIDTH, barY, Math.max(2, w), barHeight, 4, 4);

            g2.setColor(UiUtil.TEXT);
            String text = formatter.apply(e.getValue());
            g2.drawString(text, LABEL_WIDTH + barAreaWidth + 10, y + ROW_HEIGHT / 2 + fm.getAscent() / 2 - 2);

            y += ROW_HEIGHT;
        }
    }
}
