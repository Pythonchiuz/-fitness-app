package com.backendfitnessapp.service.impl;

import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.enums.Role;
import com.backendfitnessapp.service.UserService;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;

public class UserServiceImpl implements UserService {
    private final DynamoDbTable<UserProfile> userTable;

    public UserServiceImpl() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(System.getenv("REGION"))).build();
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
        this.userTable = enhancedClient.table(System.getenv("USER_TABLE"), TableSchema.fromBean(UserProfile.class));
    }

    public void save(UserProfile userProfile) {
        this.userTable.putItem(userProfile);
    }

    public List<UserProfile> getAllUsers() {
        return this.userTable.scan().items()
                .stream()
                .filter((user) -> user.getRole().equals(Role.USER))
                .toList();
    }

    public UserProfile getUserById(String userId) {
        Key key = Key.builder().partitionValue(userId).build();
        return this.userTable.getItem(key);
    }

    public UserProfile getUserByEmail(String email) {
        return this.userTable.scan().items()
                .stream()
                .filter((user) -> user.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    public void updateUserProfile(UserProfile userProfile) {
        this.userTable.updateItem(userProfile);
    }

    public List<UserProfile> getAllCoachUsers() {
        return this.userTable.scan().items()
                .stream()
                .filter((user) -> user.getRole().equals(Role.COACH))
                .toList();
    }

    public void deleteUserProfile(String userId) {
        this.userTable.deleteItem(
                Key.builder()
                        .partitionValue(userId)
                        .build()
        );
    }
}
