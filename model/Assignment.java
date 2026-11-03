package model;

public class Assignment {
    private final String id;
    private final String courseCode;
    private final String title;
    private final String description;
    private final String dueDate;
    private final int maxMarks;

    public Assignment(String id, String courseCode, String title, String description, String dueDate, int maxMarks) {
        this.id = id;
        this.courseCode = courseCode;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
    }

    public String getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public int getMaxMarks() {
        return maxMarks;
    }
}
