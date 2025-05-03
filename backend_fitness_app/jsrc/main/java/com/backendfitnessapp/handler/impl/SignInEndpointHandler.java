package com.backendfitnessapp.handler.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.backendfitnessapp.authorisation.CognitoSupport;
import com.backendfitnessapp.dto.SignInRequestBody;
import com.backendfitnessapp.dto.UserRespondBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.exceptions.ValidationException;
import com.backendfitnessapp.handler.EndpointHandler;
import com.backendfitnessapp.service.UserService;
import com.backendfitnessapp.utils.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import java.util.Map;

import static com.backendfitnessapp.utils.ResponseUtils.createResponse;

public class SignInEndpointHandler implements EndpointHandler {
    private final CognitoSupport cognitoSupport;
    private final Gson gson;
    private final UserService userService;

    public SignInEndpointHandler(UserService userService, CognitoSupport cognitoSupport, Gson gson) {
        this.cognitoSupport = cognitoSupport;
        this.gson = gson;
        this.userService = userService;
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            SignInRequestBody signIn = parseAndValidateRequest(requestEvent.getBody());
            signIn.setEmail(signIn.getEmail().toLowerCase());

            AdminInitiateAuthResponse authResponse = this.cognitoSupport.singIn(signIn.getEmail(), signIn.getPassword());

            UserProfile userProfile = this.userService.getUserByEmail(signIn.getEmail());

            UserRespondBody respondBody = new UserRespondBody();
            respondBody.setUserId(userProfile.getUserId());
            respondBody.setName(userProfile.getFirstName() + " " + userProfile.getLastName());
            respondBody.setRole(userProfile.getRole());
            respondBody.setEmail(userProfile.getEmail());

            return createResponse(200, this.gson.toJson(Map.of("token", authResponse.authenticationResult().idToken(), "user", respondBody)));
        } catch (CognitoIdentityProviderException e) {
            return createResponse(404, this.gson.toJson(Map.of("message", "Invalid email or password")));
        } catch (ValidationException | JsonSyntaxException e) {
            return createResponse(400, this.gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            return createResponse(500, this.gson.toJson(Map.of("message", e.getMessage())));
        }
    }
    private SignInRequestBody parseAndValidateRequest(String requestBodyJson) {
        SignInRequestBody requestBody = this.gson.fromJson(requestBodyJson, SignInRequestBody.class);

        ValidationUtils.validateEmail(requestBody.getEmail());
        ValidationUtils.validatePassword(requestBody.getPassword());

        requestBody.setEmail(requestBody.getEmail().toLowerCase());
        return requestBody;
    }
}
