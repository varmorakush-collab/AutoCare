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

public class GarageOwnerRegisterActivity extends AppCompatActivity {

    private EditText ownerNameEditText, garageNameEditText, emailEditText, phoneEditText, addressEditText;
    private EditText passwordEditText, confirmPasswordEditText;
    private TextView showPasswordTextView, showConfirmPasswordTextView;
    private Button createAccountButton;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private DatabaseHelper databaseHelper;
    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_owner_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        if (getIntent() != null && getIntent().hasExtra("selected_role")) {
            selectedRole = getIntent().getStringExtra("selected_role");
        }

        ownerNameEditText = findViewById(R.id.ownerNameEditText);
        garageNameEditText = findViewById(R.id.garageNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        showPasswordTextView = findViewById(R.id.showPasswordTextView);
        showConfirmPasswordTextView = findViewById(R.id.showConfirmPasswordTextView);
        createAccountButton = findViewById(R.id.createAccountButton);

        showPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordTextView.setText("SHOW");
                    isPasswordVisible = false;
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordTextView.setText("HIDE");
                    isPasswordVisible = true;
                }
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        showConfirmPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible) {
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showConfirmPasswordTextView.setText("SHOW");
                    isConfirmPasswordVisible = false;
                } else {
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showConfirmPasswordTextView.setText("HIDE");
                    isConfirmPasswordVisible = true;
                }
                confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerGarageOwner();
            }
        });
    }

    private void registerGarageOwner() {
        String ownerName = ownerNameEditText.getText().toString().trim();
        String garageName = garageNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(ownerName)) {
            ownerNameEditText.setError("Owner Name is required");
            ownerNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(garageName)) {
            garageNameEditText.setError("Garage Name is required");
            garageNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || !phone.matches("^[6-9]\\d{9}$")) {
            phoneEditText.setError("Enter a valid 10-digit Indian phone number");
            phoneEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            addressEditText.setError("Garage Address is required");
            addressEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (databaseHelper.checkEmailExists(email)) {
            Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isInserted = databaseHelper.registerGarageOwner(ownerName, garageName, email, phone, address, password);

        if (isInserted) {
            Toast.makeText(this, "Garage Account Created Successfully!", Toast.LENGTH_SHORT).show();

            getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                    .edit()
                    .putString("user_email", email)
                    .putString("user_role", "Garage Owner")
                    .apply();

            try {
                Class<?> dashboardClass = Class.forName("com.example.autocare.GarageOwnerDashboardActivity");
                Intent intent = new Intent(GarageOwnerRegisterActivity.this, dashboardClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "GarageOwnerDashboardActivity not found yet. Please create it!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}