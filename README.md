# Learning Management System (LMS)

A desktop Learning Management System built in Java using **Swing only**. It has one login screen and a separate dashboard for each of three user roles: **Student**, **Teacher** and **Admin**.

> **Status:** Phases 1–2 done. Phase 3 has started with its first feature, **Courses & Video Lessons**: teachers build courses from YouTube lessons, the admin approves courses, manages users and enrolls students, and students watch lessons and track their progress. Data is saved to text files. The other menu items are still placeholders (see [Roadmap](#roadmap)).

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Constraints](#constraints)
3. [How to Run](#how-to-run)
4. [Login Credentials](#login-credentials)
5. [Current Features](#current-features)
6. [Demo Walkthrough](#demo-walkthrough)
7. [Architecture](#architecture)
8. [Data Storage](#data-storage)
9. [Screen Layouts](#screen-layouts)
10. [Roadmap](#roadmap)
11. [Planned Features by Role](#planned-features-by-role)
12. [Open Decisions](#open-decisions)

---

## Project Overview

An LMS is where a school or college manages its courses, students and teachers. It has three kinds of users:

| Role | Purpose |
|---|---|
| **Student** | Watches the lessons of enrolled courses and tracks progress. Later: assignments, grades, attendance. |
| **Teacher** | Creates courses, adds YouTube video lessons, follows student progress. Later: assignments, grading, attendance. |
| **Admin** | Manages the system: adds/removes students and teachers, approves courses, enrolls students. Later: reports, settings. |

Each role logs in on the same login screen and gets its own dashboard.

---

## Constraints

- **GUI:** Java Swing only. No JavaFX and no third-party libraries.
- **Look:** stock Swing look. No custom Look-and-Feel, colors or fonts.
- **Components:** standard Swing classes (`JFrame`, `JPanel`, `JTable`, `JButton`, `JOptionPane`, etc.) are used directly.
- **Opening videos:** uses `java.awt.Desktop`, which is part of the JDK (Swing is built on AWT). No extra library.
- **Storage:** plain Java file I/O (`java.nio.file`). No database.

---

## How to Run

Requires a JDK (tested with Java 25).

Easiest way (macOS/Linux) is to run the script. It compiles every `.java` file into `out/` and starts the app:

```bash
./run.sh
```

To do it manually instead, compile everything into an `out/` folder and run from there (run this from the project folder so the app finds `data/`):

```bash
javac -d out $(find . -name "*.java" -not -path "./out/*")
java -cp out LearningManagementSystem
```

---

## Login Credentials

On first run the app creates `data/users.txt` with three demo accounts. All three use the password `admin`:

| Username | Password | Opens |
|---|---|---|
| `student` | `admin` | Student Dashboard |
| `teacher` | `admin` | Teacher Dashboard |
| `admin` | `admin` | Admin Dashboard |

The admin can add more students and teachers from the dashboard. To reset everything, close the app and delete the `data/` folder.

> Passwords are stored as **plain text** in `data/users.txt`. That's fine for a demo but not for real use (see [Open Decisions](#open-decisions)).

---

## Current Features

### Login screen
- Username and password fields (the password is masked).
- **Login** checks the credentials and opens the matching dashboard. **Enter** also logs in.
- **Cancel** clears both fields.
- Invalid credentials show a "Login Failed" error dialog.

### Dashboards (same frame for all three roles)
- **Top bar:** dashboard title; "Welcome, \<name\>" and a **Logout** button.
- **Side menu:** role-specific buttons. Each opens its screen in the content area, with fresh data every time.
- **Status bar:** "Logged in as: \<username\>".
- Menu items without a screen yet show "\<item\> - coming soon".

### Courses & Video Lessons ✅

How a course moves through the system:
```
Teacher creates course ──▶ PENDING ──(admin approves)──▶ APPROVED ──▶ visible to enrolled students
Admin creates course  ─────────────────────────────────▶ APPROVED (auto-approved)
Admin rejects / deletes ──▶ course removed along with its lessons, enrollments and progress
```
Approval happens **once per course**. Lessons added after approval show up for students right away.

| Role | Screen | What it does |
|---|---|---|
| **Teacher** | My Courses | Table of own courses (Code, Title, Status, Lessons, Students). **New Course** (code, title, description; starts as *Pending*), **Delete Course**. Below it, the selected course's lessons: **Add Lesson**, **Edit Lesson**, **Delete Lesson** (title + YouTube link) and **Watch Video**. |
| **Teacher** | Students | Choose one of your courses and see each enrolled student's progress (Completed x/y, %). |
| **Student** | My Courses | List of enrolled, **approved** courses with the % done. Selecting one shows the teacher, description, lessons table (Completed / Not completed) and a progress bar. **Watch Video** opens the lesson in the browser; **Mark Complete / Mark Incomplete** toggles the selected lesson. |
| **Admin** | Manage Courses | All courses (Code, Title, Teacher, Status, Lessons, Enrolled). **New Course** (pick a teacher; auto-approved), **Approve**, **Reject / Delete**, **View Lessons** (with Watch Video, to check a course before approving it), **Enrollments** (see progress, **Enroll Student**, **Remove**). |
| **Admin** | Manage Students | Table of students (Username, Name, Courses). **Add Student**, **Delete Student**. Deleting a student also removes their enrollments and progress. |
| **Admin** | Manage Teachers | Table of teachers (Username, Name, Courses). **Add Teacher**, **Delete Teacher**. A teacher can't be deleted while they still own courses. |

**Validation** (errors shown in a dialog; the form reopens with your input kept):
- Usernames and course codes must be unique and can't contain spaces. Course codes are stored in upper case (`cs101` → `CS101`).
- Name, username, password, course code, course title and lesson title are all required.
- A lesson link must be a YouTube link (`youtube.com/…` or `youtu.be/…`).
- Every delete and remove asks for confirmation.

**Watch Video:** opens the link in the default browser. If the system can't open a browser, a dialog shows the link so it can be copied.

| Role | Menu items (✅ = working) |
|---|---|
| Student | My Courses ✅, Assignments, Grades, Attendance, Timetable, Profile |
| Teacher | My Courses ✅, Create Assignment, Grade Submissions, Attendance, Students ✅, Profile |
| Admin | Manage Students ✅, Manage Teachers ✅, Manage Courses ✅, Reports, Settings |

---

## Demo Walkthrough

1. Log in as **admin**. In **Manage Students**, add `ravi`; in **Manage Teachers**, add `neha`. Log out.
2. Log in as **teacher**. In **My Courses**, click **New Course** and create `CS101` (it shows *Pending*). Select it and **Add Lesson** three times with YouTube links. Log out.
3. Log in as **admin**. In **Manage Courses**, select `CS101`, check it with **View Lessons**, then click **Approve**. Open **Enrollments** and enroll `student` and `ravi`. Log out.
4. Log in as **student**. In **My Courses**, select `CS101` and a lesson, click **Watch Video** (the browser opens), then **Mark Complete**. The progress bar shows 1/3 (33%).
5. Log in as **teacher**. **Students** → `CS101` shows `student` at 1/3 (33%).
6. Close the app and run it again. Everything is still there.

---

## Architecture

The code is split into three packages plus a small entry class:

| Package | What it holds |
|---|---|
| `model` | Plain data classes (users, courses, lessons…). No Swing code. |
| `service` | Logic and storage: login, users, courses/lessons, enrollments/progress, saving to files. No Swing code. |
| `ui` | All Swing code: the window, the login screen, the dashboards and their screens. |

`ui` depends on `service` and `model`; `service` depends on `model`; `model` depends on nothing. The GUI never touches files or lists directly; it always goes through a service.

### Files
```
lms/
├── LearningManagementSystem.java   # entry point: main() only
├── model/
│   ├── Role.java                   # enum: STUDENT, TEACHER, ADMIN
│   ├── User.java                   # abstract base class for all users
│   ├── Student.java / Teacher.java / Admin.java   # extend User
│   ├── Course.java                 # code, title, description, teacher, status
│   ├── CourseStatus.java           # enum: PENDING, APPROVED
│   ├── Lesson.java                 # one YouTube video in a course
│   ├── Enrollment.java             # student ↔ course
│   ├── LessonCompletion.java       # student completed a lesson
│   ├── Assignment.java             # (not used yet)
│   ├── Submission.java             # (not used yet) holds marks + feedback
│   └── AttendanceRecord.java       # (not used yet)
├── service/
│   ├── Services.java               # creates all services around one DataStore
│   ├── DataStore.java              # in-memory lists + load/save text files
│   ├── AuthService.java            # login check
│   ├── UserService.java            # add/delete/find students & teachers
│   ├── CourseService.java          # courses + lessons, approval, YouTube check
│   └── EnrollmentService.java      # enrollments, completion, progress %
├── ui/
│   ├── AppWindow.java              # the JFrame + switching login ↔ dashboard
│   ├── LoginScreen.java            # login form
│   ├── DashboardFactory.java       # builds a dashboard for a given user
│   ├── Screen.java                 # interface every content screen implements
│   ├── ScreenFactory.java          # menu item → Screen (or null = coming soon)
│   ├── Dialogs.java                # form / confirm / error helpers (JOptionPane)
│   ├── Tables.java                 # read-only JTable helpers
│   ├── BrowserUtil.java            # opens a link in the browser
│   └── screens/
│       ├── StudentCoursesScreen.java
│       ├── TeacherCoursesScreen.java
│       ├── TeacherStudentsScreen.java
│       ├── ManageCoursesScreen.java
│       └── ManageUsersScreen.java  # used for both students and teachers
├── data/                           # saved data (created on first run)
├── out/                            # compiled .class files (generated)
├── run.sh                          # compile + run script
└── README.md
```

### Classes

| Class | Responsibility |
|---|---|
| `LearningManagementSystem` | `main()` creates `Services` (with the `data/` folder) and an `AppWindow`, then shows it on the Swing Event Dispatch Thread. |
| `model.User` *(abstract)* | `id`, `name`, `username`, `password` (private). `checkPassword()` for login; `getPassword()` is used only by `DataStore` for saving. Abstract `getRole()` and `getMenuItems()`. |
| `model.Student` / `Teacher` / `Admin` | Each returns its own role and dashboard menu. |
| `model.Course` | `code` (unique ID), `title`, `description`, `teacherUsername`, `status`. |
| `model.Lesson` | `id`, `courseCode`, `title`, `youtubeUrl`. Lessons are shown in the order they were added. |
| `model.Enrollment` / `LessonCompletion` | Link records: which student is in which course, and which lessons they completed. |
| `service.DataStore` | Holds all lists in memory, loads them from `data/*.txt` on start and rewrites the files after every change. Seeds the demo users on first run. `clean()` strips tabs and line breaks from input so they can't break the file format. |
| `service.AuthService` | `login(username, password)` returns the `User`, or `null`. |
| `service.UserService` | `getUsers(role)`, `findByUsername`, `addUser` (validation, auto IDs like `S2`, `T3`), `deleteUser` (cleans up related records). |
| `service.CourseService` | `create`, `getAll`, `getByTeacher`, `find`, `approve`, `delete` (removes the course's lessons, enrollments and completions too); `getLessons`, `addLesson`, `updateLesson`, `deleteLesson`; YouTube link check. |
| `service.EnrollmentService` | `enroll`, `unenroll` (also clears that course's progress), `getStudentsIn`, `getCoursesFor` (approved courses only), `toggleComplete`, `isComplete`, `completedCount`, `percent`. |
| `service.Services` | Builds one `DataStore` and all services on top of it, so the UI only needs to receive one object. |
| `ui.AppWindow` | Owns the `JFrame` and the top-level `CardLayout` (login ↔ dashboard). |
| `ui.DashboardFactory` | Builds the dashboard frame. Its content area uses a second `CardLayout`, with one card per working screen plus a "coming soon" placeholder. |
| `ui.Screen` | Interface: `getPanel()` and `refresh()`. `refresh()` is called every time the menu button is clicked, so a screen always shows current data. |
| `ui.ScreenFactory` | Maps (role, menu item) → a `Screen` object, or `null` for items that aren't built yet. |
| `ui.screens.*` | One class per working screen (see [Current Features](#courses--video-lessons-)). |

Services report bad input by throwing `IllegalArgumentException` with a readable message. The screens catch it and show it with `Dialogs.error`.

### OOP concepts used

| Concept | Where |
|---|---|
| **Abstraction** | `User` is an abstract class and `Screen` is an interface. The dashboard only knows "a `Screen`" and calls `refresh()` on it without knowing which screen it is. |
| **Inheritance** | `Student`, `Teacher` and `Admin` extend `User`. |
| **Polymorphism** | `user.getMenuItems()`, `user.getRole()` and `screen.refresh()` behave differently depending on the object. `ManageUsersScreen` is one class reused for two roles. |
| **Encapsulation** | All fields are `private`. The UI never touches the data lists or files; it goes through the services. |
| **Composition** | `AppWindow` *has a* `JFrame`; `Services` *has a* `DataStore` shared by all services. |

### Class diagram
```
                    ┌──────────────────────────┐
                    │   «abstract» User        │
                    │ + checkPassword()        │
                    │ + getRole()      «abs»   │
                    │ + getMenuItems() «abs»   │
                    └────────────┬─────────────┘
               ┌─────────────────┼─────────────────┐
           Student            Teacher            Admin

 Course 1 ──── * Lesson          Course 1 ──── * Enrollment * ──── 1 Student
 Lesson 1 ──── * LessonCompletion * ──── 1 Student
 Course * ──── 1 Teacher (teacherUsername)

 ┌──────────── Services ─────────────┐
 │ AuthService   UserService          │
 │ CourseService EnrollmentService    │──uses──▶ DataStore ──reads/writes──▶ data/*.txt
 └────────────────────────────────────┘
          ▲
          │ uses
 AppWindow ──▶ LoginScreen
     └──▶ DashboardFactory ──▶ ScreenFactory ──creates──▶ «interface» Screen
                                                          ├── StudentCoursesScreen
                                                          ├── TeacherCoursesScreen
                                                          ├── TeacherStudentsScreen
                                                          ├── ManageCoursesScreen
                                                          └── ManageUsersScreen
```

### Screen switching (two levels of `CardLayout`)
```
JFrame (AppWindow)
└── CardLayout: "login" | "dashboard"
                              └── BorderLayout: top bar, side menu, status bar
                                  └── CENTER: CardLayout
                                        "placeholder" (coming soon)
                                        "My Courses"      → Screen
                                        "Students"        → Screen   ...
```
The dashboard is **rebuilt on every login** for the logged-in user. Clicking a menu button calls `screen.refresh()` and then shows that card.

### Login flow
```
LoginScreen ──▶ AuthService.login(username, password)
        ├── User ──▶ AppWindow.showDashboard(user) ──▶ DashboardFactory.create(user, services, logout)
        └── null ──▶ "Login Failed" dialog
Logout ──▶ AppWindow.showLogin()
```

---

## Data Storage

`DataStore` keeps everything in `ArrayList`s and saves them to **tab-separated text files** in `data/`. One line per record, and all files are rewritten after every change.

| File | Columns |
|---|---|
| `users.txt` | role, id, name, username, password |
| `courses.txt` | code, title, description, teacherUsername, status |
| `lessons.txt` | id, courseCode, title, youtubeUrl |
| `enrollments.txt` | courseCode, studentUsername |
| `completions.txt` | lessonId, studentUsername |

- Missing files are treated as empty. If `users.txt` is missing, the three demo users are created.
- Lines with the wrong number of columns are skipped.
- Deleting something also deletes the records that depend on it, so no dangling records are left:

| Delete… | Also removes |
|---|---|
| Course | its lessons, enrollments, and completions of its lessons |
| Lesson | completions of that lesson |
| Student | their enrollments and completions |
| Enrollment (Remove) | the student's completions in that course |
| Teacher | not allowed while they own courses |

---

## Screen Layouts

**Student → My Courses**
```
┌──────────────────────┬──────────────────────────────────────────────┐
│ My Courses           │ CS101 - Intro to Java                        │
│ ┌──────────────────┐ │ Teacher: Teacher                             │
│ │CS101 - Intro(33%)│ │ ┌──────────────────────────────────────────┐ │
│ │CS102 - DS   (0%) │ │ │ Basics of Java programming               │ │
│ └──────────────────┘ │ └──────────────────────────────────────────┘ │
│                      │  #  Lesson        Status                     │
│                      │  1  Variables     Completed                  │
│                      │  2  Loops         Not completed              │
│                      │  3  OOP Basics    Not completed              │
│                      │ [■■■■■■       Progress: 1/3 (33%)          ] │
│                      │        [Watch Video] [Mark Complete]         │
└──────────────────────┴──────────────────────────────────────────────┘
```

**Teacher → My Courses**
```
┌──────────────────────────────────────────────────────────┐
│ My Courses                                               │
│  Code   Title          Status    Lessons  Students       │
│  CS101  Intro to Java  Approved  3        2              │
│            [New Course] [Delete Course]                  │
├══════════════════════ (split pane) ══════════════════════┤
│ Lessons of CS101 - Intro to Java                         │
│  #  Title       YouTube Link                             │
│  1  Variables   https://www.youtube.com/watch?v=...      │
│ [Add Lesson] [Edit Lesson] [Delete Lesson] [Watch Video] │
└──────────────────────────────────────────────────────────┘
```

**Admin → Manage Courses**
```
┌──────────────────────────────────────────────────────────────────┐
│ All Courses                                                      │
│  Code   Title          Teacher  Status    Lessons  Enrolled      │
│  CS101  Intro to Java  Teacher  Approved  3        2             │
│  CS102  Data Struct.   Neha     Pending   1        0             │
│ [New Course] [Approve] [Reject / Delete] [View Lessons] [Enrollments] │
└──────────────────────────────────────────────────────────────────┘
```

---

## Roadmap

These phases are a proposal. We will adjust them as we decide on features.

### Phase 1: GUI skeleton ✅ *(done)*
- Login screen, three role dashboards, screen switching, logout.

### Phase 2: Split the code into classes ✅ *(done)*
- `model` / `service` / `ui` packages, `User` hierarchy, `AuthService`.

### Phase 3: Real content in the dashboards 🟡 *(in progress)*
- ✅ Nested `CardLayout` content area + `Screen` interface + `ScreenFactory`.
- ✅ **Courses & Video Lessons** (teacher courses/lessons, admin approval/enrollment/user management, student watch + progress).
- ⬜ Assignments & submissions (`Assignment`, `Submission` models already exist).
- ⬜ Grades, Attendance, Timetable, Profile, Reports, Settings.

### Phase 4: Data persistence 🟡 *(in progress)*
- ✅ Text-file storage via `DataStore` for users, courses, lessons, enrollments and completions.
- ⬜ Add a file for each new model as features arrive (assignments, attendance…).
- If the course allows it later, a database (JDBC) could replace the files.

### Phase 5: Real authentication 🟡 *(in progress)*
- ✅ Users are loaded from storage; the admin can add and delete them.
- ⬜ Change password (Profile), hash passwords.

### Phase 6: Polish
- ✅ Input validation, error dialogs and delete confirmations for the course feature.
- ⬜ Confirm on logout, minimum window size, search/sort in tables.

---

## Planned Features by Role

Candidate features for the remaining menu items. We haven't committed to any of them yet.

### Student
| Menu item | Planned feature |
|---|---|
| My Courses | ✅ Done |
| Assignments | Pending and submitted assignments with due dates; submit an assignment. |
| Grades | Grades per course/assignment. |
| Attendance | Attendance percentage per course. |
| Timetable | Weekly class schedule. |
| Profile | View own details; change password. |

### Teacher
| Menu item | Planned feature |
|---|---|
| My Courses | ✅ Done |
| Create Assignment | Form: course, title, description, due date, max marks. |
| Grade Submissions | List of submissions per assignment; enter marks and feedback. |
| Attendance | Pick a course and date, then tick present/absent for each student. |
| Students | ✅ Done (progress per course) |
| Profile | View own details; change password. |

### Admin
| Menu item | Planned feature |
|---|---|
| Manage Students | ✅ Add / delete. Later: edit, reset password. |
| Manage Teachers | ✅ Add / delete. Later: edit, reassign courses to another teacher. |
| Manage Courses | ✅ Done. Later: edit course details. |
| Reports | Summary counts (students, teachers, courses), completion and grade overviews. |
| Settings | System settings (e.g. academic term), reset passwords. |

---

## Open Decisions

- [ ] Final feature list for each role (which planned features we actually build).
- [x] ~~Storage: in-memory only, files, or a database.~~ Text files for now.
- [x] ~~Whether to split into multiple classes/packages now or after the first features.~~ Done in Phase 2.
- [x] ~~How students get into courses.~~ The admin enrolls them.
- [x] ~~Whether teacher courses need approval.~~ Yes, once per course; admin-created courses are auto-approved.
- [ ] Whether Admin can also act as a Teacher/Student (single role vs multiple roles per user).
- [ ] Whether passwords need hashing for the submission.
- [ ] Whether a teacher's courses can be moved to another teacher (so the teacher can be deleted).
