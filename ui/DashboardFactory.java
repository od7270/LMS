package ui;

import model.User;
import service.Services;

import javax.swing.*;
import java.awt.*;

public class DashboardFactory {
    private static final String PLACEHOLDER = "placeholder";

    public static JPanel create(User user, Services services, Runnable onLogout) {
        String role = user.getRole().getDisplayName();
        JPanel dashboard = new JPanel(new BorderLayout(10, 10));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(new JLabel(role + " Dashboard"), BorderLayout.WEST);

        JPanel topRight = new JPanel();
        topRight.add(new JLabel("Welcome, " + user.getName()));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> onLogout.run());
        topRight.add(logoutButton);
        topBar.add(topRight, BorderLayout.EAST);

        dashboard.add(topBar, BorderLayout.NORTH);

        // Main content: one card per built screen, plus a placeholder for the rest
        CardLayout contentLayout = new CardLayout();
        JPanel content = new JPanel(contentLayout);
        JLabel placeholderLabel = new JLabel("Select an option from the menu", JLabel.CENTER);
        content.add(placeholderLabel, PLACEHOLDER);
        dashboard.add(content, BorderLayout.CENTER);

        // Side menu
        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        for (String item : user.getMenuItems()) {
            JButton menuButton = new JButton(item);
            Screen screen = ScreenFactory.create(user, item, services);

            if (screen != null) {
                content.add(screen.getPanel(), item);
                menuButton.addActionListener(e -> {
                    screen.refresh();
                    contentLayout.show(content, item);
                });
            } else {
                menuButton.addActionListener(e -> {
                    placeholderLabel.setText(item + " - coming soon");
                    contentLayout.show(content, PLACEHOLDER);
                });
            }
            menuPanel.add(menuButton);
        }

        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.add(menuPanel, BorderLayout.NORTH);
        dashboard.add(sideBar, BorderLayout.WEST);

        // Status bar
        dashboard.add(new JLabel("Logged in as: " + user.getUsername()), BorderLayout.SOUTH);

        return dashboard;
    }
}
