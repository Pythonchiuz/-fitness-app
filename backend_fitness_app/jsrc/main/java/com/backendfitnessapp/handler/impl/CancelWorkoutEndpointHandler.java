package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.authorisation.TokenDecoder;
import com.backendfitnessapp.entity.BookedWorkout;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.enums.WorkoutState;
import com.backendfitnessapp.exceptions.UserNotFoundException;
import com.backendfitnessapp.exceptions.WorkoutNotFoundException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Optional;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class CancelWorkoutEndpointHandler implements EndpointHandler {
    private final WorkoutService workoutService;
    private final Gson gson;
    private final UserService userService;
    private final TokenDecoder tokenDecoder;

    public CancelWorkoutEndpointHandler(WorkoutService workoutService, UserService userService, Gson gson, TokenDecoder tokenDecoder) {
        this.workoutService = workoutService;
        this.userService = userService;
        this.gson = gson;
        this.tokenDecoder = tokenDecoder;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        String responseMessage = null;

        try {

            Map<String, String> pathParams = requestEvent.getPathParameters();
            if (pathParams != null && pathParams.containsKey("workoutId")) {

                UserProfile currentUser = this.tokenDecoder.getUserFromIdToken(requestEvent.getHeaders())
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

                Workout workout = Optional.ofNullable(this.workoutService.getWorkoutById(pathParams.get("workoutId")))
                        .orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));

                if (currentUser.getRole().equals(Role.COACH)) {
                    this.cancelWorkoutForAllUsers(workout);
                    responseMessage = "Workout cancelled for all users by coach";
                } else {
                    this.cancelWorkoutForUser(workout, currentUser, context);
                    responseMessage = "Workout cancelled successfully";
                }

                return createResponse(200, this.gson.toJson(Map.of("message", responseMessage)));
            } else {
                throw new IllegalArgumentException("Missing required parameter workoutId");
            }
        } catch (IllegalArgumentException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (WorkoutNotFoundException | UserNotFoundException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", ((RuntimeException)e).getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    private void cancelWorkoutForAllUsers(Workout workout) {
        workout.setState(WorkoutState.CANCELLED);

        for(String userBookedId : workout.getUsersId()) {
            UserProfile user = this.userService.getUserById(userBookedId);

            for(BookedWorkout bookedWorkout : user.getBookedWorkouts()) {
                if (bookedWorkout.getBookedWorkoutId().equals(workout.getWorkoutId())) {
                    bookedWorkout.setWorkoutState(WorkoutState.CANCELLED);
                }
            }

            this.userService.updateUserProfile(user);
        }

        this.workoutService.updateWorkout(workout);
    }

    private void cancelWorkoutForUser(Workout workout, UserProfile currentUser, Context context) {
        if (!workout.getUsersId().contains(currentUser.getUserId())) {
            throw new IllegalArgumentException("User has not booked this workout");
        } else {
            currentUser.getBookedWorkouts()
                    .stream()
                    .filter((bookedWorkout) ->
                            bookedWorkout.getBookedWorkoutId().equals(workout.getWorkoutId()))
                    .forEach((bookedWorkout) -> bookedWorkout.setWorkoutState(WorkoutState.CANCELLED)
                    );

            context.getLogger().log(this.gson.toJson(currentUser));

            this.userService.updateUserProfile(currentUser);
        }
    }
}
