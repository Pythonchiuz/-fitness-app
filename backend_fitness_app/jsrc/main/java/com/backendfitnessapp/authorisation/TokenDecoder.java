package com.backendfitnessapp.authorisation;

import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class TokenDecoder {
    private final UserService userService;

    public TokenDecoder(UserService userService) {
        this.userService = userService;
    }

    public Optional<UserProfile> getUserFromIdToken(Map<String, String> headers) throws Exception {
        String idToken = headers.get("Authorization").replace("Bearer ", "");
        String[] parts = idToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid ID Token");
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> attributes = mapper.readValue(payload, Map.class);
            return Optional.ofNullable(this.userService.getUserById(attributes.get("sub").toString()));
        }
    }
}
