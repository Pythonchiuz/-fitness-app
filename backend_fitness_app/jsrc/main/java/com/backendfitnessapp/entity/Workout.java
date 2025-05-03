package com.backendfitnessapp.entity;

import com.backendfitnessapp.enums.ActivityType;
import com.backendfitnessapp.enums.WorkoutState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean

public class Workout {
    private String workoutId;
    private String duration;
    private ActivityType activity;
    private List<String> usersId;
    private String coachId;
    private String motivationPitch;
    private String workoutName;
    private WorkoutState state;
    private String dateTime;
    private List<TimeSlot> timeSlots;
    private int capacity;
    private String location;

    @DynamoDbPartitionKey
    public String getWorkoutId() {
        return workoutId;
    }

    public void setUserId(String userId) {
        if (this.usersId == null) {
            this.usersId = new ArrayList<>();
        }
        this.usersId.add(userId);
    }
    public void setTimeSlot(TimeSlot timeSlots) {
        if (this.timeSlots == null) {
            this.timeSlots = new ArrayList();
        }

        this.timeSlots.add(timeSlots);
    }
}
