import service.Services;
import ui.AppWindow;

import javax.swing.*;
import java.nio.file.Paths;

public class LearningManagementSystem {
    public static void main(String[] args) {
        Services services = new Services(Paths.get("data"));
        SwingUtilities.invokeLater(() -> new AppWindow(services).show());
    }
}
