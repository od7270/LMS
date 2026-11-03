package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class BrowserUtil {
    // Opens the link in the default browser, or shows it so the user can copy it
    public static void open(Component parent, String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception e) {
            // fall through and show the link instead
        }

        JTextField linkField = new JTextField(url);
        linkField.setEditable(false);
        JOptionPane.showMessageDialog(parent, new Object[] {"Could not open the browser. Copy this link:", linkField},
                "Watch Video", JOptionPane.INFORMATION_MESSAGE);
    }
}
