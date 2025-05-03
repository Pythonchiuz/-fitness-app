package com.backendfitnessapp.entity;

import com.backendfitnessapp.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private String reportId;
    private String reportDate;
    private String coach;
    private String email;
    private String workoutId;
    private ActivityType workoutType;
    private String workoutDate;
    private int workoutCount;
    private int workoutCapacity;
    private int attendanceCount;
    private String location;
    private List<Integer> feedbacks;

    @DynamoDbPartitionKey
    public String getReportId() {
        return reportId;
    }

    public void setFeedback(Integer feedbackRating) {
        if (this.feedbacks == null) {
          this.feedbacks = new ArrayList<>();
        }
        this.feedbacks.add(feedbackRating);
    }
}