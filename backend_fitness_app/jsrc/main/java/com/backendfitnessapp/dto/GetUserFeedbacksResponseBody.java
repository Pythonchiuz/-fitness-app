package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserFeedbacksResponseBody {
    private String userId;
    private String clientName;
    private String clientImageUrl;
    private String comment;
    private String date;
    private Integer rating;
}
