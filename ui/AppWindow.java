package ui;

import model.User;
import service.Services;

import javax.swing.*;
import java.awt.*;

public class AppWindow {
    private static final String LOGIN = "login";
    private static final String DASHBOARD = "dashboard";

    private final JFrame frame = new JFrame();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Services services;
    private final LoginScreen loginScreen;
    private JPanel dashboard;

    public AppWindow(Services services) {
        this.services = services;
        frame.setSize(1280, 720);
        frame.setTitle("Learning Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginScreen = new LoginScreen(this, services.auth);
        cards.add(loginScreen.getPanel(), LOGIN);

        frame.add(cards);
        frame.setLocationRelativeTo(null);
    }

    public void show() {
        showLogin();
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public void showLogin() {
        frame.getRootPane().setDefaultButton(loginScreen.getLoginButton());
        cardLayout.show(cards, LOGIN);
    }

    public void showDashboard(User user) {
        if (dashboard != null) {
            cards.remove(dashboard);
        }
        dashboard = DashboardFactory.create(user, services, this::showLogin);
        cards.add(dashboard, DASHBOARD);

        frame.getRootPane().setDefaultButton(null);
        cardLayout.show(cards, DASHBOARD);
    }
}
