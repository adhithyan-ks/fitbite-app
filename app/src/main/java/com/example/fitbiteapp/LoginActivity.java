package com.example.fitbiteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitbiteapp.api.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout emailLayout, passwordLayout;
    MaterialButton btnLogin;
    TextView signupLink, forgotPassword;
    CircularProgressIndicator loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. AUTO-LOGIN CHECK
        // If user is already logged in, skip this screen immediately
        SessionManager session = new SessionManager(this);

        if (session.isLoggedIn()) {
            if (session.isProfileComplete()) {
                goToHome();
            } else {
                goToDetails();
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        emailLayout = findViewById(R.id.input_email);
        passwordLayout = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);
        signupLink = findViewById(R.id.btn_signup_link);
        loader = findViewById(R.id.loading_indicator);
        // Inside onCreate(), right after your other findViewById calls
        forgotPassword = findViewById(R.id.forgot_password);

        forgotPassword.setOnClickListener(v -> handleForgotPassword());

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.requestFocus();
            emailLayout.setError("Enter Valid Email");
            return;
        }

        setLoading(true);

        SupabaseClient.getInstance().login(email, password, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        // Inside performLogin -> onResponse
                        JSONObject json = new JSONObject(responseData);
                        String accessToken = json.getString("access_token");

// The 'user' object has the email
                        JSONObject userObj = json.getJSONObject("user");
                        String userId = userObj.getString("id");
                        String email = userObj.getString("email"); // Grab it!
                        SessionManager session = new SessionManager(LoginActivity.this);
                        session.saveSession(accessToken, userId, email);

                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // Check if they need to complete their profile
                            if (session.isProfileComplete()) {
                                goToHome();
                            } else {
                                goToDetails();
                            }
                            finish();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> setLoading(false));
                    }
                } else {
                    // Cleaner Error Handling
                    String errorBody = response.body().string();
                    Log.e("LoginActivity", "Login Failed: " + errorBody);

                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void handleForgotPassword() {
        String email = emailLayout.getEditText().getText().toString().trim();

        // Require the user to type their email into the box first
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email address first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.requestFocus();
            emailLayout.setError("Enter Valid Email");
            return;
        }

        setLoading(true);

        // We use a custom redirect scheme just like signup so they come back to the app
        String redirectUrl = "fitbite://reset-callback";

        SupabaseClient.getInstance().sendPasswordResetEmail(email, redirectUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset link sent to your email!", Toast.LENGTH_LONG).show();
                    } else {
                        // Optional: Log the exact error from Supabase for debugging
                        Toast.makeText(LoginActivity.this, "Failed to send reset link.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Helper method to navigate and clear back stack
    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        // This flag ensures the user can't press "Back" to return to the Login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void goToDetails() {
        Intent intent = new Intent(LoginActivity.this, DetailsActivity.class);
        // This flag ensures the user can't press "Back" to return to the Login screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loader.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            loader.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}