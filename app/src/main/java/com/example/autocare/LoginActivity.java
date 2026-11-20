package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView showPasswordTextView;
    private TextView forgotPasswordTextView;
    private Button signInButton;
    private TextView createAccountTextView;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail != null && userRole != null) {
            if ("Garage Owner".equals(userRole)) {
                Intent intent = new Intent(LoginActivity.this, GarageOwnerDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } else if ("Mechanic".equals(userRole)) {
                Intent intent = new Intent(LoginActivity.this, MechanicDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } else if ("Customer".equals(userRole)) {
                Intent intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } else if ("Admin".equals(userRole)) {
                Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        showPasswordTextView = findViewById(R.id.showPasswordTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        signInButton = findViewById(R.id.signInButton);
        createAccountTextView = findViewById(R.id.createAccountTextView);

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

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

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required");
                    emailEditText.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Enter a valid email address");
                    emailEditText.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required");
                    passwordEditText.requestFocus();
                    return;
                }

                DatabaseHelper databaseHelper = new DatabaseHelper(LoginActivity.this);
                if ("admin@gmail.com".equals(email) && "admin@123".equals(password)) {
                    getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                            .edit()
                            .putString("user_email", email)
                            .putString("user_role", "Admin")
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return;
                } else if (databaseHelper.loginGarageOwner(email, password)) {
                    getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                            .edit()
                            .putString("user_email", email)
                            .putString("user_role", "Garage Owner")
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, GarageOwnerDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (databaseHelper.loginMechanic(email, password)) {
                    getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                            .edit()
                            .putString("user_email", email)
                            .putString("user_role", "Mechanic")
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, MechanicDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (databaseHelper.loginCustomer(email, password)) {
                    getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                            .edit()
                            .putString("user_email", email)
                            .putString("user_role", "Customer")
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, CustomerDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials or account not found.", Toast.LENGTH_LONG).show();
                }
            }
        });

        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
            }
        });
    }
}