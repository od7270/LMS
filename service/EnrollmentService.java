package service;

import model.Course;
import model.Enrollment;
import model.Lesson;
import model.LessonCompletion;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentService {
    private final DataStore store;
    private final CourseService courseService;
    private final UserService userService;

    public EnrollmentService(DataStore store, CourseService courseService, UserService userService) {
        this.store = store;
        this.courseService = courseService;
        this.userService = userService;
    }

    public boolean isEnrolled(String studentUsername, String courseCode) {
        for (Enrollment e : store.getEnrollments()) {
            if (e.getStudentUsername().equals(studentUsername) && e.getCourseCode().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }

    public void enroll(String studentUsername, String courseCode) {
        if (!isEnrolled(studentUsername, courseCode)) {
            store.getEnrollments().add(new Enrollment(courseCode, studentUsername));
            store.save();
        }
    }

    // Removes the student from the course and clears their progress in it
    public void unenroll(String studentUsername, String courseCode) {
        List<Integer> lessonIds = lessonIds(courseCode);
        store.getEnrollments().removeIf(e ->
                e.getStudentUsername().equals(studentUsername) && e.getCourseCode().equals(courseCode));
        store.getCompletions().removeIf(c ->
                c.getStudentUsername().equals(studentUsername) && lessonIds.contains(c.getLessonId()));
        store.save();
    }

    public List<User> getStudentsIn(String courseCode) {
        List<User> result = new ArrayList<>();
        for (Enrollment e : store.getEnrollments()) {
            if (e.getCourseCode().equals(courseCode)) {
                User student = userService.findByUsername(e.getStudentUsername());
                if (student != null) {
                    result.add(student);
                }
            }
        }
        return result;
    }

    // Only approved courses are visible to students
    public List<Course> getCoursesFor(String studentUsername) {
        List<Course> result = new ArrayList<>();
        for (Enrollment e : store.getEnrollments()) {
            if (e.getStudentUsername().equals(studentUsername)) {
                Course course = courseService.find(e.getCourseCode());
                if (course != null && course.isApproved()) {
                    result.add(course);
                }
            }
        }
        return result;
    }

    public boolean isComplete(String studentUsername, int lessonId) {
        for (LessonCompletion c : store.getCompletions()) {
            if (c.getStudentUsername().equals(studentUsername) && c.getLessonId() == lessonId) {
                return true;
            }
        }
        return false;
    }

    public void toggleComplete(String studentUsername, int lessonId) {
        if (isComplete(studentUsername, lessonId)) {
            store.getCompletions().removeIf(c ->
                    c.getStudentUsername().equals(studentUsername) && c.getLessonId() == lessonId);
        } else {
            store.getCompletions().add(new LessonCompletion(lessonId, studentUsername));
        }
        store.save();
    }

    public int completedCount(String studentUsername, String courseCode) {
        int count = 0;
        for (int lessonId : lessonIds(courseCode)) {
            if (isComplete(studentUsername, lessonId)) {
                count++;
            }
        }
        return count;
    }

    public int percent(String studentUsername, String courseCode) {
        int total = courseService.getLessons(courseCode).size();
        if (total == 0) {
            return 0;
        }
        return completedCount(studentUsername, courseCode) * 100 / total;
    }

    private List<Integer> lessonIds(String courseCode) {
        List<Integer> ids = new ArrayList<>();
        for (Lesson lesson : courseService.getLessons(courseCode)) {
            ids.add(lesson.getId());
        }
        return ids;
    }
}
