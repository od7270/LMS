package ui;

import model.Role;
import model.User;
import service.Services;
import ui.screens.ManageCoursesScreen;
import ui.screens.ManageUsersScreen;
import ui.screens.StudentCoursesScreen;
import ui.screens.TeacherCoursesScreen;
import ui.screens.TeacherStudentsScreen;

public class ScreenFactory {
    // Returns the screen for a menu item, or null if it isn't built yet
    public static Screen create(User user, String menuItem, Services services) {
        switch (user.getRole()) {
            case STUDENT:
                if (menuItem.equals("My Courses")) {
                    return new StudentCoursesScreen(user, services);
                }
                break;
            case TEACHER:
                if (menuItem.equals("My Courses")) {
                    return new TeacherCoursesScreen(user, services);
                }
                if (menuItem.equals("Students")) {
                    return new TeacherStudentsScreen(user, services);
                }
                break;
            case ADMIN:
                if (menuItem.equals("Manage Students")) {
                    return new ManageUsersScreen(Role.STUDENT, services);
                }
                if (menuItem.equals("Manage Teachers")) {
                    return new ManageUsersScreen(Role.TEACHER, services);
                }
                if (menuItem.equals("Manage Courses")) {
                    return new ManageCoursesScreen(services);
                }
                break;
        }
        return null;
    }
}
