package com.example.fitbiteapp.api;

import com.google.gson.Gson;

import org.json.JSONObject;

import okhttp3.*;
import java.io.IOException;

public class SupabaseClient {
    private static final String PROJECT_URL = "https://vihecvygoflncmllomex.supabase.co";
    private static final String REST_URL = PROJECT_URL + "/rest/v1/";
    private static final String AUTH_URL = PROJECT_URL + "/auth/v1/";
    private static final String API_KEY = "sb_publishable_xTLnjjm7_e8ZxivyUDChiQ_jZQUe9bL";

    private static SupabaseClient instance;
    private final OkHttpClient client;
    private final Gson gson;

    private SupabaseClient() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) instance = new SupabaseClient();
        return instance;
    }

    // --- AUTH METHODS ---

    public void login(String email, String password, Callback callback) {
        String url = AUTH_URL + "token?grant_type=password";
        // Create JSON payload manually for Auth to avoid GSON overhead on simple strings
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = buildRequest(url).post(body).build();
        client.newCall(request).enqueue(callback);
    }

    // 1. Add 'String redirectTo' to the parameters
    public void signUp(String email, String password, String redirectTo, Callback callback) {

        // 2. Append the redirect_to parameter to the URL
        String url = AUTH_URL + "signup?redirect_to=" + redirectTo;

        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = buildRequest(url).post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public void sendPasswordResetEmail(String email, String redirectTo, Callback callback) {
        // The endpoint is /recover
        String url = AUTH_URL + "recover?redirect_to=" + redirectTo;

        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = buildRequest(url).post(body).build();
        client.newCall(request).enqueue(callback);
    }
    // Add this to your AUTH METHODS section
    public void updatePassword(String newPassword, String accessToken, Callback callback) {
        String url = AUTH_URL + "user"; // The endpoint to update user data

        JSONObject json = new JSONObject();
        try {
            json.put("password", newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        // Use the recovery access_token in the Authorization header
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .put(body) // Note: This is a PUT request
                .build();

        client.newCall(request).enqueue(callback);
    }

    // --- DATABASE METHODS (CRUD) ---

    // CREATE
    public void insert(String table, Object dataObject, Callback callback) {
        String json = gson.toJson(dataObject);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = buildRequest(REST_URL + table)
                .post(body)
                .addHeader("Prefer", "return=minimal")
                .build();
        client.newCall(request).enqueue(callback);
    }

    // READ
    public void select(String table, String selectQuery, Callback callback) {
        String url = REST_URL + table + "?select=" + selectQuery;
        Request request = buildRequest(url).get().build();
        client.newCall(request).enqueue(callback);
    }

    // UPDATE
    public void update(String table, Object dataObject, String filter, Callback callback) {
        String url = REST_URL + table + "?" + filter;
        String json = gson.toJson(dataObject);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = buildRequest(url)
                .patch(body)
                .addHeader("Prefer", "return=minimal")
                .build();
        client.newCall(request).enqueue(callback);
    }

    // DELETE
    public void delete(String table, String filter, Callback callback) {
        String url = REST_URL + table + "?" + filter;
        Request request = buildRequest(url).delete().build();
        client.newCall(request).enqueue(callback);
    }

    // --- HELPER ---

    /**
     * Simplified request builder using only the API_KEY for Authorization.
     */
    private Request.Builder buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json");
    }
}