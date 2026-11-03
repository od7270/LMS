package ui.screens;

import model.AttendanceRecord;
import model.Course;
import model.User;
import service.AttendanceService;
import service.Services;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Student -> Attendance: percentage per course, and the days of the selected course
public class StudentAttendanceScreen implements Screen {
    private final User student;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final DefaultTableModel courseModel = Tables.model("Course", "Present", "Total", "%", "Status");
    private final JTable courseTable = Tables.table(courseModel);
    private List<Course> courses = new ArrayList<>();

    private final DefaultTableModel dayModel = Tables.model("Date", "Status");
    private final JLabel daysLabel = new JLabel("Days");

    public StudentAttendanceScreen(User student, Services services) {
        this.student = student;
        this.services = services;

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(new JLabel("My Attendance (warning below " + AttendanceService.THRESHOLD + "%)"), BorderLayout.NORTH);
        top.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(daysLabel, BorderLayout.NORTH);
        bottom.add(new JScrollPane(Tables.table(dayModel)), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDays();
            }
        });
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        int selected = courseTable.getSelectedRow();
        String me = student.getUsername();

        courses = services.enrollments.getCoursesFor(me);
        courseModel.setRowCount(0);
        for (Course course : courses) {
            int total = services.attendance.totalCount(course.getCode(), me);
            String status;
            if (total == 0) {
                status = "No classes yet";
            } else if (services.attendance.isLow(course.getCode(), me)) {
                status = "Below " + AttendanceService.THRESHOLD + "%";
            } else {
                status = "OK";
            }
            courseModel.addRow(new Object[] {
                    course.getCode() + " - " + course.getTitle(),
                    services.attendance.presentCount(course.getCode(), me),
                    total,
                    total == 0 ? "-" : services.attendance.percent(course.getCode(), me) + "%",
                    status
            });
        }

        if (!courses.isEmpty()) {
            int row = (selected >= 0 && selected < courses.size()) ? selected : 0;
            courseTable.setRowSelectionInterval(row, row);
        }
        showDays();
    }

    private void showDays() {
        dayModel.setRowCount(0);
        int row = courseTable.getSelectedRow();
        if (row < 0 || row >= courses.size()) {
            daysLabel.setText(courses.isEmpty() ? "You are not enrolled in any courses yet." : "Days");
            return;
        }
        Course course = courses.get(row);
        daysLabel.setText("Days of " + course.getCode() + " - " + course.getTitle());
        for (AttendanceRecord r : services.attendance.getRecords(course.getCode(), student.getUsername())) {
            dayModel.addRow(new Object[] {r.getDate(), r.isPresent() ? "Present" : "Absent"});
        }
    }
}
