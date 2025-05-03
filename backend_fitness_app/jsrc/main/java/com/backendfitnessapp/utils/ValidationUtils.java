package com.backendfitnessapp.utils;

import com.backendfitnessapp.exceptions.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final String EMAIL_REGEX = "^([A-Za-z0-9]+(\\.?|_?)){1,3}([A-Za-z0-9])@([A-Za-z]+\\.?){1,2}([A-Za-z]{2,})$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);


    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be null or empty");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&).");
        }
    }

}
