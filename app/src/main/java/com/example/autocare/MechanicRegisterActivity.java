package com.example.autocare;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MechanicRegisterActivity extends AppCompatActivity {

    private EditText mechanicNameEditText, mechanicEmailEditText, mechanicPhoneEditText, mechanicCityEditText;
    private EditText mechanicPasswordEditText, mechanicConfirmPasswordEditText;
    private TextView showMechanicPasswordTextView, showMechanicConfirmPasswordTextView, signInLinkTextView;
    private Button createMechanicAccountButton;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        mechanicNameEditText = findViewById(R.id.mechanicNameEditText);
        mechanicEmailEditText = findViewById(R.id.mechanicEmailEditText);
        mechanicPhoneEditText = findViewById(R.id.mechanicPhoneEditText);
        mechanicCityEditText = findViewById(R.id.mechanicCityEditText);
        mechanicPasswordEditText = findViewById(R.id.mechanicPasswordEditText);
        mechanicConfirmPasswordEditText = findViewById(R.id.mechanicConfirmPasswordEditText);

        showMechanicPasswordTextView = findViewById(R.id.showMechanicPasswordTextView);
        showMechanicConfirmPasswordTextView = findViewById(R.id.showMechanicConfirmPasswordTextView);
        createMechanicAccountButton = findViewById(R.id.createMechanicAccountButton);
        signInLinkTextView = findViewById(R.id.signInLinkTextView);

        showMechanicPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    mechanicPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showMechanicPasswordTextView.setText("SHOW");
                    isPasswordVisible = false;
                } else {
                    mechanicPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showMechanicPasswordTextView.setText("HIDE");
                    isPasswordVisible = true;
                }
                mechanicPasswordEditText.setSelection(mechanicPasswordEditText.getText().length());
            }
        });

        showMechanicConfirmPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible) {
                    mechanicConfirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showMechanicConfirmPasswordTextView.setText("SHOW");
                    isConfirmPasswordVisible = false;
                } else {
                    mechanicConfirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showMechanicConfirmPasswordTextView.setText("HIDE");
                    isConfirmPasswordVisible = true;
                }
                mechanicConfirmPasswordEditText.setSelection(mechanicConfirmPasswordEditText.getText().length());
            }
        });

        signInLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MechanicRegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        createMechanicAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerMechanic();
            }
        });
    }

    private void registerMechanic() {
        String name = mechanicNameEditText.getText().toString().trim();
        String email = mechanicEmailEditText.getText().toString().trim();
        String phone = mechanicPhoneEditText.getText().toString().trim();
        String city = mechanicCityEditText.getText().toString().trim();
        String password = mechanicPasswordEditText.getText().toString().trim();
        String confirmPassword = mechanicConfirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            mechanicNameEditText.setError("Full Name is required");
            mechanicNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mechanicEmailEditText.setError("Valid Email is required");
            mechanicEmailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || !phone.matches("^[6-9]\\d{9}$")) {
            mechanicPhoneEditText.setError("Enter a valid 10-digit Indian phone number");
            mechanicPhoneEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(city)) {
            mechanicCityEditText.setError("City is required");
            mechanicCityEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            mechanicPasswordEditText.setError("Password must be at least 6 characters long");
            mechanicPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            mechanicConfirmPasswordEditText.setError("Passwords do not match");
            mechanicConfirmPasswordEditText.requestFocus();
            return;
        }

        if (databaseHelper.checkEmailExists(email)) {
            Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isInserted = databaseHelper.registerMechanic(name, email, phone, city, password);

        if (isInserted) {
            Toast.makeText(this, "Mechanic Account Created Successfully!", Toast.LENGTH_SHORT).show();

            getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                    .edit()
                    .putString("user_email", email)
                    .putString("user_role", "Mechanic")
                    .apply();

            try {
                Class<?> dashboardClass = Class.forName("com.example.autocare.MechanicDashboardActivity");
                Intent intent = new Intent(MechanicRegisterActivity.this, dashboardClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "MechanicDashboardActivity not found yet. Please create it!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}