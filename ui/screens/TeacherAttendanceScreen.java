package ui.screens;

import model.AttendanceRecord;
import model.Course;
import model.User;
import service.AttendanceService;
import service.Services;
import ui.Dialogs;
import ui.Screen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Teacher -> Attendance: pick a course and a day, tick who was present, save
public class TeacherAttendanceScreen implements Screen {
    private static final int PRESENT_COLUMN = 2;

    private final User teacher;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout(5, 5));

    private final JComboBox<String> courseBox = new JComboBox<>();
    private final JLabel infoLabel = new JLabel();
    private List<Course> courses = new ArrayList<>();
    private int currentCourseIndex = -1;

    private final DefaultListModel<String> dayListModel = new DefaultListModel<>();
    private final JList<String> dayList = new JList<>(dayListModel);
    private List<String> days = new ArrayList<>();
    private String currentDay;
    private String unsavedDay; // a New Day that hasn't been saved yet

    // Only the Present column can be edited; Boolean makes JTable draw a checkbox
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"Username", "Name", "Present", "Overall"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == PRESENT_COLUMN;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == PRESENT_COLUMN ? Boolean.class : Object.class;
        }
    };
    private final JTable table = new JTable(model);
    private List<User> students = new ArrayList<>();

    private final JButton newDayButton = new JButton("New Day");
    private final JButton deleteDayButton = new JButton("Delete Day");
    private final JButton allPresentButton = new JButton("All Present");
    private final JButton allAbsentButton = new JButton("All Absent");
    private final JButton saveButton = new JButton("Save");

    private boolean loading;  // true while the screen fills itself, so listeners ignore the changes
    private boolean dirty;    // true when ticks were changed but not saved

    public TeacherAttendanceScreen(User teacher, Services services) {
        this.teacher = teacher;
        this.services = services;

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Course:"));
        top.add(courseBox);
        top.add(infoLabel);

        dayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.add(new JLabel("Days"), BorderLayout.NORTH);
        left.add(new JScrollPane(dayList), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(180, 0));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JPanel buttons = new JPanel();
        buttons.add(newDayButton);
        buttons.add(deleteDayButton);
        buttons.add(new JLabel("   "));
        buttons.add(allPresentButton);
        buttons.add(allAbsentButton);
        buttons.add(saveButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(left, BorderLayout.WEST);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        courseBox.addActionListener(e -> {
            if (!loading) {
                courseChanged();
            }
        });
        dayList.addListSelectionListener(e -> {
            if (!loading && !e.getValueIsAdjusting()) {
                dayChanged();
            }
        });
        model.addTableModelListener(e -> {
            if (!loading) {
                dirty = true;
            }
        });

        newDayButton.addActionListener(e -> newDay());
        deleteDayButton.addActionListener(e -> deleteDay());
        allPresentButton.addActionListener(e -> setAll(true));
        allAbsentButton.addActionListener(e -> setAll(false));
        saveButton.addActionListener(e -> save());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        if (dirty && !Dialogs.confirm(panel, "Discard unsaved attendance?")) {
            return;
        }
        String previous = currentCourse() != null ? currentCourse().getCode() : null;

        courses = services.courses.getByTeacher(teacher.getUsername());
        loading = true;
        courseBox.removeAllItems();
        int index = courses.isEmpty() ? -1 : 0;
        for (int i = 0; i < courses.size(); i++) {
            courseBox.addItem(courses.get(i).getCode() + " - " + courses.get(i).getTitle());
            if (courses.get(i).getCode().equals(previous)) {
                index = i;
            }
        }
        courseBox.setSelectedIndex(index);
        loading = false;

        currentCourseIndex = index;
        loadCourse(previous != null && previous.equals(codeOrNull()) ? currentDay : null);
    }

    // ---- Switching course / day ----

    private void courseChanged() {
        if (dirty && !Dialogs.confirm(panel, "Discard unsaved attendance?")) {
            loading = true;
            courseBox.setSelectedIndex(currentCourseIndex);
            loading = false;
            return;
        }
        currentCourseIndex = courseBox.getSelectedIndex();
        loadCourse(null);
    }

    private void dayChanged() {
        int index = dayList.getSelectedIndex();
        String day = (index >= 0 && index < days.size()) ? days.get(index) : null;
        if (day == null || day.equals(currentDay)) {
            return;
        }
        if (dirty && !Dialogs.confirm(panel, "Discard unsaved attendance?")) {
            selectDay(currentDay);
            return;
        }
        if (unsavedDay != null && !unsavedDay.equals(day)) {
            unsavedDay = null; // leaving an unsaved New Day drops it
        }
        showDay(day);
        reloadDayList(day);
    }

    // Loads the days of the current course and shows the given day (or the newest one)
    private void loadCourse(String dayToShow) {
        unsavedDay = null;
        Course course = currentCourse();
        if (course == null) {
            reloadDayList(null);
            showDay(null);
            infoLabel.setText(courses.isEmpty() ? "You have no courses yet." : "");
            return;
        }
        List<String> saved = services.attendance.getDays(course.getCode());
        String day = saved.contains(dayToShow) ? dayToShow : (saved.isEmpty() ? null : saved.get(0));
        reloadDayList(day);
        showDay(day);
    }

    // Rebuilds the day list (saved days + an unsaved New Day) and selects the given day
    private void reloadDayList(String select) {
        Course course = currentCourse();
        days = course == null ? new ArrayList<>() : services.attendance.getDays(course.getCode());
        if (unsavedDay != null && !days.contains(unsavedDay)) {
            days.add(unsavedDay);
            days.sort((a, b) -> b.compareTo(a));
        }

        loading = true;
        dayListModel.clear();
        for (String day : days) {
            dayListModel.addElement(day.equals(unsavedDay) ? day + " (not saved)" : day);
        }
        loading = false;
        selectDay(select);
    }

    private void selectDay(String day) {
        loading = true;
        int index = days.indexOf(day);
        if (index >= 0) {
            dayList.setSelectedIndex(index);
        } else {
            dayList.clearSelection();
        }
        loading = false;
    }

    // Fills the table with the enrolled students' ticks for the day
    private void showDay(String day) {
        stopEditing();
        currentDay = day;
        Course course = currentCourse();

        loading = true;
        model.setRowCount(0);
        students = course == null ? new ArrayList<>() : services.enrollments.getStudentsIn(course.getCode());
        for (User student : students) {
            Boolean present = null; // null = no record yet, shown unticked and not saved unless changed
            if (day != null && day.equals(unsavedDay)) {
                present = true;
            } else if (day != null) {
                AttendanceRecord record = services.attendance.find(course.getCode(), student.getUsername(), day);
                present = record == null ? null : record.isPresent();
            }
            model.addRow(new Object[] {student.getUsername(), student.getName(), present, overall(course, student)});
        }
        loading = false;
        dirty = day != null && day.equals(unsavedDay);

        if (course != null) {
            if (students.isEmpty()) {
                infoLabel.setText("No students enrolled yet.");
            } else if (day == null) {
                infoLabel.setText("No attendance taken yet. Click New Day.");
            } else {
                infoLabel.setText("Attendance for " + day + (day.equals(unsavedDay) ? " (not saved yet)" : ""));
            }
        }
        updateButtons();
    }

    private String overall(Course course, User student) {
        int total = services.attendance.totalCount(course.getCode(), student.getUsername());
        if (total == 0) {
            return "-";
        }
        int present = services.attendance.presentCount(course.getCode(), student.getUsername());
        String text = present + "/" + total + " (" + services.attendance.percent(course.getCode(), student.getUsername()) + "%)";
        return services.attendance.isLow(course.getCode(), student.getUsername())
                ? text + " - Below " + AttendanceService.THRESHOLD + "%"
                : text;
    }

    // ---- Buttons ----

    private void newDay() {
        if (currentCourse() == null) {
            return;
        }
        if (dirty && !Dialogs.confirm(panel, "Discard unsaved attendance?")) {
            return;
        }
        JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
        while (Dialogs.showForm(panel, "New Day", new String[] {"Date (YYYY-MM-DD):"}, new JComponent[] {dateField})) {
            try {
                String day = AttendanceService.parseDay(dateField.getText());
                unsavedDay = days.contains(day) && !day.equals(unsavedDay) ? null : day;
                reloadDayList(day);
                showDay(day);
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void deleteDay() {
        Course course = currentCourse();
        if (course == null || currentDay == null) {
            return;
        }
        if (currentDay.equals(unsavedDay)) {
            if (Dialogs.confirm(panel, "Discard the unsaved day " + currentDay + "?")) {
                dirty = false;
                loadCourse(null);
            }
            return;
        }
        if (Dialogs.confirm(panel, "Delete attendance for " + currentDay + "? This can't be undone.")) {
            services.attendance.deleteDay(course.getCode(), currentDay);
            dirty = false;
            loadCourse(null);
        }
    }

    private void setAll(boolean present) {
        stopEditing();
        for (int row = 0; row < model.getRowCount(); row++) {
            model.setValueAt(present, row, PRESENT_COLUMN);
        }
    }

    private void save() {
        Course course = currentCourse();
        if (course == null || currentDay == null) {
            return;
        }
        stopEditing();
        Map<String, Boolean> presence = new LinkedHashMap<>();
        for (int row = 0; row < students.size(); row++) {
            Object value = model.getValueAt(row, PRESENT_COLUMN);
            if (value != null) {
                presence.put(students.get(row).getUsername(), (Boolean) value);
            }
        }
        try {
            services.attendance.saveDay(course.getCode(), currentDay, presence);
            String day = currentDay;
            dirty = false;
            unsavedDay = null;
            reloadDayList(day);
            showDay(day);
            Dialogs.info(panel, "Attendance saved for " + day + ".");
        } catch (IllegalArgumentException ex) {
            Dialogs.error(panel, ex.getMessage());
        }
    }

    // ---- Helpers ----

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private Course currentCourse() {
        return (currentCourseIndex >= 0 && currentCourseIndex < courses.size()) ? courses.get(currentCourseIndex) : null;
    }

    private String codeOrNull() {
        Course course = currentCourse();
        return course == null ? null : course.getCode();
    }

    private void updateButtons() {
        boolean hasCourse = currentCourse() != null;
        boolean hasDay = hasCourse && currentDay != null;
        boolean canMark = hasDay && !students.isEmpty();
        newDayButton.setEnabled(hasCourse);
        deleteDayButton.setEnabled(hasDay);
        allPresentButton.setEnabled(canMark);
        allAbsentButton.setEnabled(canMark);
        saveButton.setEnabled(canMark);
        table.setEnabled(hasDay);
    }
}
