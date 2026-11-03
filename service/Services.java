package service;

import java.nio.file.Path;

// Creates every service once, all sharing the same DataStore
public class Services {
    public final AuthService auth;
    public final UserService users;
    public final CourseService courses;
    public final EnrollmentService enrollments;

    public Services(Path dataFolder) {
        DataStore store = new DataStore(dataFolder);
        auth = new AuthService(store);
        users = new UserService(store);
        courses = new CourseService(store);
        enrollments = new EnrollmentService(store, courses, users);
    }
}
