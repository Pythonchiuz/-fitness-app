package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.backendfitnessapp.authorisation.CognitoSupport;
import com.backendfitnessapp.dto.SignUpRequestBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.exceptions.UserAlreadyExitsException;
import com.backendfitnessapp.exceptions.ValidationException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.utils.UserMapper;
import com.backendfitnessapp.utils.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.Map;
import java.util.Optional;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class SignUpEndpointHandler implements EndpointHandler {
    private final UserService userService;
    private final CognitoSupport cognitoSupport;
    private final Gson gson;

    public SignUpEndpointHandler(CognitoSupport cognitoSupport, UserService userService, Gson gson) {
        this.cognitoSupport = cognitoSupport;
        this.userService = userService;
        this.gson = gson;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        context.getLogger().log("request body: " + requestEvent.getBody());

        try {
            SignUpRequestBody requestBody = this.parseAndValidateRequest(requestEvent.getBody());

            UserProfile userProfile = this.handleExistingUser(requestBody.getEmail());

            String userId = this.registerUserInCognito(requestBody);

            userProfile = this.createOrUpdateUserProfile(requestBody, userProfile, userId);

            this.userService.save(userProfile);

            AdminRespondToAuthChallengeResponse challengeResponse = this.cognitoSupport.confirmSingUp(requestBody);

            context.getLogger().log("challenge response : " + challengeResponse);
            return createResponse(200, this.gson.toJson(Map.of("message", "User successfully registered")));
        } catch (ValidationException | JsonSyntaxException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (UserAlreadyExitsException e) {
            return createResponse(409, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }

    private SignUpRequestBody parseAndValidateRequest(String requestBodyJson) {
        SignUpRequestBody requestBody = this.gson.fromJson(requestBodyJson, SignUpRequestBody.class);

        ValidationUtils.validateEmail(requestBody.getEmail());
        ValidationUtils.validatePassword(requestBody.getPassword());

        requestBody.setEmail(requestBody.getEmail().toLowerCase());
        return requestBody;
    }

    private UserProfile handleExistingUser(String email) {

        UserProfile tempProfile = this.userService.getUserByEmail(email);
        if (tempProfile != null && !tempProfile.isAccountCreated()) {
            this.userService.deleteUserProfile(tempProfile.getUserId());
            return UserMapper.tempUserToUserProfile(tempProfile);
        } else {
            Optional.ofNullable(this.userService.getUserByEmail(email)).ifPresent((user) -> {
                throw new UserAlreadyExitsException("User already exists");
            });
            return null;
        }
    }

    private String registerUserInCognito(SignUpRequestBody requestBody) {
        return this.cognitoSupport.singUp(requestBody).user().attributes()
                .stream()
                .filter((attr) -> "sub".equals(attr.name()))
                .map(AttributeType::value).findAny()
                .orElseThrow(() -> new RuntimeException("Sub not found."));
    }

    private UserProfile createOrUpdateUserProfile(SignUpRequestBody requestBody, UserProfile existingUser, String userId) {
        UserProfile userProfile = UserMapper.signUpToUserProfile(requestBody, existingUser);
        userProfile.setRole(existingUser == null ? Role.USER : Role.COACH);
        userProfile.setUserId(userId);
        userProfile.setAccountCreated(true);

        return userProfile;
    }
}
