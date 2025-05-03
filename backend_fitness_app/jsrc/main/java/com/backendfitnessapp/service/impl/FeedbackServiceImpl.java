package com.backendfitnessapp.service.impl;

import com.backendfitnessapp.entity.Feedback;
import com.backendfitnessapp.service.FeedbackService;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;

public class FeedbackServiceImpl implements FeedbackService {
    private final DynamoDbTable<Feedback> feedbackTable;

    public FeedbackServiceImpl() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(System.getenv("REGION"))).build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
        this.feedbackTable = enhancedClient.table(System.getenv("FEEDBACK_TABLE"), TableSchema.fromBean(Feedback.class));
    }

    public List<Feedback> getFeedbacksByCoachId(String coachId) {
        return this.feedbackTable.scan().items()
                .stream()
                .filter((feedback) -> feedback.getCoachId().equals(coachId))
                .toList();
    }

    public void saveFeedback(Feedback feedback) {
        this.feedbackTable.putItem(feedback);
    }

    public List<Feedback> getAllFeedbacks() {
        return this.feedbackTable.scan().items().stream().toList();
    }
}
