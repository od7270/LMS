package ui;

import javax.swing.*;

// One page shown in a dashboard's content area
public interface Screen {
    JPanel getPanel();

    // Reloads the data; called every time the screen is opened
    void refresh();
}
