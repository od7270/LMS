package ui.screens;

import model.Assignment;
import model.Submission;
import model.User;
import service.AssignmentService;
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

// Student -> Assignments: see assignments of enrolled courses and submit a link to the work
public class StudentAssignmentsScreen implements Screen {
    private final User student;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout(5, 5));

    private final DefaultTableModel model = Tables.model("Course", "Title", "Due", "Max Marks", "Status");
    private final JTable table = Tables.table(model);
    private List<Assignment> assignments = new ArrayList<>();

    private final JLabel emptyLabel = new JLabel();
    private final JTextArea descriptionArea = new JTextArea(4, 40);
    private final JLabel linkLabel = new JLabel();

    private final JButton submitButton = new JButton("Submit Link");
    private final JButton openButton = new JButton("Open My Link");

    public StudentAssignmentsScreen(User student, Services services) {
        this.student = student;
        this.services = services;

        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel buttons = new JPanel();
        buttons.add(submitButton);
        buttons.add(openButton);

        JPanel details = new JPanel(new BorderLayout(5, 5));
        details.add(new JLabel("Description"), BorderLayout.NORTH);
        details.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        JPanel detailsBottom = new JPanel(new BorderLayout(5, 5));
        detailsBottom.add(linkLabel, BorderLayout.NORTH);
        detailsBottom.add(buttons, BorderLayout.SOUTH);
        details.add(detailsBottom, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("My Assignments"), BorderLayout.WEST);
        top.add(emptyLabel, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(details, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        submitButton.addActionListener(e -> submit());
        openButton.addActionListener(e -> {
            Submission s = mySubmission();
            if (s != null) {
                BrowserUtil.open(panel, s.getLink());
            }
        });
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        int selected = table.getSelectedRow();

        assignments = services.assignments.getForStudent(student.getUsername());
        model.setRowCount(0);
        for (Assignment a : assignments) {
            model.addRow(new Object[] {
                    a.getCourseCode(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getMaxMarks(),
                    services.assignments.status(a, student.getUsername())
            });
        }
        emptyLabel.setText(assignments.isEmpty() ? "No assignments yet." : "");

        if (!assignments.isEmpty()) {
            int row = (selected >= 0 && selected < assignments.size()) ? selected : 0;
            table.setRowSelectionInterval(row, row);
        }
        showSelected();
    }

    private void showSelected() {
        Assignment a = selected();
        Submission s = mySubmission();

        descriptionArea.setText(a == null ? "" : a.getDescription());
        if (a == null) {
            linkLabel.setText("");
        } else if (s == null) {
            linkLabel.setText("Not submitted yet.");
        } else {
            linkLabel.setText("Submitted on " + s.getSubmittedDate() + ": " + s.getLink());
        }

        submitButton.setEnabled(a != null && (s == null || !s.isGraded()));
        submitButton.setText(s == null ? "Submit Link" : "Resubmit");
        openButton.setEnabled(s != null);
    }

    private void submit() {
        Assignment a = selected();
        if (a == null) {
            return;
        }
        Submission existing = mySubmission();
        JTextField linkField = new JTextField(existing == null ? "" : existing.getLink(), 30);
        String[] labels = {"Link to your work (Google Drive, GitHub...):"};

        while (Dialogs.showForm(panel, "Submit: " + a.getTitle(), labels, new JComponent[] {linkField})) {
            try {
                services.assignments.submit(a.getId(), student.getUsername(), linkField.getText());
                refresh();
                String status = services.assignments.status(a, student.getUsername());
                Dialogs.info(panel, AssignmentService.LATE.equals(status)
                        ? "Submitted. It is past the due date, so it is marked as late."
                        : "Submitted.");
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private Assignment selected() {
        int row = table.getSelectedRow();
        return (row >= 0 && row < assignments.size()) ? assignments.get(row) : null;
    }

    private Submission mySubmission() {
        Assignment a = selected();
        return a == null ? null : services.assignments.findSubmission(a.getId(), student.getUsername());
    }
}
