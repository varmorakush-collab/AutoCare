package com.example.autocare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Hide Action Bar for onboarding screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button continueButton = findViewById(R.id.continueButton);
        
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity (or fallback to MainActivity if not yet created).
                // Once you create LoginActivity, uncomment the Intent code below.

                try {
                    // Try to launch LoginActivity via class name reflection in case it doesn't exist yet,
                    // this prevents compile errors if the file hasn't been created.
                    Class<?> loginActivityClass = Class.forName("com.example.autocare.LoginActivity");
                    Intent intent = new Intent(OnboardingActivity.this, loginActivityClass);
                    startActivity(intent);
                    finish();
                } catch (ClassNotFoundException e) {
                    // LoginActivity doesn't exist yet, fallback gently:
                    Toast.makeText(OnboardingActivity.this,
                        "LoginActivity not found yet. Please create it!",
                        Toast.LENGTH_LONG).show();

                    // As a temporary measure, you can go to MainActivity instead
                    // Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                    // startActivity(intent);
                    // finish();
                }
            }
        });
    }
}