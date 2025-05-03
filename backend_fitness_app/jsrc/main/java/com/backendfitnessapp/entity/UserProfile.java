package com.backendfitnessapp.entity;

import com.backendfitnessapp.enums.ActivityType;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.enums.Target;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class UserProfile {
    private String userId;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
    private String about;
    private String imageUrl;
    private List<String> fileUrls;
    private ActivityType preferableActivity;
    private List<String> specializations;
    private Target target;
    private Double rating;
    private String userSummary;
    private String motivationPitch;
    private List<BookedWorkout> bookedWorkouts;
    private ActivityType activityType;
    private boolean isAccountCreated;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    public void setBookedWorkout(BookedWorkout bookedWorkouts) {
        if (this.bookedWorkouts == null) {
            this.bookedWorkouts = new ArrayList<>();
        }
        this.bookedWorkouts.add(bookedWorkouts);
    }
}
