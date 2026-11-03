package model;

// A student's hand-in for an assignment: a link to their work
public class Submission {
    private final String assignmentId;
    private final String studentUsername;
    private String link;
    private String submittedDate; // yyyy-MM-dd
    private Integer marks;
    private String feedback;

    public Submission(String assignmentId, String studentUsername, String link, String submittedDate) {
        this.assignmentId = assignmentId;
        this.studentUsername = studentUsername;
        this.link = link;
        this.submittedDate = submittedDate;
        this.feedback = "";
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    // null until the teacher grades it
    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public boolean isGraded() {
        return marks != null;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
