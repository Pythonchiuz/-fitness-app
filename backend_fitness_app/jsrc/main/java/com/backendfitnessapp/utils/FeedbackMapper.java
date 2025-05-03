package com.backendfitnessapp.utils;

import com.backendfitnessapp.dto.CoachFeedbackRespondBody;
import com.backendfitnessapp.dto.GetUserFeedbacksResponseBody;
import com.backendfitnessapp.dto.PostFeedbackRequestBody;
import com.backendfitnessapp.entity.Feedback;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.enums.FeedbackType;
import com.backendfitnessapp.enums.Role;

import java.time.LocalDate;
import java.util.UUID;

public class FeedbackMapper {
    public FeedbackMapper() {
    }

    public static CoachFeedbackRespondBody feedbackToCoachFeedback(Feedback feedback, UserProfile userProfile) {
        CoachFeedbackRespondBody respondBody = new CoachFeedbackRespondBody();
        respondBody.setClientImageUrl(userProfile.getImageUrl());
        respondBody.setClientName(userProfile.getFirstName());
        respondBody.setDate(DateMapper.toLocalDate(feedback.getCreatedDate()));
        respondBody.setId(feedback.getUserId());
        respondBody.setMessage(feedback.getComment());
        respondBody.setRating(feedback.getRating());
        return respondBody;
    }

    public static Feedback PostFeedbackRequestToFeedback(PostFeedbackRequestBody postFeedbackRequestBody, UserProfile currentUser) {
        Feedback feedback = new Feedback();
        feedback.setFeedbackId(UUID.randomUUID().toString());
        feedback.setWorkoutId(postFeedbackRequestBody.getWorkoutId());
        feedback.setUserId(postFeedbackRequestBody.getClientId());
        feedback.setCoachId(postFeedbackRequestBody.getCoachId());
        feedback.setComment(postFeedbackRequestBody.getComment());
        feedback.setCreatedDate(LocalDate.now().toString());

        feedback.setRating(currentUser.getRole().equals(Role.USER) ? postFeedbackRequestBody.getRating() : null);

        feedback.setFeedbackType(determineFeedbackType(currentUser));
        return feedback;
    }

    private static FeedbackType determineFeedbackType(UserProfile currentUser) {
        return currentUser.getRole().equals(Role.COACH) ? FeedbackType.COACH_FEEDBACK : FeedbackType.USER_FEEDBACK;
    }

    public static GetUserFeedbacksResponseBody FeedbackToGetUserFeedbackResponse(Feedback feedback, UserProfile currentUser) {
        GetUserFeedbacksResponseBody userFeedbacksResponseBody = new GetUserFeedbacksResponseBody();
        userFeedbacksResponseBody.setClientName(currentUser.getFirstName());
        userFeedbacksResponseBody.setClientImageUrl(currentUser.getImageUrl());
        userFeedbacksResponseBody.setComment(feedback.getComment());
        userFeedbacksResponseBody.setDate(feedback.getCreatedDate());

        if (feedback.getFeedbackType() == FeedbackType.USER_FEEDBACK) {
            userFeedbacksResponseBody.setRating(feedback.getRating());
            userFeedbacksResponseBody.setUserId(feedback.getUserId());
        } else {
            userFeedbacksResponseBody.setUserId(feedback.getCoachId());
        }

        return userFeedbacksResponseBody;
    }
}
