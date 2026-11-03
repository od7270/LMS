package model;

public class Lesson {
    private final int id;
    private final String courseCode;
    private String title;
    private String youtubeUrl;

    public Lesson(int id, String courseCode, String title, String youtubeUrl) {
        this.id = id;
        this.courseCode = courseCode;
        this.title = title;
        this.youtubeUrl = youtubeUrl;
    }

    public int getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
}
