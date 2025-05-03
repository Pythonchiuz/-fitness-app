package com.backendfitnessapp.dto;

import com.backendfitnessapp.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoachesDetailedRespondBody {
    private String coachId;
    private String name;
    private String summary;
    private String motivationPitch;
    private String imageUrl;
    private Double rating;
    private List<String> specializations;
    private List<String> fileUrls;
    private String about;
    private List<String> workoutDates;
    private ActivityType type;
    private String duration;
}
