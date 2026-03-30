package com.example.fitbiteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class DetailsActivity extends AppCompatActivity {

    // UI Components
    private ViewFlipper cardFlipper;
    private MaterialButton btnNext;
    private CircularProgressIndicator loadingIndicator;

    // Card 1 Inputs
    private TextInputEditText inputName, inputAge;
    private RadioGroup radioGender;

    // Card 2 Inputs
    private TextInputEditText inputHeight, inputWeight;
    private RadioGroup radioActivity;

    // Card 3 Inputs
    private RadioGroup radioGoals;

    // Variables to hold collected data
    private String name, gender, activityLevel, goal;
    private int age;
    private float height, weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initViews();

        btnNext.setOnClickListener(v -> handleNextClick());
    }

    private void initViews() {
        cardFlipper = findViewById(R.id.card_flipper);
        btnNext = findViewById(R.id.btn_next);
        loadingIndicator = findViewById(R.id.loading_indicator);

        inputName = findViewById(R.id.input_name);
        inputAge = findViewById(R.id.input_age);
        radioGender = findViewById(R.id.radio_gender);

        inputHeight = findViewById(R.id.input_height);
        inputWeight = findViewById(R.id.input_weight);
        radioActivity = findViewById(R.id.radio_activity);

        radioGoals = findViewById(R.id.radio_goals);
    }

    private void handleNextClick() {
        int currentCard = cardFlipper.getDisplayedChild();

        if (currentCard == 0) {
            // Validate and move from Card 1 -> Card 2
            if (validateCard1()) {
                cardFlipper.showNext();
            }
        } else if (currentCard == 1) {
            // Validate and move from Card 2 -> Card 3
            if (validateCard2()) {
                cardFlipper.showNext();
                // Change button text for the final step
                btnNext.setText("Finish");
            }
        } else if (currentCard == 2) {
            // Validate and Submit on Card 3
            if (validateCard3()) {
                submitData();
            }
        }
    }

    // --- VALIDATION METHODS ---

    private boolean validateCard1() {
        String nameStr = inputName.getText().toString().trim();
        String ageStr = inputAge.getText().toString().trim();
        int selectedGenderId = radioGender.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(nameStr) || TextUtils.isEmpty(ageStr) || selectedGenderId == -1) {
            Toast.makeText(this, "Please fill in all basic details", Toast.LENGTH_SHORT).show();
            return false;
        }

        name = nameStr;
        age = Integer.parseInt(ageStr);

        RadioButton selectedGenderBtn = findViewById(selectedGenderId);
        gender = selectedGenderBtn.getText().toString();

        return true;
    }

    private boolean validateCard2() {
        String heightStr = inputHeight.getText().toString().trim();
        String weightStr = inputWeight.getText().toString().trim();
        int selectedActivityId = radioActivity.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr) || selectedActivityId == -1) {
            Toast.makeText(this, "Please fill in all body stats", Toast.LENGTH_SHORT).show();
            return false;
        }

        height = Float.parseFloat(heightStr);
        weight = Float.parseFloat(weightStr);

        RadioButton selectedActivityBtn = findViewById(selectedActivityId);
        activityLevel = selectedActivityBtn.getText().toString();

        return true;
    }

    private boolean validateCard3() {
        int selectedGoalId = radioGoals.getCheckedRadioButtonId();

        if (selectedGoalId == -1) {
            Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show();
            return false;
        }

        RadioButton selectedGoalBtn = findViewById(selectedGoalId);
        goal = selectedGoalBtn.getText().toString();

        return true;
    }

    private void submitData() {
        setLoading(true);

        // 1. SAVE LOCALLY
        SessionManager session = new SessionManager(this);
        session.saveUserProfile(name, age, gender, height, weight, activityLevel, goal);

        // 2. SAVE REMOTELY TO SUPABASE
        // (We will write the Supabase API call here next to save it to your cloud database)

        btnNext.postDelayed(() -> {
            setLoading(false);
            Toast.makeText(DetailsActivity.this, "Profile Saved!", Toast.LENGTH_SHORT).show();

            goToHome();
        }, 1500);
    }

    private void goToHome() {
        Intent intent = new Intent(DetailsActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
            btnNext.setEnabled(false);
            btnNext.setText("Saving...");
        } else {
            loadingIndicator.setVisibility(View.GONE);
            btnNext.setEnabled(true);
            btnNext.setText("Finish & Build Plan");
        }
    }
}