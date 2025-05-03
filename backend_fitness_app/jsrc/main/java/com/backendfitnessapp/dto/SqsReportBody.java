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
public class SqsReportBody {
    private String coach;
    private String email;
    private String workoutId;
    private ActivityType workoutType;
    private String workoutDate;
    private int workoutCount;
    private int workoutCapacity;
    private int attendanceCount;
    private String location;
    private Integer feedbackRating;
}