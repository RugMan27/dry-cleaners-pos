package xyz.rugman27.drycleanerspos.utilites;


import java.time.*;
import java.time.format.DateTimeFormatter;

public class Utils {


    public static boolean notBlankString(String string) {
        return string != null && !string.isBlank();
    }


    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public static Long toEpochMillis(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }



    public static Long toEpochMillisStartOfDay(LocalDate date) {
        if (date == null) return null;
        LocalDateTime startOfDay = date.atStartOfDay();
        // This is the correct line for UTC epoch:
        Long epochMillis = startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
        System.out.println("DEBUG: toEpochMillisStartOfDay(" + date + ") -> " + startOfDay + " (UTC) -> " + epochMillis);
        return epochMillis;
    }

    public static Long toEpochMillisEndOfDay(LocalDate date) {
        if (date == null) return null;
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        // This is the correct line for UTC epoch:
        Long epochMillis = endOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
        System.out.println("DEBUG: toEpochMillisEndOfDay(" + date + ") -> " + endOfDay + " (UTC) -> " + epochMillis);
        return epochMillis;
    }



}
