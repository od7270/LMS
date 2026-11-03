package ui.screens;

import model.Course;
import model.Lesson;
import model.User;
import service.Services;
import ui.BrowserUtil;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Student -> My Courses: enrolled courses, their lessons, watch + mark complete
public class StudentCoursesScreen implements Screen {
    private final User student;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout(10, 10));

    private final DefaultListModel<String> courseListModel = new DefaultListModel<>();
    private final JList<String> courseList = new JList<>(courseListModel);
    private List<Course> courses = new ArrayList<>();

    private final JLabel titleLabel = new JLabel();
    private final JLabel teacherLabel = new JLabel();
    private final JTextArea descriptionArea = new JTextArea(3, 40);

    private final DefaultTableModel lessonModel = Tables.model("#", "Lesson", "Status");
    private final JTable lessonTable = Tables.table(lessonModel);
    private List<Lesson> lessons = new ArrayList<>();

    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JButton watchButton = new JButton("Watch Video");
    private final JButton completeButton = new JButton("Mark Complete");

    public StudentCoursesScreen(User student, Services services) {
        this.student = student;
        this.services = services;

        // Left: enrolled courses
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedCourse();
            }
        });
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.add(new JLabel("My Courses"), BorderLayout.NORTH);
        left.add(new JScrollPane(courseList), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(280, 0));

        // Right: course details
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel details = new JPanel(new GridLayout(0, 1));
        details.add(titleLabel);
        details.add(teacherLabel);
        JPanel header = new JPanel(new BorderLayout(5, 5));
        header.add(details, BorderLayout.NORTH);
        header.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        lessonTable.getSelectionModel().addListSelectionListener(e -> updateButtons());

        progressBar.setStringPainted(true);
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(progressBar, BorderLayout.NORTH);
        JPanel buttons = new JPanel();
        buttons.add(watchButton);
        buttons.add(completeButton);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.add(header, BorderLayout.NORTH);
        right.add(new JScrollPane(lessonTable), BorderLayout.CENTER);
        right.add(bottom, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);

        watchButton.addActionListener(e -> {
            Lesson lesson = selectedLesson();
            if (lesson != null) {
                BrowserUtil.open(panel, lesson.getYoutubeUrl());
            }
        });

        completeButton.addActionListener(e -> {
            Lesson lesson = selectedLesson();
            if (lesson != null) {
                int row = lessonTable.getSelectedRow();
                services.enrollments.toggleComplete(student.getUsername(), lesson.getId());
                refresh();
                lessonTable.setRowSelectionInterval(row, row);
            }
        });
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        int selected = courseList.getSelectedIndex();

        courses = services.enrollments.getCoursesFor(student.getUsername());
        courseListModel.clear();
        for (Course course : courses) {
            int percent = services.enrollments.percent(student.getUsername(), course.getCode());
            courseListModel.addElement(course.getCode() + " - " + course.getTitle() + " (" + percent + "%)");
        }

        if (courses.isEmpty()) {
            showSelectedCourse();
        } else {
            int index = (selected >= 0 && selected < courses.size()) ? selected : 0;
            courseList.setSelectedIndex(index);
            showSelectedCourse();
        }
    }

    private void showSelectedCourse() {
        int index = courseList.getSelectedIndex();
        lessonModel.setRowCount(0);

        if (index < 0 || index >= courses.size()) {
            titleLabel.setText(courses.isEmpty() ? "You are not enrolled in any courses yet." : "Select a course");
            teacherLabel.setText("");
            descriptionArea.setText("");
            lessons = new ArrayList<>();
            progressBar.setValue(0);
            progressBar.setString("");
            updateButtons();
            return;
        }

        Course course = courses.get(index);
        User teacher = services.users.findByUsername(course.getTeacherUsername());
        titleLabel.setText(course.getCode() + " - " + course.getTitle());
        teacherLabel.setText("Teacher: " + (teacher != null ? teacher.getName() : course.getTeacherUsername()));
        descriptionArea.setText(course.getDescription());

        lessons = services.courses.getLessons(course.getCode());
        int number = 1;
        for (Lesson lesson : lessons) {
            boolean done = services.enrollments.isComplete(student.getUsername(), lesson.getId());
            lessonModel.addRow(new Object[] {number++, lesson.getTitle(), done ? "Completed" : "Not completed"});
        }

        int completed = services.enrollments.completedCount(student.getUsername(), course.getCode());
        int percent = services.enrollments.percent(student.getUsername(), course.getCode());
        progressBar.setValue(percent);
        progressBar.setString("Progress: " + completed + "/" + lessons.size() + " (" + percent + "%)");
        updateButtons();
    }

    private Lesson selectedLesson() {
        int row = lessonTable.getSelectedRow();
        return (row >= 0 && row < lessons.size()) ? lessons.get(row) : null;
    }

    private void updateButtons() {
        Lesson lesson = selectedLesson();
        watchButton.setEnabled(lesson != null);
        completeButton.setEnabled(lesson != null);
        boolean done = lesson != null && services.enrollments.isComplete(student.getUsername(), lesson.getId());
        completeButton.setText(done ? "Mark Incomplete" : "Mark Complete");
    }
}
