package service;

import model.Assignment;
import model.Course;
import model.Submission;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AssignmentService {
    public static final String NOT_SUBMITTED = "Not submitted";
    public static final String SUBMITTED = "Submitted";
    public static final String LATE = "Late";
    public static final String GRADED = "Graded";

    private final DataStore store;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public AssignmentService(DataStore store, CourseService courseService, EnrollmentService enrollmentService) {
        this.store = store;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    // ---- Assignments ----

    public List<Assignment> getByCourse(String courseCode) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment assignment : store.getAssignments()) {
            if (assignment.getCourseCode().equals(courseCode)) {
                result.add(assignment);
            }
        }
        return result;
    }

    public List<Assignment> getByTeacher(String teacherUsername) {
        List<Assignment> result = new ArrayList<>();
        for (Course course : courseService.getByTeacher(teacherUsername)) {
            result.addAll(getByCourse(course.getCode()));
        }
        return result;
    }

    // Assignments of the student's approved, enrolled courses
    public List<Assignment> getForStudent(String studentUsername) {
        List<Assignment> result = new ArrayList<>();
        for (Course course : enrollmentService.getCoursesFor(studentUsername)) {
            result.addAll(getByCourse(course.getCode()));
        }
        return result;
    }

    public Assignment find(String id) {
        for (Assignment assignment : store.getAssignments()) {
            if (assignment.getId().equals(id)) {
                return assignment;
            }
        }
        return null;
    }

    public void create(String courseCode, String title, String description, String dueDateText, String maxMarksText) {
        if (courseCode == null) {
            throw new IllegalArgumentException("Choose a course for the assignment.");
        }
        title = DataStore.clean(title);
        description = DataStore.clean(description);
        String dueDate = parseDate(dueDateText);
        int maxMarks = parseMaxMarks(maxMarksText);
        requireTitle(title);

        int nextId = 1;
        for (Assignment assignment : store.getAssignments()) {
            try {
                nextId = Math.max(nextId, Integer.parseInt(assignment.getId()) + 1);
            } catch (NumberFormatException ignored) {
                // not one of our generated IDs
            }
        }
        store.getAssignments().add(new Assignment(String.valueOf(nextId), courseCode, title, description, dueDate, maxMarks));
        store.save();
    }

    public void update(Assignment assignment, String title, String description, String dueDateText, String maxMarksText) {
        title = DataStore.clean(title);
        description = DataStore.clean(description);
        String dueDate = parseDate(dueDateText);
        int maxMarks = parseMaxMarks(maxMarksText);
        requireTitle(title);

        for (Submission submission : getSubmissions(assignment.getId())) {
            if (submission.isGraded() && submission.getMarks() > maxMarks) {
                throw new IllegalArgumentException("Max marks can't be lower than marks already given ("
                        + submission.getMarks() + ").");
            }
        }

        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setMaxMarks(maxMarks);
        store.save();
    }

    // Deletes the assignment together with its submissions
    public void delete(Assignment assignment) {
        store.getAssignments().remove(assignment);
        store.getSubmissions().removeIf(s -> s.getAssignmentId().equals(assignment.getId()));
        store.save();
    }

    // ---- Submissions ----

    public List<Submission> getSubmissions(String assignmentId) {
        List<Submission> result = new ArrayList<>();
        for (Submission submission : store.getSubmissions()) {
            if (submission.getAssignmentId().equals(assignmentId)) {
                result.add(submission);
            }
        }
        return result;
    }

    public Submission findSubmission(String assignmentId, String studentUsername) {
        for (Submission submission : store.getSubmissions()) {
            if (submission.getAssignmentId().equals(assignmentId)
                    && submission.getStudentUsername().equals(studentUsername)) {
                return submission;
            }
        }
        return null;
    }

    // Creates the submission, or replaces the link if the student already submitted
    public void submit(String assignmentId, String studentUsername, String link) {
        link = DataStore.clean(link);
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            throw new IllegalArgumentException("Enter a link that starts with http:// or https://");
        }

        String today = LocalDate.now().toString();
        Submission submission = findSubmission(assignmentId, studentUsername);
        if (submission == null) {
            store.getSubmissions().add(new Submission(assignmentId, studentUsername, link, today));
        } else if (submission.isGraded()) {
            throw new IllegalArgumentException("This assignment has already been graded and can't be resubmitted.");
        } else {
            submission.setLink(link);
            submission.setSubmittedDate(today);
        }
        store.save();
    }

    public void grade(Submission submission, String marksText, String feedback) {
        Assignment assignment = find(submission.getAssignmentId());
        int max = assignment != null ? assignment.getMaxMarks() : Integer.MAX_VALUE;
        int marks;
        try {
            marks = Integer.parseInt(DataStore.clean(marksText));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Marks must be a whole number.");
        }
        if (marks < 0 || marks > max) {
            throw new IllegalArgumentException("Marks must be between 0 and " + max + ".");
        }

        submission.setMarks(marks);
        submission.setFeedback(DataStore.clean(feedback));
        store.save();
    }

    public boolean isLate(Submission submission, Assignment assignment) {
        return LocalDate.parse(submission.getSubmittedDate()).isAfter(LocalDate.parse(assignment.getDueDate()));
    }

    // Not submitted / Submitted / Late / Graded
    public String status(Assignment assignment, String studentUsername) {
        Submission submission = findSubmission(assignment.getId(), studentUsername);
        if (submission == null) {
            return NOT_SUBMITTED;
        }
        if (submission.isGraded()) {
            return GRADED;
        }
        return isLate(submission, assignment) ? LATE : SUBMITTED;
    }

    // ---- Validation ----

    private static void requireTitle(String title) {
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Assignment title is required.");
        }
    }

    private static String parseDate(String text) {
        try {
            return LocalDate.parse(DataStore.clean(text)).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Enter the due date as YYYY-MM-DD, e.g. 2026-12-31.");
        }
    }

    private static int parseMaxMarks(String text) {
        int marks;
        try {
            marks = Integer.parseInt(DataStore.clean(text));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Max marks must be a whole number.");
        }
        if (marks <= 0) {
            throw new IllegalArgumentException("Max marks must be more than 0.");
        }
        return marks;
    }
}
