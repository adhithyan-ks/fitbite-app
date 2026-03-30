package com.example.fitbiteapp;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.example.fitbiteapp.HomeActivity;
import com.example.fitbiteapp.LoginActivity;
import com.example.fitbiteapp.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //

        // Make it look modern (Edge-to-Edge)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system bars overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Delay for 2 seconds to show the branding, then decide navigation
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSessionAndNavigate, 2000);
    }

    private void checkSessionAndNavigate() {
        SessionManager session = new SessionManager(this);

        Intent intent;
        if (session.isLoggedIn()) {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        } else {
            // User is new/logged out -> Go to Login
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Kill the Splash Screen so user can't back into it
    }
}