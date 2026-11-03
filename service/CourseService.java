package service;

import model.Course;
import model.CourseStatus;
import model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class CourseService {
    private final DataStore store;

    public CourseService(DataStore store) {
        this.store = store;
    }

    // ---- Courses ----

    public List<Course> getAll() {
        return new ArrayList<>(store.getCourses());
    }

    public List<Course> getByTeacher(String teacherUsername) {
        List<Course> result = new ArrayList<>();
        for (Course course : store.getCourses()) {
            if (course.getTeacherUsername().equals(teacherUsername)) {
                result.add(course);
            }
        }
        return result;
    }

    public Course find(String code) {
        for (Course course : store.getCourses()) {
            if (course.getCode().equalsIgnoreCase(code)) {
                return course;
            }
        }
        return null;
    }

    public void create(String code, String title, String description, String teacherUsername, boolean approved) {
        code = DataStore.clean(code).toUpperCase();
        title = DataStore.clean(title);
        description = DataStore.clean(description);

        if (code.isEmpty() || title.isEmpty()) {
            throw new IllegalArgumentException("Course code and title are required.");
        }
        if (code.contains(" ")) {
            throw new IllegalArgumentException("Course code cannot contain spaces.");
        }
        if (find(code) != null) {
            throw new IllegalArgumentException("A course with code " + code + " already exists.");
        }
        if (teacherUsername == null) {
            throw new IllegalArgumentException("Choose a teacher for the course.");
        }

        CourseStatus status = approved ? CourseStatus.APPROVED : CourseStatus.PENDING;
        store.getCourses().add(new Course(code, title, description, teacherUsername, status));
        store.save();
    }

    public void approve(String code) {
        Course course = find(code);
        if (course != null) {
            course.setStatus(CourseStatus.APPROVED);
            store.save();
        }
    }

    // Deletes the course together with its lessons, enrollments and completions
    public void delete(String code) {
        List<Integer> lessonIds = new ArrayList<>();
        for (Lesson lesson : getLessons(code)) {
            lessonIds.add(lesson.getId());
        }

        store.getCourses().removeIf(c -> c.getCode().equals(code));
        store.getLessons().removeIf(l -> l.getCourseCode().equals(code));
        store.getEnrollments().removeIf(e -> e.getCourseCode().equals(code));
        store.getCompletions().removeIf(c -> lessonIds.contains(c.getLessonId()));
        store.save();
    }

    // ---- Lessons ----

    public List<Lesson> getLessons(String courseCode) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson lesson : store.getLessons()) {
            if (lesson.getCourseCode().equals(courseCode)) {
                result.add(lesson);
            }
        }
        return result;
    }

    public void addLesson(String courseCode, String title, String youtubeUrl) {
        title = DataStore.clean(title);
        youtubeUrl = DataStore.clean(youtubeUrl);
        validateLesson(title, youtubeUrl);

        int nextId = 1;
        for (Lesson lesson : store.getLessons()) {
            nextId = Math.max(nextId, lesson.getId() + 1);
        }
        store.getLessons().add(new Lesson(nextId, courseCode, title, youtubeUrl));
        store.save();
    }

    public void updateLesson(Lesson lesson, String title, String youtubeUrl) {
        title = DataStore.clean(title);
        youtubeUrl = DataStore.clean(youtubeUrl);
        validateLesson(title, youtubeUrl);

        lesson.setTitle(title);
        lesson.setYoutubeUrl(youtubeUrl);
        store.save();
    }

    public void deleteLesson(Lesson lesson) {
        store.getLessons().remove(lesson);
        store.getCompletions().removeIf(c -> c.getLessonId() == lesson.getId());
        store.save();
    }

    private void validateLesson(String title, String youtubeUrl) {
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Lesson title is required.");
        }
        if (!isYoutubeLink(youtubeUrl)) {
            throw new IllegalArgumentException("Enter a valid YouTube link, e.g. https://www.youtube.com/watch?v=...");
        }
    }

    private boolean isYoutubeLink(String url) {
        return url.startsWith("http") && (url.contains("youtube.com/") || url.contains("youtu.be/"));
    }
}
