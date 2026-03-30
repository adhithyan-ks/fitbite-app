package com.example.fitbiteapp;

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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    TextInputLayout emailLayout, passwordLayout;
    MaterialButton btnSignup;
    TextView loginLink;
    CircularProgressIndicator loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailLayout = findViewById(R.id.input_email);
        passwordLayout = findViewById(R.id.input_password);
        btnSignup = findViewById(R.id.btn_signup);
        loginLink = findViewById(R.id.btn_login_link);
        loader = findViewById(R.id.loading_indicator);

        loginLink.setOnClickListener(v -> finish()); // Go back to login

        btnSignup.setOnClickListener(v -> performSignup());
    }

    private void performSignup() {
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email) || password.length() < 6) {
            Toast.makeText(this, "Valid email & password (min 6 chars) required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.requestFocus();
            emailLayout.setError("Enter Valid Email");
            return;
        }
        if (password.length() < 6) {
            passwordLayout.requestFocus();
            passwordLayout.setError("Password must be at least 6 characters");
            return;
        }

        setLoading(true);

        // Define your custom deep link URL
        String redirectUrl = "fitbite://callback";

        // Pass the redirectUrl to your custom client
        SupabaseClient.getInstance().signUp(email, password, redirectUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(SignupActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        // Update the message so they know to check their email!
                        Toast.makeText(SignupActivity.this, "Check your email for the confirmation link!", Toast.LENGTH_LONG).show();
                        finish();
                    });
                } else {
                    final String errorMsg = response.body().string();
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(SignupActivity.this, "Signup Failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("SignupActivity", "Signup Failed: " + errorMsg);
                    });
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loader.setVisibility(View.VISIBLE);
            btnSignup.setEnabled(false);
        } else {
            loader.setVisibility(View.GONE);
            btnSignup.setEnabled(true);
        }
    }
}