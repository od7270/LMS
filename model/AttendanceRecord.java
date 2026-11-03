package model;

public class AttendanceRecord {
    private final String courseCode;
    private final String studentUsername;
    private final String date;
    private boolean present;

    public AttendanceRecord(String courseCode, String studentUsername, String date, boolean present) {
        this.courseCode = courseCode;
        this.studentUsername = studentUsername;
        this.date = date;
        this.present = present;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getDate() {
        return date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
