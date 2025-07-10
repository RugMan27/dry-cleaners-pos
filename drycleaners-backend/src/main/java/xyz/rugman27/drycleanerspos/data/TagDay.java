package xyz.rugman27.drycleanerspos.data;

public enum TagDay {
    MONDAY("Gray"),
    TUESDAY("Orange"),
    WEDNESDAY("White"),
    THURSDAY("Brown"),
    FRIDAY("Yellow"),
    RUSH("Red");

    private final String color;

    TagDay(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
