package com.backendfitnessapp.utils;

import java.time.LocalDate;

public class DateMapper {

    public static LocalDate toLocalDate(String date) {
        return LocalDate.parse(date);
    }
}
