package ui.screens;

import model.Course;
import model.User;
import service.Services;
import ui.Screen;
import ui.Tables;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Teacher -> Students: progress of students enrolled in the teacher's courses
public class TeacherStudentsScreen implements Screen {
    private final User teacher;
    private final Services services;
    private final JPanel panel = new JPanel(new BorderLayout(5, 5));

    private final JComboBox<String> courseBox = new JComboBox<>();
    private List<Course> courses = new ArrayList<>();

    private final DefaultTableModel studentModel = Tables.model("Username", "Name", "Completed", "Progress");
    private final JLabel emptyLabel = new JLabel();

    public TeacherStudentsScreen(User teacher, Services services) {
        this.teacher = teacher;
        this.services = services;

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Course:"));
        top.add(courseBox);
        top.add(emptyLabel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(Tables.table(studentModel)), BorderLayout.CENTER);

        courseBox.addActionListener(e -> showStudents());
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void refresh() {
        int selected = courseBox.getSelectedIndex();

        courses = services.courses.getByTeacher(teacher.getUsername());
        courseBox.removeAllItems();
        for (Course course : courses) {
            courseBox.addItem(course.getCode() + " - " + course.getTitle());
        }
        if (selected >= 0 && selected < courses.size()) {
            courseBox.setSelectedIndex(selected);
        }
        showStudents();
    }

    private void showStudents() {
        studentModel.setRowCount(0);
        int index = courseBox.getSelectedIndex();
        if (index < 0 || index >= courses.size()) {
            emptyLabel.setText(courses.isEmpty() ? "You have no courses yet." : "");
            return;
        }

        String code = courses.get(index).getCode();
        int total = services.courses.getLessons(code).size();
        List<User> students = services.enrollments.getStudentsIn(code);
        emptyLabel.setText(students.isEmpty() ? "No students enrolled yet." : "");

        for (User student : students) {
            studentModel.addRow(new Object[] {
                    student.getUsername(),
                    student.getName(),
                    services.enrollments.completedCount(student.getUsername(), code) + "/" + total,
                    services.enrollments.percent(student.getUsername(), code) + "%"
            });
        }
    }
}
