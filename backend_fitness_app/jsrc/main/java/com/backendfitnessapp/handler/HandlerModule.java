package com.backendfitnessapp.handler;

import com.backendfitnessapp.authorisation.CognitoSupport;
import com.backendfitnessapp.authorisation.TokenDecoder;
import com.backendfitnessapp.handler.impl.*;
import com.backendfitnessapp.service.FeedbackService;
import com.backendfitnessapp.service.SqsAsyncQueueSender;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.service.WorkoutService;
import com.backendfitnessapp.service.WorkoutStateUpdater;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Map;

@Module
public class HandlerModule {
    @Named("general")
    @Provides
    @Singleton
    public EndpointHandler provideGeneralEndpointHandler(Map<String, EndpointHandler> endpointHandlers, @Named("notFound") EndpointHandler notFoundEndpointHandler) {
        return new GeneralEndpointHandler(endpointHandlers, notFoundEndpointHandler);
    }

    @Named("notFound")
    @Provides
    @Singleton
    public EndpointHandler provideNotFoundEndpointHandler() {
        return new NotFoundEndpointHandler();
    }

    @Provides
    @IntoMap
    @StringKey("^POST:/auth/sign-in$")
    @Singleton
    public EndpointHandler provideSignInEndpointHandler(UserService userService, CognitoSupport cognitoSupport, Gson gson) {
        return new SignInEndpointHandler(userService, cognitoSupport, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^POST:/auth/sign-up$")
    @Singleton
    public EndpointHandler provideSignUpEndpointHandler(CognitoSupport cognitoSupport, UserService userService, Gson gson) {
        return new SignUpEndpointHandler(cognitoSupport, userService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/coaches$")
    @Singleton
    public EndpointHandler provideCoachesEndpointHandler(UserService userService, Gson gson) {
        return new CoachesEndpointHandler(userService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/coaches/[^/]+$")
    @Singleton
    public EndpointHandler provideCoachesByIdEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson) {
        return new CoachesByIdEndpointHandler(userService, workoutService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/coaches/[^/]+/available-slots/\\d{4}-\\d{2}-\\d{2}$")
    @Singleton
    public EndpointHandler provideCoachesAvailableEndpointHandler(WorkoutService workoutService, Gson gson) {
        return new CoachesAvailableEndpointHandler(workoutService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/coaches/[^/]+/feedbacks$")
    @Singleton
    public EndpointHandler provideCoachesFeedbackEndpointHandler(UserService userService, FeedbackService feedbackService, @Named("dateGson") Gson gson) {
        return new CoachesFeedbackEndpointHandler(userService, feedbackService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/workouts/available$")
    @Singleton
    public EndpointHandler provideAvailableWorkoutsEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson, WorkoutStateUpdater workoutStateUpdater) {
        return new AvailableWorkoutsEndpointHandler(userService, workoutService, gson, workoutStateUpdater);
    }

    @Provides
    @IntoMap
    @StringKey("^POST:/workouts$")
    @Singleton
    public EndpointHandler provideBookWorkoutEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson) {
        return new BookWorkoutEndpointHandler(userService, workoutService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/workouts/booked$")
    @Singleton
    public EndpointHandler provideBookedWorkoutEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson, WorkoutStateUpdater workoutStateUpdater) {
        return new BookedWorkoutsEndpointHandler(userService, workoutService, gson, workoutStateUpdater);
    }

    @Provides
    @IntoMap
    @StringKey("^DELETE:/workouts/[^/]+$")
    @Singleton
    public EndpointHandler provideCancelWorkoutEndpointHandler(WorkoutService workoutService, UserService userService, Gson gson, TokenDecoder tokenDecoder) {
        return new CancelWorkoutEndpointHandler(workoutService, userService, gson, tokenDecoder);
    }

    @Provides
    @IntoMap
    @StringKey("^POST:/feedbacks$")
    @Singleton
    public EndpointHandler provideFeedbackEndpointHandler(UserService userService, FeedbackService feedbackService, WorkoutService workoutService, Gson gson, TokenDecoder tokenDecoder, SqsAsyncQueueSender sqsAsyncQueueSender) {
        return new PostFeedbackEndpointHandler(userService, feedbackService, workoutService, gson, tokenDecoder, sqsAsyncQueueSender);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/feedbacks/[^/]+$")
    @Singleton
    public EndpointHandler provideGetFeedbackEndpointHandler(UserService userService, FeedbackService feedbackService, Gson gson) {
        return new GetFeedbacksByUserEndpointHandler(userService, feedbackService, gson);
    }

    @Provides
    @IntoMap
    @StringKey("^GET:/users/[^/]+$")
    @Singleton
    public EndpointHandler provideUserGetEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson) {
        return new NotFoundEndpointHandler();
    }

    @Provides
    @IntoMap
    @StringKey("^PUT:/users/[^/]+$")
    @Singleton
    public EndpointHandler provideUserPutEndpointHandler() {
        return new NotFoundEndpointHandler();
    }

    @Provides
    @IntoMap
    @StringKey("^PUT:/users/[^/]+/password$")
    @Singleton
    public EndpointHandler provideUserPasswordEndpointHandler(UserService userService, WorkoutService workoutService, Gson gson) {
        return new NotFoundEndpointHandler();
    }
}
