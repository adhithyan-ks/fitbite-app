package com.example.fitbiteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // Account UI
    private TextView textName, textEmail;
    // Health UI
    private TextView textAge, textGender, textHeight, textWeight;
    // Lifestyle UI
    private TextView textActivity, textGoal;

    private MaterialButton btnLogout, btnEditProfile;
    private CircularProgressIndicator loader;
    private MaterialToolbar toolbar;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Edge-to-Edge Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        initViews();
        setupListeners();
        loadUserProfile();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        loader = findViewById(R.id.loading_indicator);
        btnLogout = findViewById(R.id.btn_logout);
        btnEditProfile = findViewById(R.id.btn_edit_profile);

        // Account
        textName = findViewById(R.id.text_name);
        textEmail = findViewById(R.id.text_email);

        // Stats
        textAge = findViewById(R.id.text_age);
        textGender = findViewById(R.id.text_gender);
        textHeight = findViewById(R.id.text_height);
        textWeight = findViewById(R.id.text_weight);

        // Lifestyle
        textActivity = findViewById(R.id.text_activity);
        textGoal = findViewById(R.id.text_goal);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        // Fetch from SessionManager
        textName.setText(session.getUserName());
        textEmail.setText(session.getEmail());

        // We need to fetch age, gender, activity directly from SharedPreferences
        // using the keys defined in SessionManager
        android.content.SharedPreferences prefs = getSharedPreferences("SupabaseSession", MODE_PRIVATE);

        int age = prefs.getInt("user_age", 0);
        String gender = prefs.getString("user_gender", "Not Set");
        String activity = prefs.getString("user_activity", "Not Set");

        // Set Stats
        textAge.setText("Age: " + (age > 0 ? age : "--"));
        textGender.setText("Gender: " + gender);
        textHeight.setText(String.format(Locale.getDefault(), "Height: %.1f cm", session.getUserHeight()));
        textWeight.setText(String.format(Locale.getDefault(), "Weight: %.1f kg", session.getUserWeight()));

        // Set Lifestyle
        textActivity.setText("Activity Level: " + activity);
        textGoal.setText("Goal: " + session.getUserGoal());
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setIcon(R.drawable.logout_24px)
                .setMessage("Are you sure you want to log out of your account? You will lose your personal information.")
                .setPositiveButton("Logout", (dialog, which) -> {
                    logoutUser();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User clicked Cancel, just dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }
    private void logoutUser() {
        session.logout();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}