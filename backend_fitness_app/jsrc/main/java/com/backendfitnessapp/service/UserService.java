package com.backendfitnessapp.service;



import com.backendfitnessapp.entity.UserProfile;

import java.util.List;

public interface UserService {
    void save(UserProfile newUser);
    List<UserProfile> getAllUsers();
    UserProfile getUserById(String userId);
    UserProfile getUserByEmail(String userEmail);
    void updateUserProfile(UserProfile userProfile);
    List<UserProfile> getAllCoachUsers();
    void deleteUserProfile(String userId);
}
