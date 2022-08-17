package org.argunaoverdrive.bot.model;

public enum WeekDays {
    SUN("Sunday"),
    MON("Monday"),
    TUE("Tueday"),
    WED("Wednesday"),
    THU("Thursday"),
    FRI("Friday"),
    SAT("Saturday");
    private final String name;

    WeekDays(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
