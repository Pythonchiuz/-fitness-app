package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.CoachesRespondBody;
import com.backendfitnessapp.exceptions.WorkoutNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.utils.UserMapper;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class CoachesEndpointHandler implements EndpointHandler {
    private final UserService userService;
    private final Gson gson;

    public CoachesEndpointHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            List<CoachesRespondBody> content = this.userService.getAllCoachUsers()
                    .stream()
                    .map(UserMapper::userProfileToCoachesRespond)
                    .toList();
            if (content.isEmpty()) {
                throw new WorkoutNotFoundException("no content");
            }
            return createResponse(200, this.gson.toJson(content));
        } catch (WorkoutNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }
}
