package model;

public class Course {
    private final String code;
    private final String title;
    private final String description;
    private final String teacherUsername;
    private CourseStatus status;

    public Course(String code, String title, String description, String teacherUsername, CourseStatus status) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.teacherUsername = teacherUsername;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public boolean isApproved() {
        return status == CourseStatus.APPROVED;
    }
}
