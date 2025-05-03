package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookWorkoutRequestBody {
    private String clientId;
    private String coachId;
    private String date;
    private String timeSlot;
}