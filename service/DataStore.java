package service;

import model.Admin;
import model.Assignment;
import model.Course;
import model.CourseStatus;
import model.Enrollment;
import model.Lesson;
import model.LessonCompletion;
import model.Role;
import model.Student;
import model.Submission;
import model.Teacher;
import model.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Keeps all data in memory and saves it to tab-separated text files in one folder
public class DataStore {
    private final Path folder;

    private final List<User> users = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();
    private final List<Lesson> lessons = new ArrayList<>();
    private final List<Enrollment> enrollments = new ArrayList<>();
    private final List<LessonCompletion> completions = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final List<Submission> submissions = new ArrayList<>();

    public DataStore(Path folder) {
        this.folder = folder;
        load();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public List<LessonCompletion> getCompletions() {
        return completions;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    // Makes user input safe to store: tabs and line breaks would break the file format
    public static String clean(String text) {
        return text == null ? "" : text.replaceAll("[\\t\\r\\n]+", " ").trim();
    }

    private void load() {
        for (String[] row : read("users.txt", 5)) {
            users.add(createUser(Role.valueOf(row[0]), row[1], row[2], row[3], row[4]));
        }
        for (String[] row : read("courses.txt", 5)) {
            courses.add(new Course(row[0], row[1], row[2], row[3], CourseStatus.valueOf(row[4])));
        }
        for (String[] row : read("lessons.txt", 4)) {
            lessons.add(new Lesson(Integer.parseInt(row[0]), row[1], row[2], row[3]));
        }
        for (String[] row : read("enrollments.txt", 2)) {
            enrollments.add(new Enrollment(row[0], row[1]));
        }
        for (String[] row : read("completions.txt", 2)) {
            completions.add(new LessonCompletion(Integer.parseInt(row[0]), row[1]));
        }
        for (String[] row : read("assignments.txt", 6)) {
            assignments.add(new Assignment(row[0], row[1], row[2], row[3], row[4], Integer.parseInt(row[5])));
        }
        for (String[] row : read("submissions.txt", 6)) {
            Submission submission = new Submission(row[0], row[1], row[2], row[3]);
            submission.setMarks(row[4].isEmpty() ? null : Integer.valueOf(row[4]));
            submission.setFeedback(row[5]);
            submissions.add(submission);
        }

        if (users.isEmpty()) {
            // First run: create the demo accounts
            users.add(new Student("S1", "Student", "student", "admin"));
            users.add(new Teacher("T1", "Teacher", "teacher", "admin"));
            users.add(new Admin("A1", "Admin", "admin", "admin"));
            save();
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(join(u.getRole().name(), u.getId(), u.getName(), u.getUsername(), u.getPassword()));
        }
        write("users.txt", lines);

        lines = new ArrayList<>();
        for (Course c : courses) {
            lines.add(join(c.getCode(), c.getTitle(), c.getDescription(), c.getTeacherUsername(), c.getStatus().name()));
        }
        write("courses.txt", lines);

        lines = new ArrayList<>();
        for (Lesson l : lessons) {
            lines.add(join(String.valueOf(l.getId()), l.getCourseCode(), l.getTitle(), l.getYoutubeUrl()));
        }
        write("lessons.txt", lines);

        lines = new ArrayList<>();
        for (Enrollment e : enrollments) {
            lines.add(join(e.getCourseCode(), e.getStudentUsername()));
        }
        write("enrollments.txt", lines);

        lines = new ArrayList<>();
        for (LessonCompletion c : completions) {
            lines.add(join(String.valueOf(c.getLessonId()), c.getStudentUsername()));
        }
        write("completions.txt", lines);

        lines = new ArrayList<>();
        for (Assignment a : assignments) {
            lines.add(join(a.getId(), a.getCourseCode(), a.getTitle(), a.getDescription(), a.getDueDate(),
                    String.valueOf(a.getMaxMarks())));
        }
        write("assignments.txt", lines);

        lines = new ArrayList<>();
        for (Submission s : submissions) {
            String marks = s.isGraded() ? String.valueOf(s.getMarks()) : "";
            lines.add(join(s.getAssignmentId(), s.getStudentUsername(), s.getLink(), s.getSubmittedDate(),
                    marks, s.getFeedback()));
        }
        write("submissions.txt", lines);
    }

    public static User createUser(Role role, String id, String name, String username, String password) {
        switch (role) {
            case STUDENT:
                return new Student(id, name, username, password);
            case TEACHER:
                return new Teacher(id, name, username, password);
            default:
                return new Admin(id, name, username, password);
        }
    }

    private List<String[]> read(String fileName, int columns) {
        List<String[]> rows = new ArrayList<>();
        Path file = folder.resolve(fileName);
        if (!Files.exists(file)) {
            return rows;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                String[] row = line.split("\t", -1);
                if (row.length == columns) {
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        return rows;
    }

    private void write(String fileName, List<String> lines) {
        try {
            Files.createDirectories(folder);
            Files.write(folder.resolve(fileName), lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + fileName, e);
        }
    }

    private static String join(String... values) {
        return String.join("\t", values);
    }
}
