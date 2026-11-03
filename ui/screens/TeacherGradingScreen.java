package ui.screens;

import model.Assignment;
import model.Submission;
import model.User;
import service.Services;
import ui.BrowserUtil;
import ui.Dialogs;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Teacher -> Grade Submissions: pick an assignment, open each student's link and give marks
public class TeacherGradingScreen implements Screen {
    private final User teacher;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout(5, 5));

    private final JComboBox<String> assignmentBox = new JComboBox<>();
    private final JLabel infoLabel = new JLabel();
    private List<Assignment> assignments = new ArrayList<>();

    private final DefaultTableModel model = Tables.model("Username", "Name", "Status", "Submitted On", "Marks", "Feedback");
    private final JTable table = Tables.table(model);
    private List<User> students = new ArrayList<>();

    private final JButton openButton = new JButton("Open Link");
    private final JButton gradeButton = new JButton("Grade");

    public TeacherGradingScreen(User teacher, Services services) {
        this.teacher = teacher;
        this.services = services;

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Assignment:"));
        top.add(assignmentBox);
        top.add(infoLabel);

        JPanel buttons = new JPanel();
        buttons.add(openButton);
        buttons.add(gradeButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        assignmentBox.addActionListener(e -> showStudents());
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        openButton.addActionListener(e -> {
            Submission s = selectedSubmission();
            if (s != null) {
                BrowserUtil.open(panel, s.getLink());
            }
        });
        gradeButton.addActionListener(e -> grade());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        int selected = assignmentBox.getSelectedIndex();

        assignments = services.assignments.getByTeacher(teacher.getUsername());
        assignmentBox.removeAllItems();
        for (Assignment a : assignments) {
            assignmentBox.addItem(a.getCourseCode() + " - " + a.getTitle());
        }
        if (selected >= 0 && selected < assignments.size()) {
            assignmentBox.setSelectedIndex(selected);
        }
        showStudents();
    }

    private void showStudents() {
        model.setRowCount(0);
        students = new ArrayList<>();
        Assignment a = selectedAssignment();
        if (a == null) {
            infoLabel.setText(assignments.isEmpty() ? "You have no assignments yet." : "");
            updateButtons();
            return;
        }

        infoLabel.setText("Due " + a.getDueDate() + ", out of " + a.getMaxMarks());
        students = services.enrollments.getStudentsIn(a.getCourseCode());
        if (students.isEmpty()) {
            infoLabel.setText(infoLabel.getText() + " - no students enrolled yet.");
        }
        for (User student : students) {
            Submission s = services.assignments.findSubmission(a.getId(), student.getUsername());
            String status = services.assignments.status(a, student.getUsername());
            if (s != null && s.isGraded() && services.assignments.isLate(s, a)) {
                status += " (late)";
            }
            model.addRow(new Object[] {
                    student.getUsername(),
                    student.getName(),
                    status,
                    s == null ? "" : s.getSubmittedDate(),
                    s != null && s.isGraded() ? s.getMarks() + "/" + a.getMaxMarks() : "",
                    s == null ? "" : s.getFeedback()
            });
        }
        updateButtons();
    }

    private void grade() {
        Submission s = selectedSubmission();
        Assignment a = selectedAssignment();
        if (s == null || a == null) {
            return;
        }
        int row = table.getSelectedRow();
        JTextField marksField = new JTextField(s.isGraded() ? String.valueOf(s.getMarks()) : "", 10);
        JTextArea feedbackArea = new JTextArea(s.getFeedback(), 4, 20);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        String[] labels = {"Marks (out of " + a.getMaxMarks() + "):", "Feedback:"};
        JComponent[] fields = {marksField, new JScrollPane(feedbackArea)};

        while (Dialogs.showForm(panel, "Grade " + s.getStudentUsername(), labels, fields)) {
            try {
                services.assignments.grade(s, marksField.getText(), feedbackArea.getText());
                showStudents();
                table.setRowSelectionInterval(row, row);
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private Assignment selectedAssignment() {
        int index = assignmentBox.getSelectedIndex();
        return (index >= 0 && index < assignments.size()) ? assignments.get(index) : null;
    }

    private Submission selectedSubmission() {
        Assignment a = selectedAssignment();
        int row = table.getSelectedRow();
        if (a == null || row < 0 || row >= students.size()) {
            return null;
        }
        return services.assignments.findSubmission(a.getId(), students.get(row).getUsername());
    }

    private void updateButtons() {
        boolean hasSubmission = selectedSubmission() != null;
        openButton.setEnabled(hasSubmission);
        gradeButton.setEnabled(hasSubmission);
    }
}
