package service;

import model.AttendanceRecord;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AttendanceService {
    // Students below this percentage get a warning
    public static final int THRESHOLD = 75;

    private final DataStore store;

    public AttendanceService(DataStore store) {
        this.store = store;
    }

    // Dates attendance was taken for the course, newest first
    public List<String> getDays(String courseCode) {
        TreeSet<String> days = new TreeSet<>(Comparator.reverseOrder());
        for (AttendanceRecord r : store.getAttendance()) {
            if (r.getCourseCode().equals(courseCode)) {
                days.add(r.getDate());
            }
        }
        return new ArrayList<>(days);
    }

    public AttendanceRecord find(String courseCode, String studentUsername, String date) {
        for (AttendanceRecord r : store.getAttendance()) {
            if (r.getCourseCode().equals(courseCode) && r.getStudentUsername().equals(studentUsername)
                    && r.getDate().equals(date)) {
                return r;
            }
        }
        return null;
    }

    // Checks a date typed by the teacher and returns it as YYYY-MM-DD
    public static String parseDay(String text) {
        LocalDate date;
        try {
            date = LocalDate.parse(DataStore.clean(text));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Enter the date as YYYY-MM-DD, e.g. " + LocalDate.now() + ".");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Attendance can't be taken for a future date.");
        }
        return date.toString();
    }

    // Saves present/absent for each student (username -> present) on one day
    public void saveDay(String courseCode, String dateText, Map<String, Boolean> presence) {
        String date = parseDay(dateText);
        for (Map.Entry<String, Boolean> entry : presence.entrySet()) {
            AttendanceRecord record = find(courseCode, entry.getKey(), date);
            if (record == null) {
                store.getAttendance().add(new AttendanceRecord(courseCode, entry.getKey(), date, entry.getValue()));
            } else {
                record.setPresent(entry.getValue());
            }
        }
        store.save();
    }

    public void deleteDay(String courseCode, String date) {
        store.getAttendance().removeIf(r -> r.getCourseCode().equals(courseCode) && r.getDate().equals(date));
        store.save();
    }

    // The student's records for the course, newest first
    public List<AttendanceRecord> getRecords(String courseCode, String studentUsername) {
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord r : store.getAttendance()) {
            if (r.getCourseCode().equals(courseCode) && r.getStudentUsername().equals(studentUsername)) {
                result.add(r);
            }
        }
        result.sort(Comparator.comparing(AttendanceRecord::getDate).reversed());
        return result;
    }

    // Only days with a record for the student count, so joining late doesn't count as absent
    public int totalCount(String courseCode, String studentUsername) {
        return getRecords(courseCode, studentUsername).size();
    }

    public int presentCount(String courseCode, String studentUsername) {
        int count = 0;
        for (AttendanceRecord r : getRecords(courseCode, studentUsername)) {
            if (r.isPresent()) {
                count++;
            }
        }
        return count;
    }

    public int percent(String courseCode, String studentUsername) {
        int total = totalCount(courseCode, studentUsername);
        return total == 0 ? 0 : presentCount(courseCode, studentUsername) * 100 / total;
    }

    public boolean isLow(String courseCode, String studentUsername) {
        return totalCount(courseCode, studentUsername) > 0 && percent(courseCode, studentUsername) < THRESHOLD;
    }
}
