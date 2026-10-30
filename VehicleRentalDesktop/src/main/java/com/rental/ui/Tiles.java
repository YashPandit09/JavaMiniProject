package com.rental.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Small factory for the dashboard/report stat tiles. */
final class Tiles {

    private Tiles() {
    }

    static JPanel plain(String label, String value) {
        return build(label, value, UiUtil.CARD, UiUtil.MUTED, UiUtil.TEXT);
    }

    /** The one dark "hero" tile, like the web dashboard's revenue tile. */
    static JPanel hero(String label, String value) {
        return build(label, value, UiUtil.NAVY, new Color(0xcf, 0xd6, 0xe3), Color.WHITE);
    }

    private static JPanel build(String label, String value, Color bg, Color labelColor, Color valueColor) {
        JPanel p = new JPanel();
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg == UiUtil.CARD ? UiUtil.LINE : bg, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel l = new JLabel(label);
        l.setForeground(labelColor);
        l.setFont(UiUtil.plain(12));
        l.setAlignmentX(0);

        JLabel v = new JLabel(value);
        v.setForeground(valueColor);
        v.setFont(UiUtil.bold(22));
        v.setAlignmentX(0);

        p.add(l);
        p.add(javax.swing.Box.createVerticalStrut(4));
        p.add(v);
        return p;
    }
}
