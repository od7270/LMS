package model;

public class Assignment {
    private final String id;
    private final String courseCode;
    private String title;
    private String description;
    private String dueDate; // yyyy-MM-dd
    private int maxMarks;

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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }
}
