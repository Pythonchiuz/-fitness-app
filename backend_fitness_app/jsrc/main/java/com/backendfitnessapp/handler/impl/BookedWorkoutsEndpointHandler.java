package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.BookedWorkoutResponseBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.backendfitnessapp.service.WorkoutStateUpdater;
import com.backendfitnessapp.utils.WorkoutMapper;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class BookedWorkoutsEndpointHandler implements EndpointHandler {
    private final UserService userService;
    private final WorkoutService workoutService;
    private final Gson gson;
    private final WorkoutStateUpdater workoutStateUpdater;

    public BookedWorkoutsEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson, WorkoutStateUpdater workoutStateUpdater) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.gson = gson;
        this.workoutStateUpdater = workoutStateUpdater;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            Map<String, String> queryParams = requestEvent.getQueryStringParameters();

            if (queryParams == null || !queryParams.containsKey("userId")) {
                throw new IllegalArgumentException("Missing required parameter: userId");
            }

            workoutStateUpdater.updateWorkoutStates();

            UserProfile currentUser = Optional.ofNullable(this.userService.getUserById(queryParams.get("userId")))
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            List<BookedWorkoutResponseBody> content = null;

            if (currentUser.getRole().equals(Role.COACH)) {
                content = this.workoutService.getWorkoutsByCoachId(currentUser.getUserId())
                        .stream().map((workout) ->
                                WorkoutMapper.workoutToBookedWorkoutResponse(workout, currentUser))
                        .toList();
            } else {
                content = this.workoutService.getWorkoutsByUserId(queryParams.get("userId"))
                        .stream().map((workout) ->
                                WorkoutMapper.workoutToBookedWorkoutResponse(workout, currentUser))
                        .toList();
            }
            return createResponse(200, this.gson.toJson(content));
        } catch (IllegalArgumentException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (UserNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }
}
