package model;

public class Teacher extends User {
    public Teacher(String id, String name, String username, String password) {
        super(id, name, username, password);
    }

    @Override
    public Role getRole() {
        return Role.TEACHER;
    }

    @Override
    public String[] getMenuItems() {
        return new String[] {"My Courses", "Assignments", "Grade Submissions", "Attendance", "Students", "Profile"};
    }
}
