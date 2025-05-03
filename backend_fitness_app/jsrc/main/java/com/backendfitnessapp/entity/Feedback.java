package com.backendfitnessapp.entity;

import com.backendfitnessapp.enums.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Feedback {
    private String feedbackId;
    private String workoutId;
    private String userId;
    private String coachId;
    private Integer rating;
    private String comment;
    private String createdDate;
    private FeedbackType feedbackType;

    @DynamoDbPartitionKey
    public String getFeedbackId() {
        return feedbackId;
    }
}
