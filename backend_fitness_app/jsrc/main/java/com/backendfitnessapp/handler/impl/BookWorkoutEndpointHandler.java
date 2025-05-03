package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.dto.BookWorkoutRequestBody;
import com.backendfitnessapp.entity.BookedWorkout;
import com.backendfitnessapp.entity.TimeSlot;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.WorkoutState;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.exceptions.WorkoutNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.Optional;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class BookWorkoutEndpointHandler implements EndpointHandler {
    private final UserService userService;
    private final WorkoutService workoutService;
    private final Gson gson;

    public BookWorkoutEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.gson = gson;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            BookWorkoutRequestBody requestBody = this.gson.fromJson(requestEvent.getBody(), BookWorkoutRequestBody.class);

            UserProfile client = Optional.ofNullable(this.userService.getUserById(requestBody.getClientId()))
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Workout workout = this.findMatchingWorkout(requestBody)
                    .orElseThrow(() -> new WorkoutNotFoundException("No workout found for the given coachId and date"));

            TimeSlot timeSlot = this.findAvailableTimeSlot(workout, requestBody.getTimeSlot())
                    .orElseThrow(() -> new NullPointerException("Time slot not available"));

            this.bookWorkout(client, workout, timeSlot);

            return createResponse(200, this.gson.toJson(Map.of("message", "Workout booked successfully")));
        } catch (NullPointerException | JsonSyntaxException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", ((RuntimeException)e).getMessage())));
        } catch (UserNotFoundException | WorkoutNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", ((RuntimeException)e).getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    private Optional<Workout> findMatchingWorkout(BookWorkoutRequestBody request) {
        return this.workoutService.getWorkoutsByCoachId(request.getCoachId())
                .stream()
                .filter((workout) -> workout.getDateTime().startsWith(request.getDate()))
                .findFirst();
    }

    private Optional<TimeSlot> findAvailableTimeSlot(Workout workout, String requestedTime) {
        return workout.getTimeSlots()
                .stream()
                .filter((slot) -> slot.getTime().startsWith(requestedTime) && slot.getAvailableSpace() > 0)
                .findFirst();
    }

    private void bookWorkout(UserProfile client, Workout workout, TimeSlot timeSlot) {
        BookedWorkout bookedWorkout = new BookedWorkout();
        bookedWorkout.setBookedWorkoutId(workout.getWorkoutId());
        bookedWorkout.setTimeSlot(timeSlot.getTime());
        bookedWorkout.setWorkoutState(WorkoutState.SCHEDULED);

        client.setBookedWorkout(bookedWorkout);
        workout.setUserId(client.getUserId());

        timeSlot.setAvailableSpace(timeSlot.getAvailableSpace() - 1);

        this.workoutService.updateWorkout(workout);
        this.userService.updateUserProfile(client);
    }
}
