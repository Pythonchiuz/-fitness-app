package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import com.backendfitnessapp.enums.WorkoutState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookWorkoutResponse {
    private ActivityType activity;
    private String clientId;
    private String coachId;
    private String dateTime;
    private String description;
    private String feedbackId;
    private String id;
    private String name;
    private WorkoutState state;
}
