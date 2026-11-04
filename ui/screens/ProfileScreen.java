package ui.screens;

import model.Assignment;
import model.Course;
import model.Role;
import model.User;
import service.Services;
import ui.Dialogs;
import ui.Screen;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Profile (all roles): own details, a short summary for the role, and change password
public class ProfileScreen implements Screen {
    private final User user;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final JPanel summaryGrid = new JPanel(new GridLayout(0, 2, 10, 5));
    private final JPasswordField currentField = new JPasswordField(15);
    private final JPasswordField newField = new JPasswordField(15);
    private final JPasswordField confirmField = new JPasswordField(15);

    public ProfileScreen(User user, Services services) {
        this.user = user;
        this.services = services;

        JPanel details = new JPanel(new GridLayout(0, 2, 10, 5));
        addRow(details, "Name:", user.getName());
        addRow(details, "Username:", user.getUsername());
        addRow(details, "ID:", user.getId());
        addRow(details, "Role:", user.getRole().getDisplayName());

        JPanel passwordGrid = new JPanel(new GridLayout(0, 2, 10, 5));
        passwordGrid.add(new JLabel("Current password:"));
        passwordGrid.add(currentField);
        passwordGrid.add(new JLabel("New password:"));
        passwordGrid.add(newField);
        passwordGrid.add(new JLabel("Confirm new password:"));
        passwordGrid.add(confirmField);
        JButton changeButton = new JButton("Change Password");
        passwordGrid.add(new JLabel());
        passwordGrid.add(changeButton);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        addSection(content, "My Details", details);
        addSection(content, "Summary", summaryGrid);
        addSection(content, "Change Password", passwordGrid);

        // Keep the form at its natural size in the top-left corner
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(content);
        panel.add(wrapper, BorderLayout.NORTH);

        changeButton.addActionListener(e -> changePassword());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        summaryGrid.removeAll();
        String me = user.getUsername();

        if (user.getRole() == Role.STUDENT) {
            List<Course> courses = services.enrollments.getCoursesFor(me);
            List<Assignment> assignments = services.assignments.getForStudent(me);
            int submitted = 0;
            for (Assignment a : assignments) {
                if (services.assignments.findSubmission(a.getId(), me) != null) {
                    submitted++;
                }
            }
            int present = 0;
            int total = 0;
            for (Course course : courses) {
                present += services.attendance.presentCount(course.getCode(), me);
                total += services.attendance.totalCount(course.getCode(), me);
            }
            addRow(summaryGrid, "Enrolled courses:", String.valueOf(courses.size()));
            addRow(summaryGrid, "Assignments submitted:", submitted + "/" + assignments.size());
            addRow(summaryGrid, "Attendance:", total == 0 ? "-" : present + "/" + total + " (" + present * 100 / total + "%)");
        } else if (user.getRole() == Role.TEACHER) {
            List<Course> courses = services.courses.getByTeacher(me);
            int approved = 0;
            Set<String> students = new HashSet<>();
            for (Course course : courses) {
                if (course.isApproved()) {
                    approved++;
                }
                for (User student : services.enrollments.getStudentsIn(course.getCode())) {
                    students.add(student.getUsername());
                }
            }
            addRow(summaryGrid, "Courses:", courses.size() + " (" + approved + " approved, "
                    + (courses.size() - approved) + " pending)");
            addRow(summaryGrid, "Students taught:", String.valueOf(students.size()));
            addRow(summaryGrid, "Assignments:", String.valueOf(services.assignments.getByTeacher(me).size()));
        } else {
            List<Course> courses = services.courses.getAll();
            int pending = 0;
            for (Course course : courses) {
                if (!course.isApproved()) {
                    pending++;
                }
            }
            addRow(summaryGrid, "Students:", String.valueOf(services.users.getUsers(Role.STUDENT).size()));
            addRow(summaryGrid, "Teachers:", String.valueOf(services.users.getUsers(Role.TEACHER).size()));
            addRow(summaryGrid, "Courses:", courses.size() + " (" + pending + " pending approval)");
        }

        summaryGrid.revalidate();
        summaryGrid.repaint();
        clearFields();
    }

    private void changePassword() {
        try {
            services.users.changePassword(user, new String(currentField.getPassword()),
                    new String(newField.getPassword()), new String(confirmField.getPassword()));
            clearFields();
            Dialogs.info(panel, "Password changed.");
        } catch (IllegalArgumentException ex) {
            Dialogs.error(panel, ex.getMessage());
        }
    }

    private void clearFields() {
        currentField.setText("");
        newField.setText("");
        confirmField.setText("");
    }

    private static void addRow(JPanel grid, String label, String value) {
        grid.add(new JLabel(label));
        grid.add(new JLabel(value));
    }

    private static void addSection(JPanel content, String title, JPanel body) {
        JLabel heading = new JLabel(title);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (content.getComponentCount() > 0) {
            content.add(Box.createVerticalStrut(15));
        }
        content.add(heading);
        content.add(Box.createVerticalStrut(5));
        content.add(body);
    }
}
