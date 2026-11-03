package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Tables {
    // Table model whose cells can't be edited by typing in them
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    // Puts the button panel under a titled table
    public static JPanel section(String title, JTable table, JPanel buttons) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }
}
