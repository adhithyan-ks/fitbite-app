package com.example.fitbiteapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SupabaseSession";

    // Auth Keys
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    // Profile Keys
    private static final String KEY_PROFILE_COMPLETE = "PROFILE_COMPLETE";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_AGE = "user_age";
    private static final String KEY_GENDER = "user_gender";
    private static final String KEY_HEIGHT = "user_height";
    private static final String KEY_WEIGHT = "user_weight";
    private static final String KEY_ACTIVITY = "user_activity";
    private static final String KEY_GOAL = "user_goal";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // --- AUTHENTICATION METHODS ---

    public void saveSession(String accessToken, String userId, String email) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "No Email");
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_ACCESS_TOKEN);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    // --- PROFILE & HEALTH DATA METHODS ---

    // Save all the details from DetailsActivity
    public void saveUserProfile(String name, int age, String gender, float height, float weight, String activityLevel, String goal) {
        editor.putString(KEY_NAME, name);
        editor.putInt(KEY_AGE, age);
        editor.putString(KEY_GENDER, gender);
        editor.putFloat(KEY_HEIGHT, height);
        editor.putFloat(KEY_WEIGHT, weight);
        editor.putString(KEY_ACTIVITY, activityLevel);
        editor.putString(KEY_GOAL, goal);

        // Automatically mark profile as complete when saving data
        editor.putBoolean(KEY_PROFILE_COMPLETE, true);
        editor.apply();
    }

    public boolean isProfileComplete() {
        return prefs.getBoolean(KEY_PROFILE_COMPLETE, false);
    }

    // Getters for the BMI Calculator and Home Screen to use
    public String getUserName() {
        return prefs.getString(KEY_NAME, "User");
    }

    public float getUserHeight() {
        return prefs.getFloat(KEY_HEIGHT, 170.0f); // Default 170cm
    }

    public float getUserWeight() {
        return prefs.getFloat(KEY_WEIGHT, 70.0f); // Default 70kg
    }

    public String getUserGoal() {
        return prefs.getString(KEY_GOAL, "Stay Fit");
    }
}