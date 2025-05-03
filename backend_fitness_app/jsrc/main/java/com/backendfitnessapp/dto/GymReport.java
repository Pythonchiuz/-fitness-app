package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GymReport {
    String gymLocation;
    double averageFeedback;
    int minimumFeedback;
    String deltaMinimumFeedback;
    private ActivityType workoutType;
    private int workoutsLead;
    private String attendanceRate;
    private String attendanceDelta;
}