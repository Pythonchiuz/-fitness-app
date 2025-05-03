package com.backendfitnessapp.service;

import com.backendfitnessapp.entity.Workout;

import java.util.List;

public interface WorkoutService {
    List<Workout> getWorkoutsByCoachId(String coachId);
    void saveWorkout(Workout workout);
    List<Workout> getAllWorkouts();
    List<Workout> getWorkoutsByUserId(String userId);
    void updateWorkout(Workout workout);
    Workout getWorkoutById(String workoutId);
}
