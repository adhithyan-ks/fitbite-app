package com.example.fitbiteapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText editName, editAge, editHeight, editWeight;
    private AutoCompleteTextView editGender, editActivity, editGoal;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        session = new SessionManager(this);

        initViews();
        setupDropdowns();
        loadCurrentData();

        toolbar.setNavigationOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnSave = findViewById(R.id.btn_save);

        editName = findViewById(R.id.edit_name);
        editAge = findViewById(R.id.edit_age);
        editHeight = findViewById(R.id.edit_height);
        editWeight = findViewById(R.id.edit_weight);

        editGender = findViewById(R.id.edit_gender);
        editActivity = findViewById(R.id.edit_activity);
        editGoal = findViewById(R.id.edit_goal);
    }

    private void setupDropdowns() {
        // Gender Options
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        editGender.setAdapter(genderAdapter);

        // Activity Options
        String[] activities = {"Low (Sedentary)", "Moderate (Active)", "High (Very Active)"};
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, activities);
        editActivity.setAdapter(activityAdapter);

        // Goal Options
        String[] goals = {"Weight Loss", "Weight Gain", "Stay Fit", "Manage Sugar / BP"};
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, goals);
        editGoal.setAdapter(goalAdapter);
    }

    private void loadCurrentData() {
        editName.setText(session.getUserName());
        editHeight.setText(String.valueOf(session.getUserHeight()));
        editWeight.setText(String.valueOf(session.getUserWeight()));

        android.content.SharedPreferences prefs = getSharedPreferences("SupabaseSession", MODE_PRIVATE);

        int age = prefs.getInt("user_age", 0);
        if (age > 0) editAge.setText(String.valueOf(age));

        // Use setText(text, false) for AutoCompleteTextView to prevent the dropdown from popping open instantly on load
        editGender.setText(prefs.getString("user_gender", ""), false);
        editActivity.setText(prefs.getString("user_activity", ""), false);
        editGoal.setText(prefs.getString("user_goal", ""), false);
    }

    private void saveChanges() {
        String nameStr = editName.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();
        String heightStr = editHeight.getText().toString().trim();
        String weightStr = editWeight.getText().toString().trim();

        String genderStr = editGender.getText().toString().trim();
        String activityStr = editActivity.getText().toString().trim();
        String goalStr = editGoal.getText().toString().trim();

        if (TextUtils.isEmpty(nameStr) || TextUtils.isEmpty(ageStr) ||
                TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr) ||
                TextUtils.isEmpty(genderStr) || TextUtils.isEmpty(activityStr) || TextUtils.isEmpty(goalStr)) {

            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        float height = Float.parseFloat(heightStr);
        float weight = Float.parseFloat(weightStr);

        // Save everything back to SessionManager
        session.saveUserProfile(nameStr, age, genderStr, height, weight, activityStr, goalStr);

        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        finish();
    }
}