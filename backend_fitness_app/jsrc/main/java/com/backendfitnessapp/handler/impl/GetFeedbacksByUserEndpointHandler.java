package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.GetUserFeedbacksResponseBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.enums.FeedbackType;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.FeedbackService;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.utils.FeedbackMapper;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class GetFeedbacksByUserEndpointHandler implements EndpointHandler {
    private final FeedbackService feedbackService;
    private final UserService userService;
    private final Gson gson;

    public GetFeedbacksByUserEndpointHandler(UserService userService, FeedbackService feedbackService, Gson gson) {
        this.userService = userService;
        this.feedbackService = feedbackService;
        this.gson = gson;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            Map<String, String> parameters = requestEvent.getPathParameters();

            UserProfile currentUser =  Optional.ofNullable(this.userService.getUserById(parameters.get("userId")))
                    .orElseThrow(() -> new UserNotFoundException("user not found"));

            FeedbackType feedbackType = currentUser.getRole() == Role.COACH
                    ? FeedbackType.USER_FEEDBACK
                    : FeedbackType.COACH_FEEDBACK;

            List<GetUserFeedbacksResponseBody> feedbacks = this.feedbackService.getAllFeedbacks()
                    .stream()
                    .filter((feedback) -> feedback.getFeedbackType().equals(feedbackType))
                    .map((feedback) -> FeedbackMapper.FeedbackToGetUserFeedbackResponse(feedback, currentUser))
                    .toList();

            return createResponse(200, this.gson.toJson(feedbacks));
        } catch (UserNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }
}
