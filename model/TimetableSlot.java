package model;

import java.time.DayOfWeek;

// One weekly class: a course meets on a day for one or more consecutive hourly periods in a room
public class TimetableSlot {
    private final int id;
    private final String courseCode;
    private DayOfWeek day;
    private int startPeriod; // 1 = first period of the day
    private int length;      // number of periods (hours)
    private String room;

    public TimetableSlot(int id, String courseCode, DayOfWeek day, int startPeriod, int length, String room) {
        this.id = id;
        this.courseCode = courseCode;
        this.day = day;
        this.startPeriod = startPeriod;
        this.length = length;
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public int getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(int startPeriod) {
        this.startPeriod = startPeriod;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int endPeriod() {
        return startPeriod + length - 1;
    }

    // True if this slot covers the given period on the given day
    public boolean covers(DayOfWeek day, int period) {
        return this.day == day && period >= startPeriod && period <= endPeriod();
    }

    // True if this slot shares at least one period with the other time
    public boolean overlaps(DayOfWeek day, int start, int length) {
        return this.day == day && start <= endPeriod() && start + length - 1 >= startPeriod;
    }
}
