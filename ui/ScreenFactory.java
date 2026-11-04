package ui;

import model.Role;
import model.User;
import service.Services;
import ui.screens.ManageCoursesScreen;
import ui.screens.ManageTimetableScreen;
import ui.screens.ManageUsersScreen;
import ui.screens.ProfileScreen;
import ui.screens.StudentAssignmentsScreen;
import ui.screens.StudentAttendanceScreen;
import ui.screens.StudentCoursesScreen;
import ui.screens.StudentGradesScreen;
import ui.screens.TeacherAssignmentsScreen;
import ui.screens.TeacherAttendanceScreen;
import ui.screens.TeacherCoursesScreen;
import ui.screens.TeacherGradingScreen;
import ui.screens.TeacherStudentsScreen;

public class ScreenFactory {
    // Returns the screen for a menu item, or null if it isn't built yet
    public static Screen create(User user, String menuItem, Services services) {
        // Every role has the same Profile screen
        if (menuItem.equals("Profile")) {
            return new ProfileScreen(user, services);
        }
        switch (user.getRole()) {
            case STUDENT:
                if (menuItem.equals("My Courses")) {
                    return new StudentCoursesScreen(user, services);
                }
                if (menuItem.equals("Assignments")) {
                    return new StudentAssignmentsScreen(user, services);
                }
                if (menuItem.equals("Grades")) {
                    return new StudentGradesScreen(user, services);
                }
                if (menuItem.equals("Attendance")) {
                    return new StudentAttendanceScreen(user, services);
                }
                break;
            case TEACHER:
                if (menuItem.equals("My Courses")) {
                    return new TeacherCoursesScreen(user, services);
                }
                if (menuItem.equals("Assignments")) {
                    return new TeacherAssignmentsScreen(user, services);
                }
                if (menuItem.equals("Grade Submissions")) {
                    return new TeacherGradingScreen(user, services);
                }
                if (menuItem.equals("Attendance")) {
                    return new TeacherAttendanceScreen(user, services);
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
                if (menuItem.equals("Timetable")) {
                    return new ManageTimetableScreen(services);
                }
                break;
        }
        return null;
    }
}
