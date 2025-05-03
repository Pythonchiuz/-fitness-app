package com.backendfitnessapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackPageResponseBody {
    private List<CoachFeedbackRespondBody> content;
    private int currentPage;
    private int totalElements;
    private int totalPages;
}
