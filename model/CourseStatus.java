package model;

public enum CourseStatus {
    PENDING("Pending"),
    APPROVED("Approved");

    private final String displayName;

    CourseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
