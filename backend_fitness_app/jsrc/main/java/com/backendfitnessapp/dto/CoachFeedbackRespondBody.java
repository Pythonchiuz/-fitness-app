package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoachFeedbackRespondBody {
    private String id;
    private String clientName;
    private String message;
    private String clientImageUrl;
    private Integer rating;
    private LocalDate date;
}
