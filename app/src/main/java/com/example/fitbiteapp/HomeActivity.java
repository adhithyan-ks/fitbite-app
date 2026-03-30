package com.example.fitbiteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class HomeActivity extends AppCompatActivity {
    MaterialCardView cardScanFood, cardBmiCalculator;
    FloatingActionButton fabChat;
    TextView txtDailyTip, homeGreet;
    ImageView ivProfile;
    private SessionManager session;
    String[] healthTips = {
            "💧 Drink at least 2.5L water today to stay hydrated.",
            "🥗 Fill half your plate with vegetables for better digestion.",
            "🚶 Take a 10-minute walk after lunch to lower blood sugar.",
            "😴 Aim for 7-8 hours of sleep to help your muscles recover.",
            "🚫 Avoid sugary drinks; try lemon water or green tea instead.",
            "💪 Protein keeps you full longer. Add eggs or beans to your meal.",
            "🧘 Stress causes weight gain. Take 5 deep breaths right now.",
            "🍎 Eat fruit instead of drinking juice to get more fiber.",
            "📉 Chew your food slowly—it takes 20 mins for your brain to feel full."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        session = new SessionManager(this);
        if (session.isLoggedIn() && !session.isProfileComplete()) {
            // ...kick them straight back to the Details form!
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_home);

        cardBmiCalculator = findViewById(R.id.cardBmiCalculator);
        cardScanFood = findViewById(R.id.cardScanFood);
        txtDailyTip = findViewById(R.id.txtDailyTip);
        fabChat = findViewById(R.id.fabChat);
        ivProfile = findViewById(R.id.ivProfile);
        homeGreet = findViewById(R.id.homeGreet);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setRandomTip();

        cardScanFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, FoodUploadActivity.class);
                startActivity(i);
            }
        });
        cardBmiCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, BmiActivity.class);
                startActivity(i);
            }
        });
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeGreet.setText("Hello, "+session.getUserName()+" 👋");
    }

    private void setRandomTip() {
        Random random = new Random();
        int index = random.nextInt(healthTips.length);
        txtDailyTip.setText(healthTips[index]);
    }
}