package com.backendfitnessapp.entity;

import com.backendfitnessapp.enums.WorkoutState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class BookedWorkout {
    private String bookedWorkoutId;
    private String timeSlot;
    private WorkoutState workoutState;
}
