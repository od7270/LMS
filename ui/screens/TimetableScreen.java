package ui.screens;

import model.Course;
import model.Role;
import model.TimetableSlot;
import model.User;
import service.Services;
import service.TimetableService;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.util.List;

// Student / Teacher -> Timetable: read-only weekly grid plus a list of classes
public class TimetableScreen implements Screen {
    private final User user;
    private final Services services;
    private final boolean student;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final DefaultTableModel gridModel;
    private final DefaultTableModel listModel;
    private final JLabel emptyLabel = new JLabel();

    public TimetableScreen(User user, Services services) {
        this.user = user;
        this.services = services;
        this.student = user.getRole() == Role.STUDENT;

        String[] gridColumns = new String[TimetableService.DAYS.length + 1];
        gridColumns[0] = "Time";
        for (int i = 0; i < TimetableService.DAYS.length; i++) {
            gridColumns[i + 1] = TimetableService.dayLabel(TimetableService.DAYS[i]);
        }
        gridModel = Tables.model(gridColumns);
        listModel = student
                ? Tables.model("Day", "Time", "Course", "Title", "Room", "Teacher")
                : Tables.model("Day", "Time", "Course", "Title", "Room");

        JTable grid = Tables.table(gridModel);
        grid.setRowSelectionAllowed(false);
        JPanel top = new JPanel(new BorderLayout(5, 5));
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("My Week"), BorderLayout.WEST);
        header.add(emptyLabel, BorderLayout.EAST);
        top.add(header, BorderLayout.NORTH);
        top.add(new JScrollPane(grid), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(new JLabel("Classes"), BorderLayout.NORTH);
        bottom.add(new JScrollPane(Tables.table(listModel)), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        List<TimetableSlot> slots = student
                ? services.timetable.getForStudent(user.getUsername())
                : services.timetable.getForTeacher(user.getUsername());

        // Grid: one row per period, one column per day; two classes in one cell means a clash
        gridModel.setRowCount(0);
        for (int period = 1; period <= TimetableService.PERIODS; period++) {
            Object[] row = new Object[TimetableService.DAYS.length + 1];
            row[0] = TimetableService.periodLabel(period);
            for (int d = 0; d < TimetableService.DAYS.length; d++) {
                row[d + 1] = cell(slots, TimetableService.DAYS[d], period);
            }
            gridModel.addRow(row);
        }

        listModel.setRowCount(0);
        for (TimetableSlot slot : slots) {
            Course course = services.courses.find(slot.getCourseCode());
            String title = course == null ? "" : course.getTitle();
            String day = TimetableService.dayLabel(slot.getDay());
            String time = TimetableService.timeRange(slot.getStartPeriod(), slot.getLength());
            if (student) {
                User teacher = course == null ? null : services.users.findByUsername(course.getTeacherUsername());
                listModel.addRow(new Object[] {day, time, slot.getCourseCode(), title, slot.getRoom(),
                        teacher == null ? "" : teacher.getName()});
            } else {
                listModel.addRow(new Object[] {day, time, slot.getCourseCode(), title, slot.getRoom()});
            }
        }
        emptyLabel.setText(slots.isEmpty() ? "No classes scheduled yet." : "");
    }

    private static String cell(List<TimetableSlot> slots, DayOfWeek day, int period) {
        StringBuilder text = new StringBuilder();
        for (TimetableSlot slot : slots) {
            if (slot.covers(day, period)) {
                if (text.length() > 0) {
                    text.append(" / ");
                }
                text.append(slot.getCourseCode()).append(" (").append(slot.getRoom()).append(')');
            }
        }
        return text.toString();
    }
}
