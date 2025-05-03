package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDetailsResponseBody {
    private String coachId;
    private String coachAvatar;
    private String coachName;
    private String coachDescription;
    private Double rating;
    private ActivityType activity;
    private String date;
    private String duration;
    private String time;
    private String description;
    private List<String> alternateTimes;
}

