package ui.screens;

import model.Course;
import model.Lesson;
import model.Role;
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

// Admin -> Manage Courses: all courses, approval, lessons and enrollments
public class ManageCoursesScreen implements Screen {
    private final Services services;
    private final JPanel panel;

    private final DefaultTableModel courseModel =
            Tables.model("Code", "Title", "Teacher", "Status", "Lessons", "Enrolled");
    private final JTable courseTable = Tables.table(courseModel);
    private List<Course> courses = new ArrayList<>();

    private final JButton approveButton = new JButton("Approve");
    private final JButton deleteButton = new JButton("Reject / Delete");
    private final JButton lessonsButton = new JButton("View Lessons");
    private final JButton enrollmentsButton = new JButton("Enrollments");

    public ManageCoursesScreen(Services services) {
        this.services = services;

        JButton newCourseButton = new JButton("New Course");
        JPanel buttons = new JPanel();
        buttons.add(newCourseButton);
        buttons.add(approveButton);
        buttons.add(deleteButton);
        buttons.add(lessonsButton);
        buttons.add(enrollmentsButton);

        panel = Tables.section("All Courses", courseTable, buttons);

        courseTable.getSelectionModel().addListSelectionListener(e -> updateButtons());
        newCourseButton.addActionListener(e -> newCourse());
        approveButton.addActionListener(e -> approve());
        deleteButton.addActionListener(e -> delete());
        lessonsButton.addActionListener(e -> showLessons());
        enrollmentsButton.addActionListener(e -> showEnrollments());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        int selected = courseTable.getSelectedRow();

        courses = services.courses.getAll();
        courseModel.setRowCount(0);
        for (Course course : courses) {
            User teacher = services.users.findByUsername(course.getTeacherUsername());
            courseModel.addRow(new Object[] {
                    course.getCode(),
                    course.getTitle(),
                    teacher != null ? teacher.getName() : course.getTeacherUsername(),
                    course.getStatus().getDisplayName(),
                    services.courses.getLessons(course.getCode()).size(),
                    services.enrollments.getStudentsIn(course.getCode()).size()
            });
        }

        if (selected >= 0 && selected < courses.size()) {
            courseTable.setRowSelectionInterval(selected, selected);
        }
        updateButtons();
    }

    private Course selectedCourse() {
        int row = courseTable.getSelectedRow();
        return (row >= 0 && row < courses.size()) ? courses.get(row) : null;
    }

    private void updateButtons() {
        Course course = selectedCourse();
        approveButton.setEnabled(course != null && !course.isApproved());
        deleteButton.setEnabled(course != null);
        lessonsButton.setEnabled(course != null);
        enrollmentsButton.setEnabled(course != null);
    }

    private void newCourse() {
        List<User> teachers = services.users.getUsers(Role.TEACHER);
        if (teachers.isEmpty()) {
            Dialogs.info(panel, "Add a teacher first (Manage Teachers).");
            return;
        }

        JTextField codeField = new JTextField(15);
        JTextField titleField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);
        JComboBox<String> teacherBox = new JComboBox<>();
        for (User teacher : teachers) {
            teacherBox.addItem(teacher.getName() + " (" + teacher.getUsername() + ")");
        }
        String[] labels = {"Course code (e.g. CS101):", "Title:", "Description:", "Teacher:"};
        JComponent[] fields = {codeField, titleField, descriptionField, teacherBox};

