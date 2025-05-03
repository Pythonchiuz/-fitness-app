package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.CoachesDetailedRespondBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.backendfitnessapp.utils.UserMapper;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class CoachesByIdEndpointHandler implements EndpointHandler {
    private final UserService userService;
    private final Gson gson;
    private final WorkoutService workoutService;

    public CoachesByIdEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
        this.workoutService = workoutService;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            Map<String, String> pathParameters = requestEvent.getPathParameters();
            UserProfile coach = this.userService.getUserById(pathParameters.get("coachId"));

            if (coach == null || !coach.getRole().equals(Role.COACH)) {
                throw new UserNotFoundException("coach not found");
            }

            List<String> coachWorkoutDates = this.workoutService.getWorkoutsByCoachId(coach.getUserId())
                    .stream().map(Workout::getDateTime)
                    .filter(Objects::nonNull)
                    .toList();

            CoachesDetailedRespondBody respondBody = UserMapper.userProfileToCoachesDetailedRespond(coach, coachWorkoutDates);

            return createResponse(200, this.gson.toJson(respondBody));
        } catch (UserNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }
}
