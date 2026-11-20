package com.example.autocare;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText resetEmailEditText, newPasswordEditText, confirmPasswordEditText;
    private Button resetButton, updatePasswordButton;
    private TextView backToLoginTextView;
    private android.widget.LinearLayout newPasswordContainer;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        resetEmailEditText = findViewById(R.id.resetEmailEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetButton = findViewById(R.id.resetButton);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);
        newPasswordContainer = findViewById(R.id.newPasswordContainer);

        resetButton.setOnClickListener(v -> {
            String email = resetEmailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                resetEmailEditText.setError("Enter a valid email address");
                resetEmailEditText.requestFocus();
                return;
            }

            if (databaseHelper.checkEmailExists(email)) {
                Toast.makeText(this, "Email verified!", Toast.LENGTH_SHORT).show();
                resetEmailEditText.setEnabled(false);
                resetButton.setVisibility(android.view.View.GONE);
                newPasswordContainer.setVisibility(android.view.View.VISIBLE);
                newPasswordEditText.requestFocus();
            } else {
                Toast.makeText(this, "Email is not registered!", Toast.LENGTH_SHORT).show();
            }
        });
        
        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            
            if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                newPasswordEditText.setError("Password must be at least 6 characters");
                newPasswordEditText.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords do not match");
                confirmPasswordEditText.requestFocus();
                return;
            }
            
            String email = resetEmailEditText.getText().toString().trim();
            boolean updated = databaseHelper.updatePasswordByEmail(email, newPassword);
            
            if (updated) {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show();
            }
        });

        backToLoginTextView.setOnClickListener(v -> finish());
    }
}