package com.backendfitnessapp.service;

import com.backendfitnessapp.entity.BookedWorkout;
import com.backendfitnessapp.entity.TimeSlot;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.WorkoutState;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutStateUpdater {
    private final WorkoutService workoutService;
    private final UserService userService;
    private final SqsAsyncQueueSender sqsAsyncQueueSender;
    public WorkoutStateUpdater(UserService userService, WorkoutService workoutService, SqsAsyncQueueSender sqsAsyncQueueSender) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.sqsAsyncQueueSender = sqsAsyncQueueSender;
    }

    public void updateWorkoutStates() {
        List<Workout> workouts = workoutService.getAllWorkouts()
                .stream()
                .filter(workout -> workout.getState() != WorkoutState.CANCELLED
                        && workout.getState() != WorkoutState.FINISHED)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        for (Workout workout : workouts) {
            LocalDateTime workoutStart = LocalDateTime.parse(workout.getDateTime());
            LocalDateTime latestTimeSlot = getLatestTimeSlot(workoutStart, workout.getTimeSlots());

            WorkoutState newState = determineState(workoutStart, latestTimeSlot, now);
            if (!newState.equals(workout.getState())) {
                if (newState.equals(WorkoutState.WAITING_FOR_FEEDBACK)) {
                    sqsAsyncQueueSender.report(null, workout);
                }
                workout.setState(newState);
                workoutService.updateWorkout(workout);
                if (workout.getUsersId() != null) {
                    for (String userId : workout.getUsersId()) {
                        UserProfile user = userService.getUserById(userId);
                        for (BookedWorkout bookedWorkout : user.getBookedWorkouts()) {
                            if (bookedWorkout.getBookedWorkoutId().equals(workout.getWorkoutId())) {
                                bookedWorkout.setWorkoutState(newState);
                            }
                        }
                        userService.updateUserProfile(user);
                    }
                }
            }
        }
    }

    private LocalDateTime getLatestTimeSlot(LocalDateTime workoutStart, List<TimeSlot> timeSlots) {
        LocalDateTime latestTime = workoutStart;
        for (TimeSlot slot : timeSlots) {
            LocalDateTime slotEndTime = parseTimeSlot(workoutStart, slot.getTime());
            if (slotEndTime.isAfter(latestTime)) {
                latestTime = slotEndTime;
            }
        }
        return latestTime;
    }

    private LocalDateTime parseTimeSlot(LocalDateTime workoutStart, String timeRange) {
        String[] times = timeRange.split("-");
        String endTime = times[1].trim();

        int hour = Integer.parseInt(endTime.split(":")[0]);
        boolean isPM = endTime.contains("PM");

        if (isPM && hour != 12) hour += 12;
        if (!isPM && hour == 12) hour = 0;

        return workoutStart.withHour(hour).withMinute(0).withSecond(0);
    }

    private WorkoutState determineState(LocalDateTime workoutStart, LocalDateTime latestTimeSlot, LocalDateTime now) {
        if (now.isAfter(workoutStart) && now.isBefore(latestTimeSlot)) {
            return WorkoutState.IN_PROGRESS;
        } else if (now.isAfter(latestTimeSlot)) {
            return WorkoutState.WAITING_FOR_FEEDBACK;
        }
        return WorkoutState.SCHEDULED;
    }
}
