package model;

public class Enrollment {
    private final String courseCode;
    private final String studentUsername;

    public Enrollment(String courseCode, String studentUsername) {
        this.courseCode = courseCode;
        this.studentUsername = studentUsername;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getStudentUsername() {
        return studentUsername;
    }
}
