package com.backendfitnessapp.authorisation;

import com.backendfitnessapp.dto.ChangePasswordRequestBody;
import com.backendfitnessapp.dto.SignUpRequestBody;
import com.backendfitnessapp.entity.UserProfile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class CognitoSupport {
    private final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().region(Region.of(System.getenv("REGION"))).credentialsProvider(DefaultCredentialsProvider.create()).build();
    private String userPoolId = System.getenv("COGNITO_ID");
    private String clientId = System.getenv("CLIENT_ID");

    public AdminCreateUserResponse singUp(SignUpRequestBody signUpRequest) throws AliasExistsException {
        return cognitoClient.adminCreateUser(AdminCreateUserRequest
                .builder()
                .userPoolId(userPoolId)
                .username(signUpRequest.getEmail())
                .temporaryPassword(signUpRequest.getPassword())
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .userAttributes(
                        AttributeType.builder().name("given_name").value(signUpRequest.getFirstName()).build(),
                        AttributeType.builder().name("family_name").value(signUpRequest.getLastName()).build(),
                        AttributeType.builder().name("email").value(signUpRequest.getEmail()).build(),
                        AttributeType.builder().name("email_verified").value("true").build()
                )
                .messageAction("SUPPRESS")
                .build()
        );
    }

    public AdminInitiateAuthResponse singIn(String email, String password) throws AliasExistsException {
        AdminInitiateAuthRequest request = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authParameters(Map.of(
                        "USERNAME", email,
                        "PASSWORD", password
                )).build();
        return cognitoClient.adminInitiateAuth(request);
    }

    public AdminRespondToAuthChallengeResponse confirmSingUp(SignUpRequestBody signUpRequest) {
        AdminInitiateAuthResponse adminInitiateAuthResponse = singIn(signUpRequest.getEmail(), signUpRequest.getPassword());


        if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(adminInitiateAuthResponse.challengeNameAsString())) {
            throw new RuntimeException("Unknown challenge: " + adminInitiateAuthResponse.challengeName());
        }
        return cognitoClient.adminRespondToAuthChallenge(AdminRespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .challengeResponses(Map.of(
                        "USERNAME", signUpRequest.getEmail(),
                        "PASSWORD", signUpRequest.getPassword(),
                        "NEW_PASSWORD", signUpRequest.getPassword()
                ))
                .userPoolId(userPoolId)
                .clientId(clientId)
                .session(adminInitiateAuthResponse.session())
                .build());
    }

    public ChangePasswordResponse changePassword(UserProfile userProfile, ChangePasswordRequestBody changePasswordRequest) {
        AdminInitiateAuthResponse response = singIn(userProfile.getEmail(), changePasswordRequest.getOldPassword());
        return cognitoClient.changePassword(ChangePasswordRequest.builder()
                .accessToken(response.authenticationResult().accessToken())
                .previousPassword(changePasswordRequest.getOldPassword())
                .proposedPassword(changePasswordRequest.getNewPassword())
                .build());
    }
    
}
