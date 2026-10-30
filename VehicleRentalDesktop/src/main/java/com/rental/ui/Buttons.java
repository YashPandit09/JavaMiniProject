package com.rental.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/** Small factory for the button styles used across the app (teal / light / danger). */
final class Buttons {

    private Buttons() {
    }

    static JButton primary(String text, ActionListener onClick) {
        JButton b = new JButton(text);
        b.setBackground(UiUtil.TEAL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(UiUtil.bold(13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(onClick);
        return b;
    }

    static JButton light(String text, ActionListener onClick) {
        JButton b = new JButton(text);
        b.setBackground(UiUtil.CARD);
        b.setForeground(UiUtil.TEAL);
        b.setBorder(BorderFactory.createLineBorder(UiUtil.TEAL));
        b.setFocusPainted(false);
        b.setFont(UiUtil.plain(13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(onClick);
        return b;
    }

    static JButton danger(String text, ActionListener onClick) {
        JButton b = new JButton(text);
        b.setBackground(UiUtil.RED_FG);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(UiUtil.bold(12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(onClick);
        return b;
    }

    static JButton small(String text, ActionListener onClick) {
        JButton b = primary(text, onClick);
        b.setFont(UiUtil.plain(11));
        b.setMargin(new java.awt.Insets(2, 8, 2, 8));
        return b;
    }

    static JButton smallLight(String text, ActionListener onClick) {
        JButton b = light(text, onClick);
        b.setFont(UiUtil.plain(11));
        b.setMargin(new java.awt.Insets(2, 8, 2, 8));
        return b;
    }
}
