package model;

public class Admin extends User {
    public Admin(String id, String name, String username, String passwordHash) {
        super(id, name, username, passwordHash);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String[] getMenuItems() {
        return new String[] {"Manage Students", "Manage Teachers", "Manage Courses", "Reports", "Settings", "Profile"};
    }
}
