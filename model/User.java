package model;

public abstract class User {
    private final String id;
    private final String name;
    private final String username;
    private final String password;

    public User(String id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    // Used only by DataStore to save users; the UI should use checkPassword()
    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public abstract Role getRole();

    public abstract String[] getMenuItems();
}
