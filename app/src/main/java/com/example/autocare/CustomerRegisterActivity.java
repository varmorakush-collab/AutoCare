package com.example.autocare;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerRegisterActivity extends AppCompatActivity {

    private ImageView backImageView;
    private EditText customerNameEditText, customerEmailEditText, customerPhoneEditText;
    private EditText customerPasswordEditText, customerConfirmPasswordEditText;
    private TextView showCustomerPasswordTextView, showCustomerConfirmPasswordTextView, signInLinkTextView;
    private Button createCustomerAccountButton;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        backImageView = findViewById(R.id.backImageView);
        customerNameEditText = findViewById(R.id.customerNameEditText);
        customerEmailEditText = findViewById(R.id.customerEmailEditText);
        customerPhoneEditText = findViewById(R.id.customerPhoneEditText);
        customerPasswordEditText = findViewById(R.id.customerPasswordEditText);
        customerConfirmPasswordEditText = findViewById(R.id.customerConfirmPasswordEditText);

        showCustomerPasswordTextView = findViewById(R.id.showCustomerPasswordTextView);
        showCustomerConfirmPasswordTextView = findViewById(R.id.showCustomerConfirmPasswordTextView);
        createCustomerAccountButton = findViewById(R.id.createCustomerAccountButton);
        signInLinkTextView = findViewById(R.id.signInLinkTextView);

        backImageView.setOnClickListener(v -> finish());

        showCustomerPasswordTextView.setOnClickListener(v -> {
            if (isPasswordVisible) {
                customerPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showCustomerPasswordTextView.setText("SHOW");
                isPasswordVisible = false;
            } else {
                customerPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showCustomerPasswordTextView.setText("HIDE");
                isPasswordVisible = true;
            }
            customerPasswordEditText.setSelection(customerPasswordEditText.getText().length());
        });

        showCustomerConfirmPasswordTextView.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                customerConfirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showCustomerConfirmPasswordTextView.setText("SHOW");
                isConfirmPasswordVisible = false;
            } else {
                customerConfirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showCustomerConfirmPasswordTextView.setText("HIDE");
                isConfirmPasswordVisible = true;
            }
            customerConfirmPasswordEditText.setSelection(customerConfirmPasswordEditText.getText().length());
        });

        signInLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerRegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        createCustomerAccountButton.setOnClickListener(v -> registerCustomer());
    }

    private void registerCustomer() {
        String name = customerNameEditText.getText().toString().trim();
        String email = customerEmailEditText.getText().toString().trim();
        String phone = customerPhoneEditText.getText().toString().trim();
        String password = customerPasswordEditText.getText().toString().trim();
        String confirmPassword = customerConfirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            customerNameEditText.setError("Full Name is required");
            customerNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            customerEmailEditText.setError("Valid Email Address is required");
            customerEmailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone) || !phone.matches("^[6-9]\\d{9}$")) {
            customerPhoneEditText.setError("Enter a valid 10-digit mobile number");
            customerPhoneEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            customerPasswordEditText.setError("Password must be at least 6 characters long");
            customerPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            customerConfirmPasswordEditText.setError("Passwords do not match");
            customerConfirmPasswordEditText.requestFocus();
            return;
        }

        if (databaseHelper.checkEmailExists(email)) {
            Toast.makeText(this, "An account with this email already exists as Garage Owner or Mechanic.", Toast.LENGTH_LONG).show();
            return;
        }

        Cursor cByEmail = databaseHelper.getCustomerByEmail(email);
        Cursor cByMobile = databaseHelper.getCustomerByMobile(phone);

        boolean hasEmailRecord = (cByEmail != null && cByEmail.moveToFirst());
        boolean hasMobileRecord = (cByMobile != null && cByMobile.moveToFirst());

        if (hasEmailRecord && hasMobileRecord) {
            int emailCustomerId = cByEmail.getInt(cByEmail.getColumnIndexOrThrow("id"));
            int mobileCustomerId = cByMobile.getInt(cByMobile.getColumnIndexOrThrow("id"));

            if (emailCustomerId != mobileCustomerId) {
                customerPhoneEditText.setError("Mobile number and Email belong to different customer records.");
                customerPhoneEditText.requestFocus();
                Toast.makeText(this, "The provided Mobile Number and Email belong to different records added by mechanics. Please enter matching details.", Toast.LENGTH_LONG).show();
                cByEmail.close();
                cByMobile.close();
                return;
            }

            int customerId = emailCustomerId;
            int pwdIndex = cByEmail.getColumnIndex("password_hash");
            String existingPwdHash = (pwdIndex != -1 && !cByEmail.isNull(pwdIndex)) ? cByEmail.getString(pwdIndex) : "";
            cByEmail.close();
            cByMobile.close();

            if (existingPwdHash != null && !existingPwdHash.trim().isEmpty()) {
                Toast.makeText(this, "An account with this email and mobile is already registered. Please sign in.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CustomerRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            boolean updated = databaseHelper.registerCustomerAccount(customerId, name, phone, password);
            if (updated) {
                Toast.makeText(this, "Customer Account Registered Successfully!", Toast.LENGTH_SHORT).show();

                getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                        .edit()
                        .putString("user_email", email)
                        .putString("user_role", "Customer")
                        .apply();

                Intent intent = new Intent(CustomerRegisterActivity.this, CustomerDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to register account. Please try again.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (hasEmailRecord) {
            String storedMobile = cByEmail.getString(cByEmail.getColumnIndexOrThrow("mobile"));
            cByEmail.close();
            if (cByMobile != null) cByMobile.close();

            customerPhoneEditText.setError("Mobile number does not match the details added by your mechanic.");
            customerPhoneEditText.requestFocus();
            Toast.makeText(this, "Mobile number does not match the record added by your mechanic. Please use the mobile number (" + storedMobile + ") provided to your mechanic.", Toast.LENGTH_LONG).show();
            return;
        }

        if (hasMobileRecord) {
            String storedEmail = cByMobile.isNull(cByMobile.getColumnIndexOrThrow("email")) ? "" : cByMobile.getString(cByMobile.getColumnIndexOrThrow("email"));
            cByMobile.close();
            if (cByEmail != null) cByEmail.close();

            customerEmailEditText.setError("Email address does not match the details added by your mechanic.");
            customerEmailEditText.requestFocus();
            Toast.makeText(this, "Email address does not match the record added by your mechanic (" + storedEmail + "). Please use the email provided to your mechanic.", Toast.LENGTH_LONG).show();
            return;
        }

        if (cByEmail != null) cByEmail.close();
        if (cByMobile != null) cByMobile.close();

        // Direct registration for brand new customer
        boolean inserted = databaseHelper.registerNewCustomerAccountDirect(name, phone, email, password);
        if (inserted) {
            Toast.makeText(this, "Customer Account Registered Successfully!", Toast.LENGTH_SHORT).show();

            getSharedPreferences("AutoCarePrefs", MODE_PRIVATE)
                    .edit()
                    .putString("user_email", email)
                    .putString("user_role", "Customer")
                    .apply();

            Intent intent = new Intent(CustomerRegisterActivity.this, CustomerDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to register account. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}