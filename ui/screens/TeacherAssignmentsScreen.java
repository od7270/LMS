package ui.screens;

import model.Assignment;
import model.Course;
import model.Submission;
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

// Teacher -> Assignments: create, edit and delete assignments for own courses
public class TeacherAssignmentsScreen implements Screen {
    private final User teacher;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final DefaultTableModel model = Tables.model("Course", "Title", "Due", "Max Marks", "Submitted", "Graded");
    private final JTable table = Tables.table(model);
    private final JTextArea descriptionArea = new JTextArea(4, 40);
    private List<Assignment> assignments = new ArrayList<>();

    private final JButton newButton = new JButton("New Assignment");
    private final JButton editButton = new JButton("Edit");
    private final JButton deleteButton = new JButton("Delete");

    public TeacherAssignmentsScreen(User teacher, Services services) {
        this.teacher = teacher;
        this.services = services;

        JPanel buttons = new JPanel();
        buttons.add(newButton);
        buttons.add(editButton);
        buttons.add(deleteButton);

        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JPanel details = new JPanel(new BorderLayout(5, 5));
        details.add(new JLabel("Description"), BorderLayout.NORTH);
        details.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        JPanel section = Tables.section("My Assignments", table, buttons);
        panel.add(section, BorderLayout.CENTER);
        panel.add(details, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        newButton.addActionListener(e -> newAssignment());
        editButton.addActionListener(e -> editAssignment());
        deleteButton.addActionListener(e -> deleteAssignment());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        Assignment previous = selected();

        assignments = services.assignments.getByTeacher(teacher.getUsername());
        model.setRowCount(0);
        for (Assignment a : assignments) {
            List<Submission> submissions = services.assignments.getSubmissions(a.getId());
            int graded = 0;
            for (Submission s : submissions) {
                if (s.isGraded()) {
                    graded++;
                }
            }
            int enrolled = services.enrollments.getStudentsIn(a.getCourseCode()).size();
            model.addRow(new Object[] {
                    a.getCourseCode(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getMaxMarks(),
                    submissions.size() + "/" + enrolled,
                    graded
            });
        }

        select(previous != null ? previous.getId() : null);
        showSelected();
    }

    private void showSelected() {
        Assignment a = selected();
        descriptionArea.setText(a == null ? "" : a.getDescription());
        editButton.setEnabled(a != null);
        deleteButton.setEnabled(a != null);
    }

    private void newAssignment() {
        List<Course> courses = services.courses.getByTeacher(teacher.getUsername());
        if (courses.isEmpty()) {
            Dialogs.error(panel, "Create a course first (My Courses).");
            return;
        }
        JComboBox<String> courseBox = new JComboBox<>();
        for (Course course : courses) {
            courseBox.addItem(course.getCode() + " - " + course.getTitle());
        }
        JTextField titleField = new JTextField(20);
        JTextArea descriptionField = new JTextArea(4, 20);
        JTextField dueField = new JTextField(20);
        JTextField maxField = new JTextField(20);

        while (showForm("New Assignment", courseBox, titleField, descriptionField, dueField, maxField)) {
            try {
                String code = courses.get(courseBox.getSelectedIndex()).getCode();
                services.assignments.create(code, titleField.getText(), descriptionField.getText(),
                        dueField.getText(), maxField.getText());
                refresh();
                select(newestId());
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void editAssignment() {
        Assignment a = selected();
        if (a == null) {
            return;
        }
        JComboBox<String> courseBox = new JComboBox<>(new String[] {a.getCourseCode()});
        courseBox.setEnabled(false);
        JTextField titleField = new JTextField(a.getTitle(), 20);
        JTextArea descriptionField = new JTextArea(a.getDescription(), 4, 20);
        JTextField dueField = new JTextField(a.getDueDate(), 20);
        JTextField maxField = new JTextField(String.valueOf(a.getMaxMarks()), 20);

        while (showForm("Edit Assignment", courseBox, titleField, descriptionField, dueField, maxField)) {
            try {
                services.assignments.update(a, titleField.getText(), descriptionField.getText(),
                        dueField.getText(), maxField.getText());
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void deleteAssignment() {
        Assignment a = selected();
        if (a != null && Dialogs.confirm(panel, "Delete \"" + a.getTitle()
                + "\"? All student submissions and grades for it will be removed too.")) {
            services.assignments.delete(a);
            refresh();
        }
    }

    private boolean showForm(String title, JComboBox<String> courseBox, JTextField titleField,
                             JTextArea descriptionField, JTextField dueField, JTextField maxField) {
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        String[] labels = {"Course:", "Title:", "Description:", "Due date (YYYY-MM-DD):", "Max marks:"};
        JComponent[] fields = {courseBox, titleField, new JScrollPane(descriptionField), dueField, maxField};
        return Dialogs.showForm(panel, title, labels, fields);
    }

    // The ID of the assignment created last (IDs only go up)
    private String newestId() {
        String newest = null;
        int max = -1;
        for (Assignment a : assignments) {
            try {
                int id = Integer.parseInt(a.getId());
                if (id > max) {
                    max = id;
                    newest = a.getId();
                }
            } catch (NumberFormatException ignored) {
                // not one of our generated IDs
            }
        }
        return newest;
    }

    private void select(String id) {
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).getId().equals(id)) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
        if (!assignments.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private Assignment selected() {
        int row = table.getSelectedRow();
        return (row >= 0 && row < assignments.size()) ? assignments.get(row) : null;
    }
}
