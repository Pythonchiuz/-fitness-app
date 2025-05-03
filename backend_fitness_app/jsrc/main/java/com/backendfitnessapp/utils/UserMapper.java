package com.backendfitnessapp.utils;


import com.backendfitnessapp.dto.CoachesDetailedRespondBody;
import com.backendfitnessapp.dto.CoachesRespondBody;
import com.backendfitnessapp.dto.SignUpRequestBody;
import com.backendfitnessapp.entity.UserProfile;
import com.backendfitnessapp.entity.Workout;
import com.backendfitnessapp.enums.ActivityType;

import java.util.List;

public class UserMapper {
    public UserMapper() {
    }

    public static UserProfile tempUserToUserProfile(UserProfile tempUser) {
        UserProfile userProfile = new UserProfile();

        userProfile.setAbout(tempUser.getAbout());
        userProfile.setAccountCreated(true);
        userProfile.setActivityType(tempUser.getActivityType());
        userProfile.setImageUrl(tempUser.getImageUrl());
        userProfile.setMotivationPitch(tempUser.getMotivationPitch());
        userProfile.setRating(tempUser.getRating());
        userProfile.setSpecializations(tempUser.getSpecializations());
        userProfile.setUserSummary(tempUser.getUserSummary());

        return userProfile;
    }

    public static UserProfile signUpToUserProfile(SignUpRequestBody signUpRequestBody, UserProfile currentUser) {
        if (currentUser == null) {
            currentUser = new UserProfile();
        }

        currentUser.setEmail(signUpRequestBody.getEmail());
        currentUser.setFirstName(signUpRequestBody.getFirstName());
        currentUser.setLastName(signUpRequestBody.getLastName());
        currentUser.setPreferableActivity(signUpRequestBody.getPreferableActivity());
        currentUser.setTarget(signUpRequestBody.getTarget());

        return currentUser;
    }

    public static CoachesRespondBody userProfileToCoachesRespond(UserProfile userProfile) {
        CoachesRespondBody coachesRespondBody = new CoachesRespondBody();

        coachesRespondBody.setCoachId(userProfile.getUserId());
        coachesRespondBody.setName(userProfile.getFirstName() + " " + userProfile.getLastName());
        coachesRespondBody.setSummary(userProfile.getUserSummary());
        coachesRespondBody.setMotivationPitch(userProfile.getMotivationPitch());
        coachesRespondBody.setImageUrl(userProfile.getImageUrl());
        coachesRespondBody.setRating(userProfile.getRating());

        return coachesRespondBody;
    }

    public static CoachesDetailedRespondBody userProfileToCoachesDetailedRespond(UserProfile userProfile, List<String> workouts) {
        CoachesDetailedRespondBody coachesDetailedRespondBody = new CoachesDetailedRespondBody();

        coachesDetailedRespondBody.setCoachId(userProfile.getUserId());
        coachesDetailedRespondBody.setName( userProfile.getFirstName() + " " + userProfile.getLastName());
        coachesDetailedRespondBody.setSummary(userProfile.getUserSummary());
        coachesDetailedRespondBody.setImageUrl(userProfile.getImageUrl());
        coachesDetailedRespondBody.setRating(userProfile.getRating());
        coachesDetailedRespondBody.setMotivationPitch(userProfile.getMotivationPitch());
        coachesDetailedRespondBody.setSpecializations(userProfile.getSpecializations());
        coachesDetailedRespondBody.setAbout(userProfile.getAbout());
        coachesDetailedRespondBody.setWorkoutDates(workouts);
        coachesDetailedRespondBody.setFileUrls(userProfile.getFileUrls());

        return coachesDetailedRespondBody;
    }
}
