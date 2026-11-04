package service;

import model.Course;
import model.TimetableSlot;
import model.User;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimetableService {
    public static final int PERIODS = 8;     // hourly periods per day
    public static final int FIRST_HOUR = 9;  // period 1 starts at 09:00
    public static final DayOfWeek[] DAYS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    };

    private static final Comparator<TimetableSlot> BY_TIME =
            Comparator.comparing(TimetableSlot::getDay).thenComparingInt(TimetableSlot::getStartPeriod);

    private final DataStore store;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public TimetableService(DataStore store, CourseService courseService, EnrollmentService enrollmentService) {
        this.store = store;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    // ---- Labels ----

    // "09:00" for the start of period 1, "17:00" for the end of period 8
    private static String clock(int hour) {
        return String.format("%02d:00", hour);
    }

    public static String periodLabel(int period) {
        return timeRange(period, 1);
    }

    public static String timeRange(int start, int length) {
        return clock(FIRST_HOUR + start - 1) + "–" + clock(FIRST_HOUR + start - 1 + length);
    }

    public static String dayLabel(DayOfWeek day) {
        String name = day.name();
        return name.charAt(0) + name.substring(1, 3).toLowerCase();
    }

    public static String describe(TimetableSlot slot) {
        return slot.getCourseCode() + " " + dayLabel(slot.getDay()) + " " + timeRange(slot.getStartPeriod(), slot.getLength());
    }

    // ---- Queries (sorted by day, then period) ----

    public List<TimetableSlot> getAll() {
        List<TimetableSlot> result = new ArrayList<>(store.getSlots());
        result.sort(BY_TIME);
        return result;
    }

    public List<TimetableSlot> getByCourse(String courseCode) {
        List<TimetableSlot> result = new ArrayList<>();
        for (TimetableSlot slot : store.getSlots()) {
            if (slot.getCourseCode().equals(courseCode)) {
                result.add(slot);
            }
        }
        result.sort(BY_TIME);
        return result;
    }

    // Slots of the student's approved, enrolled courses
    public List<TimetableSlot> getForStudent(String studentUsername) {
        return slotsOf(enrollmentService.getCoursesFor(studentUsername));
    }

    public List<TimetableSlot> getForTeacher(String teacherUsername) {
        return slotsOf(courseService.getByTeacher(teacherUsername));
    }

    public TimetableSlot find(int id) {
        for (TimetableSlot slot : store.getSlots()) {
            if (slot.getId() == id) {
                return slot;
            }
        }
        return null;
    }

    private List<TimetableSlot> slotsOf(List<Course> courses) {
        List<TimetableSlot> result = new ArrayList<>();
        for (Course course : courses) {
            result.addAll(getByCourse(course.getCode()));
        }
        result.sort(BY_TIME);
        return result;
    }

    // ---- Checks ----

    // Rules that block saving: valid time, a room, no teacher or room double-booking.
    // excludeId is the slot being edited (so it doesn't clash with itself), or -1.
    public void checkHard(String courseCode, DayOfWeek day, int start, int length, String room, int excludeId) {
        Course course = courseCode == null ? null : courseService.find(courseCode);
        if (course == null) {
            throw new IllegalArgumentException("Choose a course.");
        }
        if (day == null) {
            throw new IllegalArgumentException("Choose a day.");
        }
        if (start < 1 || start > PERIODS || length < 1) {
            throw new IllegalArgumentException("Choose a start time and a length.");
        }
        if (start + length - 1 > PERIODS) {
            throw new IllegalArgumentException("The class would end after " + clock(FIRST_HOUR + PERIODS)
                    + ". Choose an earlier start or a shorter length.");
        }
        room = DataStore.clean(room);
        if (room.isEmpty()) {
            throw new IllegalArgumentException("Room is required.");
        }

        for (TimetableSlot other : store.getSlots()) {
            if (other.getId() == excludeId || !other.overlaps(day, start, length)) {
                continue;
            }
            Course otherCourse = courseService.find(other.getCourseCode());
            if (otherCourse != null && otherCourse.getTeacherUsername().equals(course.getTeacherUsername())) {
                throw new IllegalArgumentException("The teacher of " + course.getCode()
                        + " already teaches " + describe(other) + ".");
            }
            if (other.getRoom().equalsIgnoreCase(room)) {
                throw new IllegalArgumentException("Room " + other.getRoom() + " is already used by " + describe(other) + ".");
            }
        }
    }

    // Students of the course who would have another class at the same time (a warning, not an error)
    public List<String> studentClashes(String courseCode, DayOfWeek day, int start, int length, int excludeId) {
        List<String> clashes = new ArrayList<>();
        for (User student : enrollmentService.getStudentsIn(courseCode)) {
            for (TimetableSlot other : getForStudent(student.getUsername())) {
                if (other.getId() != excludeId && !other.getCourseCode().equals(courseCode)
                        && other.overlaps(day, start, length)) {
                    clashes.add(student.getUsername() + ": " + describe(other));
                }
            }
        }
        return clashes;
    }

    // ---- Changes ----

    public void add(String courseCode, DayOfWeek day, int start, int length, String room) {
        checkHard(courseCode, day, start, length, room, -1);
        int nextId = 1;
        for (TimetableSlot slot : store.getSlots()) {
            nextId = Math.max(nextId, slot.getId() + 1);
        }
        store.getSlots().add(new TimetableSlot(nextId, courseCode, day, start, length, DataStore.clean(room)));
        store.save();
    }

    public void update(TimetableSlot slot, DayOfWeek day, int start, int length, String room) {
        checkHard(slot.getCourseCode(), day, start, length, room, slot.getId());
        slot.setDay(day);
        slot.setStartPeriod(start);
        slot.setLength(length);
        slot.setRoom(DataStore.clean(room));
        store.save();
    }

    public void delete(TimetableSlot slot) {
        store.getSlots().remove(slot);
        store.save();
    }
}
