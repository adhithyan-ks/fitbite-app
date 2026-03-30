package com.example.fitbiteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class BmiActivity extends AppCompatActivity {

    private TextView txtWeightVal, txtHeightVal;
    private TextView txtBmiResult, txtBmiCategory, txtHealthTip;
    private Slider sliderWeight, sliderHeight;
    private Button btnCalculate, btnPersonalizedPlan;
    private MaterialToolbar topAppBar;
    private MaterialCardView cardResult;
    private float currentWeight;
    private float currentHeight;
    private float currentBmi = 0.0f;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        session = new SessionManager(this);

        initViews();

        currentWeight = session.getUserWeight();
        currentHeight = session.getUserHeight();

        if (currentWeight < 20.0f) currentWeight = 20.0f;
        if (currentWeight > 150.0f) currentWeight = 150.0f;

        if (currentHeight < 50.0f) currentHeight = 50.0f;
        if (currentHeight > 220.0f) currentHeight = 220.0f;

        sliderWeight.setValue(currentWeight);
        sliderHeight.setValue(currentHeight);

        txtWeightVal.setText(String.format(Locale.getDefault(), "%.1f kg", currentWeight));
        txtHeightVal.setText(String.format(Locale.getDefault(), "%.1f cm", currentHeight));

        // Hide card initially
        cardResult.setVisibility(View.GONE);

        topAppBar.setNavigationOnClickListener(v -> finish());
        setupSliderListeners();

        btnCalculate.setOnClickListener(v -> calculateBMI());

        // Launch ChatActivity when clicking Personalized Plan
        // Launch ChatActivity and pass the data
        btnPersonalizedPlan.setOnClickListener(v -> {
            Intent intent = new Intent(BmiActivity.this, ChatActivity.class);
            intent.putExtra("from_bmi_calculator", true);
            intent.putExtra("passed_bmi", currentBmi);
            intent.putExtra("passed_weight", currentWeight);
            intent.putExtra("passed_height", currentHeight);
            startActivity(intent);
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);

        txtWeightVal = findViewById(R.id.txtWeightVal);
        txtHeightVal = findViewById(R.id.txtHeightVal);

        sliderWeight = findViewById(R.id.sliderWeight);
        sliderHeight = findViewById(R.id.sliderHeight);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnPersonalizedPlan = findViewById(R.id.btnPersonalizedPlan);

        txtBmiResult = findViewById(R.id.txtBmiResult);
        txtBmiCategory = findViewById(R.id.txtBmiCategory);
        txtHealthTip = findViewById(R.id.txtHealthTip);

        cardResult = findViewById(R.id.cardResult);
    }

    private void setupSliderListeners() {
        sliderWeight.addOnChangeListener((slider, value, fromUser) -> {
            currentWeight = value;
            txtWeightVal.setText(String.format(Locale.getDefault(), "%.1f kg", currentWeight));
        });

        sliderHeight.addOnChangeListener((slider, value, fromUser) -> {
            currentHeight = value;
            txtHeightVal.setText(String.format(Locale.getDefault(), "%.1f cm", currentHeight));
        });
    }

    private void calculateBMI() {
        cardResult.setVisibility(View.VISIBLE);

        float heightInMeters = currentHeight / 100.0f;
        float bmi = currentWeight / (heightInMeters * heightInMeters);

        currentBmi = bmi;

        txtBmiResult.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        updateResultUI(bmi);
    }

    private void updateResultUI(float bmi) {
        String category;
        String tip;
        int colorResId;

        if (bmi < 18.5) {
            category = "Underweight";
            tip = "✔ Focus on nutrient-dense foods (nuts, dairy)\n✔ Add healthy fats to meals\n✔ Consider strength training to build mass safely";
            colorResId = android.R.color.holo_blue_light;

        } else if (bmi >= 18.5 && bmi < 25) {
            category = "Normal Weight";
            tip = "✔ Maintain a balanced diet\n✔ Aim for 30 mins of daily exercise\n✔ Stay hydrated (2.5L water daily)";
            colorResId = android.R.color.holo_green_dark;

        } else if (bmi >= 25 && bmi < 30) {
            category = "Overweight";
            tip = "✔ Reduce sugar and refined carbs\n✔ Aim for 30 mins walking daily\n✔ Increase protein and fiber intake";
            colorResId = android.R.color.holo_orange_dark;

        } else {
            category = "Obese";
            tip = "✔ Focus on low-calorie, high-volume foods\n✔ Start with low-impact cardio (walking/swimming)\n✔ Consult a specialist for a tailored plan";
            colorResId = android.R.color.holo_red_dark;
        }

        txtBmiCategory.setText(category);
        txtHealthTip.setText(tip);

        txtBmiCategory.setTextColor(ContextCompat.getColor(this, colorResId));
        txtBmiResult.setTextColor(ContextCompat.getColor(this, colorResId));
    }
}