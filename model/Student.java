package model;

public class Student extends User {
    public Student(String id, String name, String username, String passwordHash) {
        super(id, name, username, passwordHash);
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    @Override
    public String[] getMenuItems() {
        return new String[] {"My Courses", "Assignments", "Grades", "Attendance", "Timetable", "Profile"};
    }
}
