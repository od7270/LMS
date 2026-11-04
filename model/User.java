package model;

public abstract class User {
    private final String id;
    private final String name;
    private final String username;
    private String passwordHash;

    // passwordHash is a salted hash made by service.Passwords (or a plain password from an old file,
    // which DataStore hashes on load)
    public User(String id, String name, String username, String passwordHash) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
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

    // Used by DataStore to save users and by AuthService/UserService to check passwords
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public abstract Role getRole();

    public abstract String[] getMenuItems();
}
