package com.backendfitnessapp.service;

import com.backendfitnessapp.entity.Feedback;
import java.util.List;

public interface FeedbackService {
    List<Feedback> getFeedbacksByCoachId(String coachId);
    void saveFeedback(Feedback feedback);
    List<Feedback> getAllFeedbacks();
}
