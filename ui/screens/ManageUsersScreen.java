package ui.screens;

import model.Course;
import model.Role;
import model.User;
import service.Services;
import ui.Dialogs;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Admin -> Manage Students / Manage Teachers (same screen, different role)
public class ManageUsersScreen implements Screen {
    private final Role role;
    private final Services services;
    private final JPanel panel;

    private final DefaultTableModel userModel = Tables.model("Username", "Name", "Courses");
    private final JTable userTable = Tables.table(userModel);
    private List<User> users = new ArrayList<>();

    public ManageUsersScreen(Role role, Services services) {
        this.role = role;
        this.services = services;

        JButton addButton = new JButton("Add " + role.getDisplayName());
        JButton deleteButton = new JButton("Delete " + role.getDisplayName());
        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(deleteButton);

        panel = Tables.section(role.getDisplayName() + "s", userTable, buttons);

        addButton.addActionListener(e -> addUser());
        deleteButton.addActionListener(e -> deleteUser());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        users = services.users.getUsers(role);
        userModel.setRowCount(0);
        for (User user : users) {
            userModel.addRow(new Object[] {user.getUsername(), user.getName(), courseCount(user)});
        }
    }

    // Students: courses enrolled in. Teachers: courses they own.
    private int courseCount(User user) {
        if (role == Role.TEACHER) {
            return services.courses.getByTeacher(user.getUsername()).size();
        }
        int count = 0;
        for (Course course : services.courses.getAll()) {
            if (services.enrollments.isEnrolled(user.getUsername(), course.getCode())) {
                count++;
            }
        }
        return count;
    }

    private void addUser() {
        JTextField nameField = new JTextField(15);
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        String[] labels = {"Full name:", "Username:", "Password:"};
        JComponent[] fields = {nameField, usernameField, passwordField};

        while (Dialogs.showForm(panel, "Add " + role.getDisplayName(), labels, fields)) {
            try {
                services.users.addUser(role, nameField.getText(), usernameField.getText(),
                        new String(passwordField.getPassword()));
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row < 0 || row >= users.size()) {
            Dialogs.info(panel, "Select a " + role.getDisplayName().toLowerCase() + " first.");
            return;
        }
        User user = users.get(row);
        String warning = role == Role.STUDENT ? " Their enrollments, progress, submissions and attendance will be removed too." : "";
        if (Dialogs.confirm(panel, "Delete " + user.getName() + " (" + user.getUsername() + ")?" + warning)) {
            try {
                services.users.deleteUser(user.getUsername());
                refresh();
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }
}
