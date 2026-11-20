package com.example.autocare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the Action Bar for a cleaner splash screen look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Delay for 2 seconds (2000 milliseconds) then move to the next screen
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is already logged in
                android.content.SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
                String userEmail = prefs.getString("user_email", null);
                String userRole = prefs.getString("user_role", null);

                if (userEmail != null && userRole != null) {
                    if (userRole.equals("Garage Owner")) {
                        Intent intent = new Intent(SplashActivity.this, GarageOwnerDashboardActivity.class);
                        startActivity(intent);
                    } else if (userRole.equals("Mechanic")) {
                        Intent intent = new Intent(SplashActivity.this, MechanicDashboardActivity.class);
                        startActivity(intent);
                    } else if (userRole.equals("Customer")) {
                        Intent intent = new Intent(SplashActivity.this, CustomerDashboardActivity.class);
                        startActivity(intent);
                    } else if (userRole.equals("Admin")) {
                        Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                        startActivity(intent);
                    }
                } else {
                    // Navigate to the next activity (OnboardingActivity)
                    Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                    startActivity(intent);
                }
                
                // Finish SplashActivity so the user can't go back to it
                finish();
            }
        }, 2000);
    }
}