# Learning Management System (LMS)

A desktop Learning Management System built in Java using **Swing only**. It has one login screen and a separate dashboard for each of three user roles: **Student**, **Teacher** and **Admin**.

> **Status:** Phases 1–2 done. Phase 3 has two features working: **Courses & Video Lessons** (teachers build courses from YouTube lessons, the admin approves courses, manages users and enrolls students, students watch lessons and track their progress) **Assignments & Grades** (teachers set assignments, students submit a link to their work, teachers grade it, students see their marks) and **Attendance** (teachers tick who was present each day, students see their percentage with a warning below 75%). Data is saved to text files. The other menu items are still placeholders (see [Roadmap](#roadmap)).

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
| **Student** | Watches the lessons of enrolled courses and tracks progress, submits assignments, sees grades and attendance. Later: timetable, profile. |
| **Teacher** | Creates courses, adds YouTube video lessons, follows student progress, sets and grades assignments, takes attendance. Later: profile. |
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

### Assignments & Grades ✅

How an assignment moves through the system:
```
Teacher creates assignment (course, due date, max marks)
  ──▶ students of that course see it (approved courses only) ── "Not submitted"
  ──▶ student submits a link to their work ── "Submitted", or "Late" if after the due date
  ──▶ student can resubmit (replaces the link) until it is graded
  ──▶ teacher opens the link and grades it (marks + feedback) ── "Graded"; resubmitting is locked
```

| Role | Screen | What it does |
|---|---|---|
| **Teacher** | Assignments | Table of the assignments in own courses (Course, Title, Due, Max Marks, Submitted x/enrolled, Graded) with the selected one's description. **New Assignment** (course, title, description, due date, max marks), **Edit**, **Delete** (also deletes its submissions). |
| **Teacher** | Grade Submissions | Choose an assignment and see every enrolled student (Status, Submitted On, Marks, Feedback). **Open Link** opens the student's work in the browser; **Grade** enters marks and feedback (can be changed later). |
| **Student** | Assignments | Assignments of all enrolled, approved courses (Course, Title, Due, Max Marks, Status) with the description and the submitted link. **Submit Link** / **Resubmit** (disabled once graded), **Open My Link**. |
| **Student** | Grades | Every graded assignment (Marks x/max, %, Feedback), and below it a total per course over the graded assignments. |

**Validation:**
- Assignment title is required. The due date must be a real date written as `YYYY-MM-DD`. Max marks must be a whole number above 0, and can't be lowered below marks already given.
- A submission must be a link starting with `http://` or `https://` (Google Drive, GitHub, etc.).
- Marks must be a whole number from 0 to the assignment's max marks.

### Attendance ✅

| Role | Screen | What it does |
|---|---|---|
| **Teacher** | Attendance | Choose a course. The left list shows every day attendance was taken (newest first). The table lists the enrolled students with a **Present** checkbox and their overall attendance (e.g. `3/4 (75%)`, with "Below 75%" when low). **New Day** (date, defaults to today; every student starts ticked present), **Delete Day**, **All Present**, **All Absent**, **Save**. Past days can be opened and corrected. |
| **Student** | Attendance | One row per enrolled course: Present, Total, %, Status (*OK*, *Below 75%* or *No classes yet*). Selecting a course lists its days as Present / Absent. |

- Dates must be real dates written as `YYYY-MM-DD` and can't be in the future.
- A student only counts the days that have a record for them, so a student enrolled mid-term isn't marked absent for earlier days. On an older day they show unticked and are only saved if the teacher ticks or unticks them.
- Switching course or day with unsaved ticks asks before discarding them.

| Role | Menu items (✅ = working) |
|---|---|
| Student | My Courses ✅, Assignments ✅, Grades ✅, Attendance ✅, Timetable, Profile |
| Teacher | My Courses ✅, Assignments ✅, Grade Submissions ✅, Attendance ✅, Students ✅, Profile |
| Admin | Manage Students ✅, Manage Teachers ✅, Manage Courses ✅, Reports, Settings |

---

## Demo Walkthrough

1. Log in as **admin**. In **Manage Students**, add `ravi`; in **Manage Teachers**, add `neha`. Log out.
2. Log in as **teacher**. In **My Courses**, click **New Course** and create `CS101` (it shows *Pending*). Select it and **Add Lesson** three times with YouTube links. Log out.
3. Log in as **admin**. In **Manage Courses**, select `CS101`, check it with **View Lessons**, then click **Approve**. Open **Enrollments** and enroll `student` and `ravi`. Log out.
4. Log in as **student**. In **My Courses**, select `CS101` and a lesson, click **Watch Video** (the browser opens), then **Mark Complete**. The progress bar shows 1/3 (33%).
5. Log in as **teacher**. **Students** → `CS101` shows `student` at 1/3 (33%). In **Assignments**, click **New Assignment** and create `HW1` for `CS101` (due date in the future, max marks 10). Log out.
6. Log in as **student**. In **Assignments**, select `HW1`, click **Submit Link** and paste a link (e.g. a GitHub repo). The status shows *Submitted*. Log out.
7. Log in as **teacher**. In **Grade Submissions**, choose `HW1`, select `student`, click **Open Link**, then **Grade** with 8 and some feedback. Log out.
8. Log in as **student**. **Assignments** shows `HW1` as *Graded* (Resubmit is disabled); **Grades** shows 8/10 (80%) and the course total. Log out.
9. Log in as **teacher**. In **Attendance**, choose `CS101`, click **New Day** (today), untick `ravi`, click **Save**. `ravi` shows `0/1 (0%) - Below 75%`. Log out.
10. Log in as **student**. **Attendance** shows `CS101` at 1/1 (100%), *OK*.
11. Close the app and run it again. Everything is still there.

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
│   ├── Assignment.java             # course, title, description, due date, max marks
│   ├── Submission.java             # student's link + date, marks + feedback
│   └── AttendanceRecord.java       # student present/absent in a course on a date
├── service/
│   ├── Services.java               # creates all services around one DataStore
│   ├── DataStore.java              # in-memory lists + load/save text files
│   ├── AuthService.java            # login check
│   ├── UserService.java            # add/delete/find students & teachers
│   ├── CourseService.java          # courses + lessons, approval, YouTube check
│   ├── EnrollmentService.java      # enrollments, completion, progress %
│   ├── AssignmentService.java      # assignments, submissions, grading, status
│   └── AttendanceService.java      # attendance days, present/absent, % and 75% warning
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
│       ├── StudentAssignmentsScreen.java
│       ├── StudentAttendanceScreen.java
│       ├── StudentGradesScreen.java
│       ├── TeacherCoursesScreen.java
│       ├── TeacherAssignmentsScreen.java
│       ├── TeacherAttendanceScreen.java
│       ├── TeacherGradingScreen.java
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
| `model.Assignment` | `id`, `courseCode`, `title`, `description`, `dueDate` (`YYYY-MM-DD`), `maxMarks`. |
| `model.Submission` | `assignmentId`, `studentUsername`, `link`, `submittedDate`, `marks` (`null` until graded), `feedback`. |
| `model.AttendanceRecord` | `courseCode`, `studentUsername`, `date`, `present`. |
| `service.DataStore` | Holds all lists in memory, loads them from `data/*.txt` on start and rewrites the files after every change. Seeds the demo users on first run. `clean()` strips tabs and line breaks from input so they can't break the file format. |
| `service.AuthService` | `login(username, password)` returns the `User`, or `null`. |
| `service.UserService` | `getUsers(role)`, `findByUsername`, `addUser` (validation, auto IDs like `S2`, `T3`), `deleteUser` (cleans up related records). |
| `service.CourseService` | `create`, `getAll`, `getByTeacher`, `find`, `approve`, `delete` (removes the course's lessons, enrollments, completions, assignments, submissions and attendance too); `getLessons`, `addLesson`, `updateLesson`, `deleteLesson`; YouTube link check. |
| `service.EnrollmentService` | `enroll`, `unenroll` (also clears that course's progress, submissions and attendance), `getStudentsIn`, `getCoursesFor` (approved courses only), `toggleComplete`, `isComplete`, `completedCount`, `percent`. |
| `service.AssignmentService` | `getByCourse`, `getByTeacher`, `getForStudent` (approved courses only), `create`, `update`, `delete`; `submit` (create or resubmit until graded), `grade`, `findSubmission`, `getSubmissions`, `status` (Not submitted / Submitted / Late / Graded), `isLate`. |
| `service.AttendanceService` | `getDays` (newest first), `find`, `saveDay` (date check, no future dates; adds or updates one record per student), `deleteDay`, `getRecords`, `presentCount`, `totalCount`, `percent`, `isLow` (below `THRESHOLD` = 75%). |
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
 Course 1 ──── * Assignment 1 ──── * Submission * ──── 1 Student
 Course 1 ──── * AttendanceRecord * ──── 1 Student
 Course * ──── 1 Teacher (teacherUsername)

 ┌──────────── Services ─────────────┐
 │ AuthService   UserService          │
 │ CourseService EnrollmentService    │
 │ AssignmentService AttendanceService│──uses──▶ DataStore ──reads/writes──▶ data/*.txt
 └────────────────────────────────────┘
          ▲
          │ uses
 AppWindow ──▶ LoginScreen
     └──▶ DashboardFactory ──▶ ScreenFactory ──creates──▶ «interface» Screen
                                                          ├── StudentCoursesScreen
                                                          ├── StudentAssignmentsScreen
                                                          ├── StudentAttendanceScreen
                                                          ├── StudentGradesScreen
                                                          ├── TeacherCoursesScreen
                                                          ├── TeacherAssignmentsScreen
                                                          ├── TeacherAttendanceScreen
                                                          ├── TeacherGradingScreen
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
| `assignments.txt` | id, courseCode, title, description, dueDate, maxMarks |
| `submissions.txt` | assignmentId, studentUsername, link, submittedDate, marks (empty until graded), feedback |
| `attendance.txt` | courseCode, studentUsername, date, present (`true`/`false`) |

- Missing files are treated as empty. If `users.txt` is missing, the three demo users are created.
- Lines with the wrong number of columns are skipped.
- Deleting something also deletes the records that depend on it, so no dangling records are left:

| Delete… | Also removes |
|---|---|
| Course | its lessons, enrollments, completions of its lessons, its assignments and their submissions, its attendance |
| Lesson | completions of that lesson |
| Assignment | its submissions |
| Student | their enrollments, completions, submissions and attendance |
| Enrollment (Remove) | the student's completions, submissions and attendance in that course |
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
- ✅ **Assignments & Grades** (teacher creates and grades assignments, student submits a link and sees grades).
- ✅ **Attendance** (teacher ticks present/absent per day, student sees % per course with a warning below 75%).
- ⬜ Timetable, Profile, Reports, Settings.

### Phase 4: Data persistence 🟡 *(in progress)*
- ✅ Text-file storage via `DataStore` for users, courses, lessons, enrollments, completions, assignments, submissions and attendance.
- ✅ `attendance.txt` for attendance.
- ⬜ Add a file for each new model as features arrive (timetable…).
- If the course allows it later, a database (JDBC) could replace the files.

### Phase 5: Real authentication 🟡 *(in progress)*
- ✅ Users are loaded from storage; the admin can add and delete them.
- ⬜ Change password (Profile), hash passwords.

### Phase 6: Polish
- ✅ Input validation, error dialogs and delete confirmations for the course, assignment and attendance features.
- ⬜ Confirm on logout, minimum window size, search/sort in tables.

---

## Planned Features by Role

Candidate features for the remaining menu items. We haven't committed to any of them yet.

### Student
| Menu item | Planned feature |
|---|---|
| My Courses | ✅ Done |
| Assignments | ✅ Done (submit a link, resubmit until graded) |
| Grades | ✅ Done (marks per assignment + course totals) |
| Attendance | ✅ Done (% per course, warning below 75%) |
| Timetable | Weekly class schedule. |
| Profile | View own details; change password. |

### Teacher
| Menu item | Planned feature |
|---|---|
| My Courses | ✅ Done |
| Assignments | ✅ Done (create, edit, delete) |
| Grade Submissions | ✅ Done (open link, marks + feedback) |
| Attendance | ✅ Done (tick present per day, edit past days) |
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
