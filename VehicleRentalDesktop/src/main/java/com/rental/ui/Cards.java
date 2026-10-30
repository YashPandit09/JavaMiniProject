package com.rental.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Small factory for the white "card" containers used across the app. */
final class Cards {

    private Cards() {
    }

    /** A plain card with no title, border like the web app's .card. */
    static JPanel plain() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UiUtil.CARD);
        p.setBorder(UiUtil.cardBorder());
        return p;
    }

    /** A card with a bold title above the given content. */
    static JPanel titled(String title, Component content) {
        JPanel p = plain();
        JLabel t = new JLabel(title);
        t.setFont(UiUtil.bold(15));
        t.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
        p.add(t, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }
}
