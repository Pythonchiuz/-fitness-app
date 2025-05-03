package com.backendfitnessapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;

import java.time.LocalDate;
import java.util.Map;

@Module
public class UtilsModule {

    @Provides
    @Named(value = "cors")
    public Map<String, String> provideCors() {
        return Map.of(
                "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Accept-Version", "*"
        );
    }
    @Provides
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Named("dateGson")
    public Gson provideDateAdapterGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }
}
