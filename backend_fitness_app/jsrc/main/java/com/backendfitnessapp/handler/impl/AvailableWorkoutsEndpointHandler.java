package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.WorkoutDetailsResponseBody;
import com.backendfitnessapp.entity.TimeSlot;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.enums.WorkoutState;
import com.backendfitnessapp.exceptions.WorkoutNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.backendfitnessapp.service.WorkoutStateUpdater;
import com.backendfitnessapp.utils.WorkoutMapper;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class AvailableWorkoutsEndpointHandler implements EndpointHandler {
    private final WorkoutService workoutService;
    private final UserService userService;
    private final Gson gson;
    private final WorkoutStateUpdater workoutStateUpdater;

    public AvailableWorkoutsEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson, WorkoutStateUpdater workoutStateUpdater) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.gson = gson;
        this.workoutStateUpdater = workoutStateUpdater;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            Map<String, String> queryParams = requestEvent.getQueryStringParameters();

            String activityType = queryParams != null ? queryParams.get("activity") : null;
            String date = queryParams != null ? queryParams.get("date") : null;
            String time = queryParams != null ? queryParams.get("time") : null;
            String coachId = queryParams != null ? queryParams.get("coachId") : null;

            LocalDateTime currentTime = LocalDateTime.now();

            workoutStateUpdater.updateWorkoutStates();

            List<WorkoutDetailsResponseBody> content = this.workoutService.getAllWorkouts()
                    .stream()
                    .filter((workout) -> {
                try {
                    LocalDateTime workoutDateTime = LocalDateTime.parse(workout.getDateTime());
                    return currentTime.isBefore(workoutDateTime) && workout.getState().equals(WorkoutState.SCHEDULED);
                } catch (DateTimeParseException e) {
                    return false;
                }
                    }).filter((workout) -> this.filterByQueryParams(workout, activityType, date, time, coachId))
                    .map((workout) -> {
                        workout.setTimeSlots(this.filterAvailableTimeSlots(workout));
                        return this.mapToWorkoutResponse(workout);
                    }).toList();

            if (content.isEmpty()) {
                throw new WorkoutNotFoundException("no workout found");
            }

            return createResponse(200, this.gson.toJson(content.stream().filter(Objects::nonNull).toList()));
        } catch (WorkoutNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    private boolean filterByQueryParams(Workout workout, String activityType, String date, String time, String coachId) {
        return (activityType == null || workout.getActivity().name().equalsIgnoreCase(activityType))
                && (date == null || workout.getDateTime().startsWith(date))
                && (time == null || workout.getDateTime().contains("T" + time))
                && (coachId == null || workout.getCoachId().equals(coachId));
    }

    private List<TimeSlot> filterAvailableTimeSlots(Workout workout) {
        return workout.getTimeSlots().stream()
                .filter((timeSlot) -> timeSlot.getAvailableSpace() > 0)
                .toList();
    }

    private WorkoutDetailsResponseBody mapToWorkoutResponse(Workout workout) {
        UserProfile coach = this.userService.getUserById(workout.getCoachId());
        return coach != null && coach.getRole() == Role.COACH ? WorkoutMapper.workoutToWorkoutDetailsRespond(workout, coach) : null;
    }
}