        while (Dialogs.showForm(panel, "New Course", labels, fields)) {
            try {
                String teacherUsername = teachers.get(teacherBox.getSelectedIndex()).getUsername();
                services.courses.create(codeField.getText(), titleField.getText(), descriptionField.getText(),
                        teacherUsername, true);
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void approve() {
        Course course = selectedCourse();
        if (course != null) {
            services.courses.approve(course.getCode());
            refresh();
        }
    }

    private void delete() {
        Course course = selectedCourse();
        if (course != null && Dialogs.confirm(panel, "Delete " + course.getCode()
                + "? Its lessons, assignments, timetable, enrollments, attendance, student progress and submissions will be removed too.")) {
            services.courses.delete(course.getCode());
            courseTable.clearSelection();
            refresh();
        }
    }

    private void showLessons() {
        Course course = selectedCourse();
        if (course == null) {
            return;
        }
        List<Lesson> lessons = services.courses.getLessons(course.getCode());

        DefaultTableModel lessonModel = Tables.model("#", "Title", "YouTube Link");
        int number = 1;
        for (Lesson lesson : lessons) {
            lessonModel.addRow(new Object[] {number++, lesson.getTitle(), lesson.getYoutubeUrl()});
        }
        JTable lessonTable = Tables.table(lessonModel);

        JButton watchButton = new JButton("Watch Video");
        watchButton.addActionListener(e -> {
            int row = lessonTable.getSelectedRow();
            if (row >= 0) {
                BrowserUtil.open(panel, lessons.get(row).getYoutubeUrl());
            }
        });
        JPanel buttons = new JPanel();
        buttons.add(watchButton);

        JPanel content = Tables.section(course.getCode() + " - " + course.getTitle()
                + " (" + lessons.size() + " lessons)", lessonTable, buttons);
        content.setPreferredSize(new Dimension(650, 300));
        JOptionPane.showMessageDialog(panel, content, "Lessons", JOptionPane.PLAIN_MESSAGE);
    }

    private void showEnrollments() {
        Course course = selectedCourse();
        if (course == null) {
            return;
        }
        String code = course.getCode();

        DefaultTableModel enrolledModel = Tables.model("Username", "Name", "Completed", "Progress");
        JTable enrolledTable = Tables.table(enrolledModel);
        List<User> enrolled = new ArrayList<>();
        Runnable reload = () -> {
            enrolled.clear();
            enrolled.addAll(services.enrollments.getStudentsIn(code));
            int total = services.courses.getLessons(code).size();
            enrolledModel.setRowCount(0);
            for (User student : enrolled) {
                enrolledModel.addRow(new Object[] {
                        student.getUsername(),
                        student.getName(),
                        services.enrollments.completedCount(student.getUsername(), code) + "/" + total,
                        services.enrollments.percent(student.getUsername(), code) + "%"
                });
            }
        };
        reload.run();

        JButton enrollButton = new JButton("Enroll Student");
        JButton removeButton = new JButton("Remove");
        JPanel buttons = new JPanel();
        buttons.add(enrollButton);
        buttons.add(removeButton);

        enrollButton.addActionListener(e -> {
            List<User> available = new ArrayList<>();
            for (User student : services.users.getUsers(Role.STUDENT)) {
                if (!services.enrollments.isEnrolled(student.getUsername(), code)) {
                    available.add(student);
                }
            }
            if (available.isEmpty()) {
                Dialogs.info(enrolledTable, "All students are already enrolled.");
                return;
            }
            String[] options = new String[available.size()];
            for (int i = 0; i < available.size(); i++) {
                options[i] = available.get(i).getName() + " (" + available.get(i).getUsername() + ")";
            }
            Object choice = JOptionPane.showInputDialog(enrolledTable, "Choose a student:", "Enroll Student",
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice != null) {
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(choice)) {
                        services.enrollments.enroll(available.get(i).getUsername(), code);
                    }
                }
                reload.run();
            }
        });

        removeButton.addActionListener(e -> {
            int row = enrolledTable.getSelectedRow();
            if (row < 0) {
                Dialogs.info(enrolledTable, "Select a student first.");
                return;
            }
            User student = enrolled.get(row);
            if (Dialogs.confirm(enrolledTable, "Remove " + student.getName() + " from " + code
                    + "? Their progress, submissions and attendance in this course will be cleared.")) {
                services.enrollments.unenroll(student.getUsername(), code);
                reload.run();
            }
        });

        String note = course.isApproved() ? "" : "  (pending approval - students won't see it yet)";
        JPanel content = Tables.section("Students in " + code + " - " + course.getTitle() + note,
                enrolledTable, buttons);
        content.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(panel, content, "Enrollments", JOptionPane.PLAIN_MESSAGE);
        refresh();
    }
}
