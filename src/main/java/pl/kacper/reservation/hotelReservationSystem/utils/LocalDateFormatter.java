package pl.kacper.reservation.hotelReservationSystem.utils;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

public class LocalDateFormatter {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static DateTimeFormatter getDateFormatter() {

        return DateTimeFormatter.ofPattern(DATE_FORMAT);
    }
}
