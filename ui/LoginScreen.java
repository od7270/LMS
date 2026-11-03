package ui;

import model.User;
import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginScreen {
    private final JPanel panel;
    private final JButton loginButton;

    public LoginScreen(AppWindow window, AuthService authService) {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2, 10, 10));

        loginPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(15);
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(15);
        loginPanel.add(passwordField);

        JButton cancelButton = new JButton("Cancel");
        loginButton = new JButton("Login");
        loginPanel.add(cancelButton);
        loginPanel.add(loginButton);

        JPanel loginBox = new JPanel(new BorderLayout(10, 10));
        loginBox.add(new JLabel("Learning Management System", JLabel.CENTER), BorderLayout.NORTH);
        loginBox.add(loginPanel, BorderLayout.CENTER);

        panel = new JPanel(new GridBagLayout());
        panel.add(loginBox);

        cancelButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
        });

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            User user = authService.login(username, password);
            if (user != null) {
                usernameField.setText("");
                passwordField.setText("");
                window.showDashboard(user);
            } else {
                JOptionPane.showMessageDialog(window.getFrame(), "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public JButton getLoginButton() {
        return loginButton;
    }
}
