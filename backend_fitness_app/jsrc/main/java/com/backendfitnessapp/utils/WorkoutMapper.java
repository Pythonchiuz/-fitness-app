package com.backendfitnessapp.utils;

import com.backendfitnessapp.dto.BookedWorkoutResponseBody;
import com.backendfitnessapp.dto.WorkoutDetailsResponseBody;
import com.backendfitnessapp.entity.BookedWorkout;
import com.backendfitnessapp.entity.TimeSlot;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.Role;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class WorkoutMapper {
    public static WorkoutDetailsResponseBody workoutToWorkoutDetailsRespond(Workout workout, UserProfile userProfile) {
        LocalDateTime localDateTime = LocalDateTime.parse(workout.getDateTime());
        WorkoutDetailsResponseBody workoutDetailsRespond = new WorkoutDetailsResponseBody();
        workoutDetailsRespond.setCoachId(workout.getCoachId());
        workoutDetailsRespond.setCoachAvatar(userProfile.getImageUrl());
        workoutDetailsRespond.setCoachName(userProfile.getFirstName() + " " + userProfile.getLastName());
        workoutDetailsRespond.setCoachDescription(userProfile.getUserSummary());
        workoutDetailsRespond.setRating(userProfile.getRating());
        workoutDetailsRespond.setActivity(workout.getActivity());
        workoutDetailsRespond.setDate(localDateTime.toLocalDate().toString());
        workoutDetailsRespond.setDuration(workout.getDuration());
        workoutDetailsRespond.setTime(localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        workoutDetailsRespond.setDescription(workout.getMotivationPitch());
        workoutDetailsRespond.setAlternateTimes(workout.getTimeSlots().stream().map(TimeSlot::getTime).toList());
        return workoutDetailsRespond;
    }

    public static BookedWorkoutResponseBody workoutToBookedWorkoutResponse(Workout workout, UserProfile userProfile) {
        BookedWorkoutResponseBody bookedWorkoutResponse = new BookedWorkoutResponseBody();
        bookedWorkoutResponse.setWorkoutId(workout.getWorkoutId());
        bookedWorkoutResponse.setClientId(userProfile.getUserId());
        bookedWorkoutResponse.setCoachId(workout.getCoachId());
        bookedWorkoutResponse.setActivity(workout.getActivity());
        bookedWorkoutResponse.setDateTime(workout.getDateTime());
        bookedWorkoutResponse.setName(workout.getWorkoutName());
        bookedWorkoutResponse.setDuration(workout.getDuration());
        bookedWorkoutResponse.setDescription(workout.getMotivationPitch());

        if (userProfile.getRole().equals(Role.COACH)) {
            bookedWorkoutResponse.setState(workout.getState());
            bookedWorkoutResponse.setUsersId(workout.getUsersId());
        } else {
            for(BookedWorkout bookedWorkout : userProfile.getBookedWorkouts()) {
                if (bookedWorkout.getBookedWorkoutId().equals(workout.getWorkoutId())) {
                    bookedWorkoutResponse.setState(bookedWorkout.getWorkoutState());
                }
            }
        }

        return bookedWorkoutResponse;
    }
}
