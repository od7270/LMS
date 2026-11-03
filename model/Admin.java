package model;

public class Admin extends User {
    public Admin(String id, String name, String username, String password) {
        super(id, name, username, password);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String[] getMenuItems() {
        return new String[] {"Manage Students", "Manage Teachers", "Manage Courses", "Reports", "Settings"};
    }
}
