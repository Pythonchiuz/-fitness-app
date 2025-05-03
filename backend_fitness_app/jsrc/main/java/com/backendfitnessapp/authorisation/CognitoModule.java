package com.backendfitnessapp.authorisation;

import com.backendfitnessapp.service.UserService;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;

@Module
public class CognitoModule {

    @Provides
    @Singleton
    public CognitoSupport provideCognitoSupport() {
        return new CognitoSupport();
    }

    @Provides
    @Singleton
    public TokenDecoder provideTokenDecoder(UserService userService) {
        return new TokenDecoder(userService);
    }
}