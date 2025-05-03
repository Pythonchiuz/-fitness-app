package com.backendfitnessapp.service.impl;

import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.service.WorkoutService;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;

public class WorkoutServiceImpl implements WorkoutService {
    private final DynamoDbTable<Workout> workoutTable;

    public WorkoutServiceImpl() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(System.getenv("REGION"))).build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
        this.workoutTable = enhancedClient.table(System.getenv("WORKOUT_TABLE"), TableSchema.fromBean(Workout.class));
    }

    public List<Workout> getWorkoutsByCoachId(String coachId) {
        return this.workoutTable.scan().items()
                .stream()
                .filter((workout) -> workout.getCoachId().equals(coachId))
                .toList();
    }

    public void saveWorkout(Workout workout) {
        this.workoutTable.putItem(workout);
    }

    public List<Workout> getAllWorkouts() {
        return this.workoutTable.scan().items().stream().toList();
    }

    public List<Workout> getWorkoutsByUserId(String userId) {
        return this.workoutTable.scan().items()
                .stream()
                .filter((workout) -> workout.getUsersId() != null
                        && workout.getUsersId().contains(userId))
                .toList();
    }

    public void updateWorkout(Workout workout) {
        this.workoutTable.updateItem(workout);
    }

    public Workout getWorkoutById(String workoutId) {
        Key key = Key.builder().partitionValue(workoutId).build();
        return this.workoutTable.getItem(key);
    }
}
