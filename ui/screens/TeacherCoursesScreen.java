package ui.screens;

import model.Course;
import model.Lesson;
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

// Teacher -> My Courses: create/delete own courses and manage their lessons
public class TeacherCoursesScreen implements Screen {
    private final User teacher;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout());

    private final DefaultTableModel courseModel = Tables.model("Code", "Title", "Status", "Lessons", "Students");
    private final JTable courseTable = Tables.table(courseModel);
    private List<Course> courses = new ArrayList<>();

    private final DefaultTableModel lessonModel = Tables.model("#", "Title", "YouTube Link");
    private final JTable lessonTable = Tables.table(lessonModel);
    private final JLabel lessonsLabel = new JLabel("Lessons");
    private List<Lesson> lessons = new ArrayList<>();

    private final JButton deleteCourseButton = new JButton("Delete Course");
    private final JButton addLessonButton = new JButton("Add Lesson");
    private final JButton editLessonButton = new JButton("Edit Lesson");
    private final JButton deleteLessonButton = new JButton("Delete Lesson");
    private final JButton watchButton = new JButton("Watch Video");

    public TeacherCoursesScreen(User teacher, Services services) {
        this.teacher = teacher;
        this.services = services;

        // Courses section
        JButton newCourseButton = new JButton("New Course");
        JPanel courseButtons = new JPanel();
        courseButtons.add(newCourseButton);
        courseButtons.add(deleteCourseButton);
        JPanel coursesSection = Tables.section("My Courses", courseTable, courseButtons);

        // Lessons section
        JPanel lessonButtons = new JPanel();
        lessonButtons.add(addLessonButton);
        lessonButtons.add(editLessonButton);
        lessonButtons.add(deleteLessonButton);
        lessonButtons.add(watchButton);
        JPanel lessonsSection = new JPanel(new BorderLayout(5, 5));
        lessonsSection.add(lessonsLabel, BorderLayout.NORTH);
        lessonsSection.add(new JScrollPane(lessonTable), BorderLayout.CENTER);
        lessonsSection.add(lessonButtons, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, coursesSection, lessonsSection);
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showLessons();
            }
        });
        lessonTable.getSelectionModel().addListSelectionListener(e -> updateButtons());

        newCourseButton.addActionListener(e -> newCourse());
        deleteCourseButton.addActionListener(e -> deleteCourse());
        addLessonButton.addActionListener(e -> addLesson());
        editLessonButton.addActionListener(e -> editLesson());
        deleteLessonButton.addActionListener(e -> deleteLesson());
        watchButton.addActionListener(e -> {
            Lesson lesson = selectedLesson();
            if (lesson != null) {
                BrowserUtil.open(panel, lesson.getYoutubeUrl());
            }
        });
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        Course previous = selectedCourse();

        courses = services.courses.getByTeacher(teacher.getUsername());
        courseModel.setRowCount(0);
        for (Course course : courses) {
            courseModel.addRow(new Object[] {
                    course.getCode(),
                    course.getTitle(),
                    course.getStatus().getDisplayName(),
                    services.courses.getLessons(course.getCode()).size(),
                    services.enrollments.getStudentsIn(course.getCode()).size()
            });
        }

        selectCourse(previous != null ? previous.getCode() : null);
        showLessons();
    }

    private void showLessons() {
        Course course = selectedCourse();
        lessonModel.setRowCount(0);
        lessons = new ArrayList<>();

        if (course == null) {
            lessonsLabel.setText("Lessons (select a course)");
        } else {
            lessonsLabel.setText("Lessons of " + course.getCode() + " - " + course.getTitle());
            lessons = services.courses.getLessons(course.getCode());
            int number = 1;
            for (Lesson lesson : lessons) {
                lessonModel.addRow(new Object[] {number++, lesson.getTitle(), lesson.getYoutubeUrl()});
            }
        }
        updateButtons();
    }

    private void newCourse() {
        JTextField codeField = new JTextField(15);
        JTextField titleField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);
        String[] labels = {"Course code (e.g. CS101):", "Title:", "Description:"};
        JComponent[] fields = {codeField, titleField, descriptionField};

        while (Dialogs.showForm(panel, "New Course", labels, fields)) {
            try {
                services.courses.create(codeField.getText(), titleField.getText(), descriptionField.getText(),
                        teacher.getUsername(), false);
                refresh();
                selectCourse(codeField.getText().trim().toUpperCase());
                Dialogs.info(panel, "Course created. Students will see it after an admin approves it.");
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void deleteCourse() {
        Course course = selectedCourse();
        if (course != null && Dialogs.confirm(panel, "Delete " + course.getCode()
                + "? Its lessons, enrollments and student progress will be removed too.")) {
            services.courses.delete(course.getCode());
            refresh();
        }
    }

    private void addLesson() {
        Course course = selectedCourse();
        if (course == null) {
            return;
        }
        JTextField titleField = new JTextField(25);
        JTextField urlField = new JTextField(25);

        while (showLessonForm("Add Lesson", titleField, urlField)) {
            try {
                services.courses.addLesson(course.getCode(), titleField.getText(), urlField.getText());
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void editLesson() {
        Lesson lesson = selectedLesson();
        if (lesson == null) {
            return;
        }
        JTextField titleField = new JTextField(lesson.getTitle(), 25);
        JTextField urlField = new JTextField(lesson.getYoutubeUrl(), 25);

        while (showLessonForm("Edit Lesson", titleField, urlField)) {
            try {
                services.courses.updateLesson(lesson, titleField.getText(), urlField.getText());
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                Dialogs.error(panel, ex.getMessage());
            }
        }
    }

    private void deleteLesson() {
        Lesson lesson = selectedLesson();
        if (lesson != null && Dialogs.confirm(panel, "Delete lesson \"" + lesson.getTitle() + "\"?")) {
            services.courses.deleteLesson(lesson);
            refresh();
        }
    }

    private boolean showLessonForm(String title, JTextField titleField, JTextField urlField) {
        String[] labels = {"Lesson title:", "YouTube link:"};
        return Dialogs.showForm(panel, title, labels, new JComponent[] {titleField, urlField});
    }

    private void selectCourse(String code) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCode().equals(code)) {
                courseTable.setRowSelectionInterval(i, i);
                return;
            }
        }
        if (!courses.isEmpty()) {
            courseTable.setRowSelectionInterval(0, 0);
        }
    }

    private Course selectedCourse() {
        int row = courseTable.getSelectedRow();
        return (row >= 0 && row < courses.size()) ? courses.get(row) : null;
    }

    private Lesson selectedLesson() {
        int row = lessonTable.getSelectedRow();
        return (row >= 0 && row < lessons.size()) ? lessons.get(row) : null;
    }

    private void updateButtons() {
        boolean hasCourse = selectedCourse() != null;
        boolean hasLesson = selectedLesson() != null;
        deleteCourseButton.setEnabled(hasCourse);
        addLessonButton.setEnabled(hasCourse);
        editLessonButton.setEnabled(hasLesson);
        deleteLessonButton.setEnabled(hasLesson);
        watchButton.setEnabled(hasLesson);
    }
}
