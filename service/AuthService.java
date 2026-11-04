package service;

import model.User;

public class AuthService {
    private final DataStore store;

    public AuthService(DataStore store) {
        this.store = store;
    }

    // Returns the matching user, or null if the credentials are wrong
    public User login(String username, String password) {
        for (User user : store.getUsers()) {
            if (user.getUsername().equals(username) && Passwords.matches(password, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }
}
