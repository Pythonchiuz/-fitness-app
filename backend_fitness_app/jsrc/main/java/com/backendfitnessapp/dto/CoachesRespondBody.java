package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoachesRespondBody {
    private String coachId;
    private String name;
    private String summary;
    private String motivationPitch;
    private String imageUrl;
    private Double rating;
}
