package com.backendfitnessapp.service;


import com.backendfitnessapp.service.impl.*;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;

@Module
public class ServiceModule {

    @Provides
    @Singleton
    public UserService provideUserService() {
        return new UserServiceImpl();
    }

    @Provides
    @Singleton
    public WorkoutService provideWorkoutService() {
        return new WorkoutServiceImpl();
    }

    @Provides
    @Singleton
    public FeedbackService provideFeedbackService() {
        return new FeedbackServiceImpl();
    }

    @Provides
    @Singleton
    public WorkoutStateUpdater provideWorkoutStateUpdater(UserService userService, WorkoutService workoutService, SqsAsyncQueueSender  sqsAsyncQueueSender) {
        return new WorkoutStateUpdater(userService, workoutService, sqsAsyncQueueSender);
    }

    @Provides
    @Singleton
    public ReportService provideReportService() {
        return new ReportServiceImpl();
    }

    @Provides
    public SqsAsyncQueueSender provideSqsAsyncQueueSender(UserService userService, Gson gson) {
        return new SqsAsyncQueueSender(userService, gson);
    }
    @Provides
    @Singleton
    public GymReportAggregatorService provideGymReportAggregatorService() {
        return new GymReportAggregatorServiceImpl();
    }

    @Provides
    @Singleton
    public CoachReportAggregatorService provideCoachReportAggregatorService() {
        return new CoachReportAggregatorServiceImpl();
    }

    @Provides
    @Singleton
    public AmazonSESService provideAmazonSESService() {
        return new AmazonSESService();
    }
}
