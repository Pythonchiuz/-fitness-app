package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.authorisation.TokenDecoder;
import com.backendfitnessapp.dto.PostFeedbackRequestBody;
import com.backendfitnessapp.entity.BookedWorkout;
import com.backendfitnessapp.entity.Feedback;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.FeedbackType;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.enums.WorkoutState;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.SqsAsyncQueueSender;
import com.backendfitnessapp.service.FeedbackService;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.backendfitnessapp.utils.FeedbackMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.List;
import java.util.Map;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class PostFeedbackEndpointHandler implements EndpointHandler {
    private final FeedbackService feedbackService;
    private final UserService userService;
    private final WorkoutService workoutService;
    private final TokenDecoder tokenDecoder;
    private final Gson gson;
    private final SqsAsyncQueueSender sqsAsyncQueueSender;

    public PostFeedbackEndpointHandler(UserService userService, FeedbackService feedbackService, WorkoutService workoutService, Gson gson, TokenDecoder tokenDecoder, SqsAsyncQueueSender sqsAsyncQueueSender) {
        this.userService = userService;
        this.feedbackService = feedbackService;
        this.workoutService = workoutService;
        this.gson = gson;
        this.tokenDecoder = tokenDecoder;
        this.sqsAsyncQueueSender = sqsAsyncQueueSender;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            UserProfile currentUser = this.tokenDecoder.getUserFromIdToken(requestEvent.getHeaders())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            PostFeedbackRequestBody feedbackRequest = this.gson.fromJson(requestEvent.getBody(), PostFeedbackRequestBody.class);

            Workout workout = this.workoutService.getWorkoutById(feedbackRequest.getWorkoutId());

            Feedback newFeedback = FeedbackMapper.PostFeedbackRequestToFeedback(feedbackRequest, currentUser);
            if (currentUser.getRole() == Role.COACH) {
                if (workout.getState() != WorkoutState.WAITING_FOR_FEEDBACK) {
                    throw new IllegalStateException("Feedback can only be posted if workout is in WAITING_FOR_FEEDBACK state.");
                }
                workout.setState(WorkoutState.FINISHED);
                this.workoutService.updateWorkout(workout);
                this.feedbackService.saveFeedback(newFeedback);
            } else {
                for(BookedWorkout bookedWorkout : currentUser.getBookedWorkouts()) {
                    if (workout.getWorkoutId().equals(bookedWorkout.getBookedWorkoutId())) {
                        if (bookedWorkout.getWorkoutState() != WorkoutState.WAITING_FOR_FEEDBACK) {
                            throw new IllegalStateException("Feedback can only be posted if workout is in WAITING_FOR_FEEDBACK state.");
                        }
                        bookedWorkout.setWorkoutState(WorkoutState.FINISHED);
                    }
                }

                this.userService.updateUserProfile(currentUser);
                this.feedbackService.saveFeedback(newFeedback);

                this.updateCoachRating(feedbackRequest.getCoachId());

                sqsAsyncQueueSender.report(newFeedback, workout);
            }

            return createResponse(201, this.gson.toJson(Map.of("message", "feedback posted successfully")));

        } catch (JsonParseException | IllegalStateException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (UserNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void updateCoachRating(String coachId) {
        UserProfile coach = this.userService.getUserById(coachId);
        List<Feedback> feedbacks = this.feedbackService.getFeedbacksByCoachId(coach.getUserId())
                .stream()
                .filter((feedback) -> feedback.getFeedbackType().equals(FeedbackType.USER_FEEDBACK))
                .toList();

        double averageRating = feedbacks
                .stream()
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0F);

        String formattedRating = String.format("%.1f", averageRating);
        double finalRating = Double.parseDouble(formattedRating);
        coach.setRating(finalRating);

        this.userService.updateUserProfile(coach);
    }
}
