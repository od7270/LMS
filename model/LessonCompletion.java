package model;

// A student has completed a lesson when one of these exists for the pair
public class LessonCompletion {
    private final int lessonId;
    private final String studentUsername;

    public LessonCompletion(int lessonId, String studentUsername) {
        this.lessonId = lessonId;
        this.studentUsername = studentUsername;
    }

    public int getLessonId() {
        return lessonId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }
}
