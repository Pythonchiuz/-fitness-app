package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoachReport {
    String gymLocation;
    double averageFeedback;
    int minimumFeedback;
    String deltaMinimumFeedback;
    private String coach;
    private String email;
    private int workoutsLead;
    private String deltaWorkoutsLead;
}
