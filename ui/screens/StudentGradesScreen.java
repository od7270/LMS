package ui.screens;

import model.Assignment;
import model.Course;
import model.Submission;
import model.User;
import service.Services;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Student -> Grades: marks for each graded assignment and a total per course
public class StudentGradesScreen implements Screen {
    private final User student;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final DefaultTableModel gradeModel = Tables.model("Course", "Assignment", "Marks", "%", "Feedback");
    private final DefaultTableModel totalModel = Tables.model("Course", "Graded", "Total", "%");

    public StudentGradesScreen(User student, Services services) {
        this.student = student;
        this.services = services;

        JPanel grades = new JPanel(new BorderLayout(5, 5));
        grades.add(new JLabel("Graded Assignments"), BorderLayout.NORTH);
        grades.add(new JScrollPane(Tables.table(gradeModel)), BorderLayout.CENTER);

        JPanel totals = new JPanel(new BorderLayout(5, 5));
        totals.add(new JLabel("Course Totals (graded assignments only)"), BorderLayout.NORTH);
        totals.add(new JScrollPane(Tables.table(totalModel)), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, grades, totals);
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        gradeModel.setRowCount(0);
        totalModel.setRowCount(0);

        for (Course course : services.enrollments.getCoursesFor(student.getUsername())) {
            int assignmentCount = 0;
            int gradedCount = 0;
            int marks = 0;
            int max = 0;

            for (Assignment a : services.assignments.getByCourse(course.getCode())) {
                assignmentCount++;
                Submission s = services.assignments.findSubmission(a.getId(), student.getUsername());
                if (s == null || !s.isGraded()) {
                    continue;
                }
                gradedCount++;
                marks += s.getMarks();
                max += a.getMaxMarks();
                gradeModel.addRow(new Object[] {
                        course.getCode(),
                        a.getTitle(),
                        s.getMarks() + "/" + a.getMaxMarks(),
                        s.getMarks() * 100 / a.getMaxMarks() + "%",
                        s.getFeedback()
                });
            }

            totalModel.addRow(new Object[] {
                    course.getCode() + " - " + course.getTitle(),
                    gradedCount + "/" + assignmentCount,
                    gradedCount == 0 ? "-" : marks + "/" + max,
                    gradedCount == 0 ? "-" : marks * 100 / max + "%"
            });
        }
    }
}
