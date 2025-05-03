package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.WorkoutTimeSlotsBody;
import com.backendfitnessapp.entity.TimeSlot;
import com.backendfitnessapp.enums.WorkoutState;
import com.backendfitnessapp.exceptions.WorkoutNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.WorkoutService;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class CoachesAvailableEndpointHandler implements EndpointHandler {
    private final WorkoutService workoutService;
    private final Gson gson;

    public CoachesAvailableEndpointHandler(WorkoutService workoutService, Gson gson) {
        this.workoutService = workoutService;
        this.gson = gson;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            Map<String, String> pathParameters = requestEvent.getPathParameters();
            String coachId = pathParameters.get("coachId");
            String paramDate = pathParameters.get("date");
            List<WorkoutTimeSlotsBody> availableTimeSlots = this.workoutService.getWorkoutsByCoachId(coachId)
                    .stream()
                    .filter((workout) -> workout.getDateTime().startsWith(paramDate)
                            && workout.getState().equals(WorkoutState.SCHEDULED))
                    .map((workout) -> {
                        WorkoutTimeSlotsBody workoutTimeSlotsBody = new WorkoutTimeSlotsBody();
                        workoutTimeSlotsBody.setTimeSlots(workout.getTimeSlots().stream().map(TimeSlot::getTime).toList());
                        workoutTimeSlotsBody.setDuration(workout.getDuration());
                        workoutTimeSlotsBody.setType(workout.getActivity());
                        return workoutTimeSlotsBody;
                            })
                    .toList();

            return createResponse(200, this.gson.toJson(availableTimeSlots));
        } catch (IllegalArgumentException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (WorkoutNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }
}
