package com.rental.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Applies the same look (teal header, row height, no cell editing) to every JTable. */
final class Tables {

    private Tables() {
    }

    static void style(JTable t) {
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new java.awt.Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setFont(UiUtil.plain(13));

        JTableHeader header = t.getTableHeader();
        header.setBackground(UiUtil.TEAL);
        header.setForeground(Color.WHITE);
        header.setFont(UiUtil.bold(12));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(UiUtil.TEAL);
                setForeground(Color.WHITE);
                setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return c;
            }
        });

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        t.setDefaultRenderer(Object.class, cell);
    }
}
