package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import com.backendfitnessapp.enums.WorkoutState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedWorkoutResponseBody {
    private String workoutId;
    private String clientId;
    private String coachId;
    private ActivityType activity;
    private String dateTime;
    private String name;
    private String duration;
    private String description;
    private WorkoutState state;
    private List<String> usersId;
}