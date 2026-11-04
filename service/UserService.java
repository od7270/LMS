package service;

import model.Role;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final DataStore store;

    public UserService(DataStore store) {
        this.store = store;
    }

    public List<User> getUsers(Role role) {
        List<User> result = new ArrayList<>();
        for (User user : store.getUsers()) {
            if (user.getRole() == role) {
                result.add(user);
            }
        }
        return result;
    }

    public User findByUsername(String username) {
        for (User user : store.getUsers()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void addUser(Role role, String name, String username, String password) {
        name = DataStore.clean(name);
        username = DataStore.clean(username);
        password = DataStore.clean(password);

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Name, username and password are all required.");
        }
        if (username.contains(" ")) {
            throw new IllegalArgumentException("Username cannot contain spaces.");
        }
        if (findByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        store.getUsers().add(DataStore.createUser(role, nextId(role), name, username, Passwords.hash(password)));
        store.save();
    }

    public void deleteUser(String username) {
        User user = findByUsername(username);
        if (user == null) {
            return;
        }
        if (user.getRole() == Role.TEACHER) {
            boolean ownsCourses = store.getCourses().stream()
                    .anyMatch(c -> c.getTeacherUsername().equals(username));
            if (ownsCourses) {
                throw new IllegalArgumentException("This teacher still owns courses. Delete their courses first.");
            }
        }

        store.getUsers().remove(user);
        store.getEnrollments().removeIf(e -> e.getStudentUsername().equals(username));
        store.getCompletions().removeIf(c -> c.getStudentUsername().equals(username));
        store.getSubmissions().removeIf(s -> s.getStudentUsername().equals(username));
        store.getAttendance().removeIf(a -> a.getStudentUsername().equals(username));
        store.save();
    }

    // IDs look like S1, S2... / T1... / A1...
    private String nextId(Role role) {
        String prefix = role.name().substring(0, 1);
        int max = 0;
        for (User user : store.getUsers()) {
            String id = user.getId();
            if (id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {
                    // not one of our generated IDs
                }
            }
        }
        return prefix + (max + 1);
    }
}
