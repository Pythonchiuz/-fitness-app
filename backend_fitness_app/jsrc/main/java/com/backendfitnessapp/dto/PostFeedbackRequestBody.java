package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostFeedbackRequestBody {
    private String clientId;
    private String coachId;
    private String comment;
    private Integer rating;
    private String workoutId;
}
