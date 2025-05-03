package com.backendfitnessapp.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.backendfitnessapp.dto.SqsReportBody;
import com.backendfitnessapp.entity.Feedback;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.google.gson.Gson;

import java.time.LocalDateTime;

public class SqsAsyncQueueSender {
    private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private final String queue_url = sqs.getQueueUrl(System.getenv("QUEUE_NAME")).getQueueUrl();

    private final Gson gson;
    private final UserService userService;
    public SqsAsyncQueueSender(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }


    public void report(Feedback feedback, Workout workout) {
        UserProfile coach = userService.getUserById(workout.getCoachId());

        SqsReportBody reportBody = SqsReportBody.builder()
                .coach(coach.getFirstName() + " " + coach.getLastName())
                .email(coach.getEmail())
                .workoutId(workout.getWorkoutId())
                .workoutType(workout.getActivity())
                .workoutDate(LocalDateTime.parse(workout.getDateTime()).toLocalDate().toString())
                .workoutCount(workout.getTimeSlots().size())
                .workoutCapacity(workout.getCapacity() * workout.getTimeSlots().size())
                .attendanceCount(workout.getUsersId() != null ? workout.getUsersId().size() : 0)
                .location(workout.getLocation())
                .feedbackRating(feedback == null ? null : feedback.getRating()).build();

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queue_url)
                .withMessageBody(gson.toJson(reportBody))
                .withDelaySeconds(5);

        sqs.sendMessage(send_msg_request);
    }
}
