package ui.screens;

import model.Course;
import model.TimetableSlot;
import model.User;
import service.Services;
import service.TimetableService;
import ui.Dialogs;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

// Admin -> Timetable: schedule weekly classes for every course
public class ManageTimetableScreen implements Screen {
    private static final int MAX_LENGTH = 3;

    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final DefaultTableModel model = Tables.model("Day", "Time", "Course", "Teacher", "Room");
    private final JTable table = Tables.table(model);
    private List<TimetableSlot> slots = new ArrayList<>();

    private final JButton editButton = new JButton("Edit Slot");
    private final JButton deleteButton = new JButton("Delete Slot");

    public ManageTimetableScreen(Services services) {
        this.services = services;

        JButton addButton = new JButton("Add Slot");
        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        panel.add(Tables.section("Timetable (all courses, Mon–Sat, 09:00–17:00)", table, buttons), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        addButton.addActionListener(e -> addSlot());
        editButton.addActionListener(e -> editSlot());
        deleteButton.addActionListener(e -> deleteSlot());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        TimetableSlot previous = selected();
        slots = services.timetable.getAll();
        model.setRowCount(0);
        for (TimetableSlot slot : slots) {
            Course course = services.courses.find(slot.getCourseCode());
            model.addRow(new Object[] {
                    TimetableService.dayLabel(slot.getDay()),
                    TimetableService.timeRange(slot.getStartPeriod(), slot.getLength()),
                    course == null ? slot.getCourseCode() : course.getCode() + " - " + course.getTitle(),
                    course == null ? "" : teacherName(course),
                    slot.getRoom()
            });
        }
        select(previous == null ? -1 : previous.getId());
        updateButtons();
    }

    private void addSlot() {
        List<Course> courses = services.courses.getAll();
        if (courses.isEmpty()) {
            Dialogs.error(panel, "Create a course first (Manage Courses).");
            return;
        }
        JComboBox<String> courseBox = new JComboBox<>();
        for (Course course : courses) {
            courseBox.addItem(course.getCode() + " - " + course.getTitle() + " (" + teacherName(course) + ")");
        }
        Form form = new Form(courseBox);

        while (form.show("Add Slot")) {
            String code = courses.get(courseBox.getSelectedIndex()).getCode();
            try {
                services.timetable.checkHard(code, form.day(), form.start(), form.length(), form.room(), -1);
                if (!acceptStudentClashes(code, form, -1)) {
                    continue;
                }
                services.timetable.add(code, form.day(), form.start(), form.length(), form.room());
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void editSlot() {
        TimetableSlot slot = selected();
        if (slot == null) {
            return;
        }
        JComboBox<String> courseBox = new JComboBox<>(new String[] {slot.getCourseCode()});
        courseBox.setEnabled(false);
        Form form = new Form(courseBox);
        form.dayBox.setSelectedIndex(slot.getDay().getValue() - 1);
        form.startBox.setSelectedIndex(slot.getStartPeriod() - 1);
        form.lengthBox.setSelectedIndex(Math.min(slot.getLength(), MAX_LENGTH) - 1);
        form.roomField.setText(slot.getRoom());

        while (form.show("Edit Slot")) {
            try {
                services.timetable.checkHard(slot.getCourseCode(), form.day(), form.start(), form.length(),
                        form.room(), slot.getId());
                if (!acceptStudentClashes(slot.getCourseCode(), form, slot.getId())) {
                    continue;
                }
                services.timetable.update(slot, form.day(), form.start(), form.length(), form.room());
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void deleteSlot() {
        TimetableSlot slot = selected();
        if (slot != null && Dialogs.confirm(panel, "Delete " + TimetableService.describe(slot) + "?")) {
            services.timetable.delete(slot);
            refresh();
        }
    }

    // Shows students who'd have two classes at once; true = save anyway
    private boolean acceptStudentClashes(String courseCode, Form form, int excludeId) {
        List<String> clashes = services.timetable.studentClashes(courseCode, form.day(), form.start(), form.length(), excludeId);
        if (clashes.isEmpty()) {
            return true;
        }
        StringBuilder message = new StringBuilder("These students would have two classes at the same time:\n");
        int shown = 0;
        for (String clash : clashes) {
            if (shown++ == 10) {
                message.append("  ...and ").append(clashes.size() - 10).append(" more\n");
                break;
            }
            message.append("  ").append(clash).append('\n');
        }
        return Dialogs.confirm(panel, message.append("\nSave anyway?").toString());
    }

    private String teacherName(Course course) {
        User teacher = services.users.findByUsername(course.getTeacherUsername());
        return teacher == null ? course.getTeacherUsername() : teacher.getName();
    }

    private void select(int id) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getId() == id) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private TimetableSlot selected() {
        int row = table.getSelectedRow();
        return (row >= 0 && row < slots.size()) ? slots.get(row) : null;
    }

    private void updateButtons() {
        boolean has = selected() != null;
        editButton.setEnabled(has);
        deleteButton.setEnabled(has);
    }

    // The fields of the Add/Edit Slot dialog; kept between tries so input isn't lost
    private class Form {
        final JComboBox<String> courseBox;
        final JComboBox<String> dayBox = new JComboBox<>();
        final JComboBox<String> startBox = new JComboBox<>();
        final JComboBox<String> lengthBox = new JComboBox<>();
        final JTextField roomField = new JTextField(12);

        Form(JComboBox<String> courseBox) {
            this.courseBox = courseBox;
            for (DayOfWeek day : TimetableService.DAYS) {
                dayBox.addItem(TimetableService.dayLabel(day));
            }
            for (int p = 1; p <= TimetableService.PERIODS; p++) {
                startBox.addItem(TimetableService.periodLabel(p).substring(0, 5));
            }
            for (int h = 1; h <= MAX_LENGTH; h++) {
                lengthBox.addItem(h == 1 ? "1 hour" : h + " hours");
            }
        }

        boolean show(String title) {
            String[] labels = {"Course:", "Day:", "Starts at:", "Length:", "Room:"};
            JComponent[] fields = {courseBox, dayBox, startBox, lengthBox, roomField};
            return Dialogs.showForm(panel, title, labels, fields);
        }

        DayOfWeek day() {
            return TimetableService.DAYS[dayBox.getSelectedIndex()];
        }

        int start() {
            return startBox.getSelectedIndex() + 1;
        }

        int length() {
            return lengthBox.getSelectedIndex() + 1;
        }

        String room() {
            return roomField.getText();
        }
    }
}
