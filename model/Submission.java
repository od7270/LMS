package model;

public class Submission {
    private final String assignmentId;
    private final String studentUsername;
    private final String content;
    private Integer marks;
    private String feedback;

    public Submission(String assignmentId, String studentUsername, String content) {
        this.assignmentId = assignmentId;
        this.studentUsername = studentUsername;
        this.content = content;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getContent() {
        return content;
    }

    // null until the teacher grades it
    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
