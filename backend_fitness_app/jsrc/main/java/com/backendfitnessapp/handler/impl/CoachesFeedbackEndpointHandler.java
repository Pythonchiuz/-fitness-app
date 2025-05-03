package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.CoachFeedbackRespondBody;
import com.backendfitnessapp.dto.FeedbackPageResponseBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.enums.FeedbackType;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.FeedbackService;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.utils.FeedbackMapper;
import com.google.gson.Gson;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class CoachesFeedbackEndpointHandler implements EndpointHandler {
    private final UserService userService;
    private final Gson gson;
    private final FeedbackService feedbackService;

    public CoachesFeedbackEndpointHandler(UserService userService, FeedbackService feedbackService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
        this.feedbackService = feedbackService;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            Map<String, String> parameters = requestEvent.getPathParameters();

            Map<String, String> queryParm = requestEvent.getQueryStringParameters() != null
                    ? requestEvent.getQueryStringParameters()
                    : Map.of();

            String coachId = parameters.get("coachId");
            int page = this.parseQueryParam(queryParm, "page", 1);
            int size = this.parseQueryParam(queryParm, "size", 3);
            String sort = queryParm.getOrDefault("sort", "date,asc");

            UserProfile coach = this.userService.getUserById(coachId);

            if (coach != null && coach.getRole().equals(Role.COACH)) {
                List<CoachFeedbackRespondBody> coachFeedbacks = this.fetchAndSortFeedbacks(coachId, sort);
                FeedbackPageResponseBody feedbackPageResponse = this.paginateFeedbacks(coachFeedbacks, page, size);

                return createResponse(200, this.gson.toJson(feedbackPageResponse));
            } else {
                throw new UserNotFoundException("Coach not found");
            }
        } catch (IllegalArgumentException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (UserNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    private List<CoachFeedbackRespondBody> fetchAndSortFeedbacks(String coachId, String sort) {
        return this.feedbackService.getFeedbacksByCoachId(coachId)
                .stream()
                .filter((feedback) -> feedback.getFeedbackType() == FeedbackType.USER_FEEDBACK)
                .map((feedback) ->
                        FeedbackMapper.feedbackToCoachFeedback(feedback, this.userService.getUserById(feedback.getUserId()))
                )
                .sorted(this.getComparator(sort)).toList();
    }

    private FeedbackPageResponseBody paginateFeedbacks(List<CoachFeedbackRespondBody> feedbacks, int page, int size) {
        int totalFeedbacks = feedbacks.size();
        int totalPages = (int)Math.ceil((double)totalFeedbacks / (double)size);
        int start = Math.max(0, (page - 1) * size);
        int end = Math.min(start + size, totalFeedbacks);

        return new FeedbackPageResponseBody(feedbacks.subList(start, end), page, totalFeedbacks, totalPages);
    }

    private int parseQueryParam(Map<String, String> queryParams, String key, int defaultValue) {
        try {
            return queryParams.containsKey(key) ? Integer.parseInt(queryParams.get(key)) : defaultValue;
        } catch (NumberFormatException var5) {
            return defaultValue;
        }
    }

    private Comparator<CoachFeedbackRespondBody> getComparator(String sort) {
        return switch (sort) {
            case "date,asc" -> Comparator.comparing(CoachFeedbackRespondBody::getDate);
            case "date,desc" -> Comparator.comparing(CoachFeedbackRespondBody::getDate).reversed();
            case "rating,asc" -> Comparator.comparingDouble(CoachFeedbackRespondBody::getRating);
            case "rating,desc" ->  Comparator.comparingDouble(CoachFeedbackRespondBody::getRating).reversed();
            default -> throw new IllegalStateException("Unexpected sort value: " + sort);
        };
    }
}
