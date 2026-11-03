package ui;

import javax.swing.*;
import java.awt.*;

public class Dialogs {
    // Shows labelled fields in an OK/Cancel dialog. Returns true if OK was pressed.
    public static boolean showForm(Component parent, String title, String[] labels, JComponent[] fields) {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            form.add(fields[i]);
        }
        int result = JOptionPane.showConfirmDialog(parent, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message);
    }
}
