package com.backendfitnessapp;


import com.backendfitnessapp.authorisation.CognitoModule;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.handler.HandlerModule;
import com.backendfitnessapp.service.*;
import com.backendfitnessapp.utils.UtilsModule;
import com.google.gson.Gson;
import dagger.Component;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
@Component(modules = {HandlerModule.class, UtilsModule.class, CognitoModule.class, ServiceModule.class})
public interface MyApplication {

    @Named("general")
    EndpointHandler getGeneralApiHandler();

    @Named("cors")
    Map<String, String> getCorsHeaders();

    ReportService getReportService();

    Gson getGson();

    GymReportAggregatorService getGymReportAggregatorService();

    CoachReportAggregatorService getCoachReportAggregatorService();

    AmazonSESService getAmazonSESService();
}
