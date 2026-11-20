package com.example.autocare;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditCustomerActivity extends AppCompatActivity {

    private ImageView backImageView;
    private EditText customerNameEditText, customerMobileEditText, customerEmailEditText, customerAddressEditText;
    private Button updateCustomerButton;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail != null && "Mechanic".equals(userRole)) {
            loggedInMechanicId = databaseHelper.getMechanicId(userEmail);
        }

        customerId = getIntent().getIntExtra("customer_id", -1);

        if (loggedInMechanicId == -1 || customerId == -1) {
            Toast.makeText(this, "Session or customer data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to edit customers.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        customerNameEditText = findViewById(R.id.customerNameEditText);
        customerMobileEditText = findViewById(R.id.customerMobileEditText);
        customerEmailEditText = findViewById(R.id.customerEmailEditText);
        customerAddressEditText = findViewById(R.id.customerAddressEditText);
        updateCustomerButton = findViewById(R.id.updateCustomerButton);

        loadCustomerData();

        backImageView.setOnClickListener(v -> finish());

        updateCustomerButton.setOnClickListener(v -> updateCustomer());
    }

    private void loadCustomerData() {
        Cursor cursor = databaseHelper.getCustomerById(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

            customerNameEditText.setText(name);
            customerMobileEditText.setText(mobile);
            customerEmailEditText.setText(email != null ? email : "");
            customerAddressEditText.setText(address != null ? address : "");
            cursor.close();
        } else {
            Toast.makeText(this, "Customer not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void updateCustomer() {
        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to edit customers.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String name = customerNameEditText.getText().toString().trim();
        String mobile = customerMobileEditText.getText().toString().trim();
        String email = customerEmailEditText.getText().toString().trim();
        String address = customerAddressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            customerNameEditText.setError("Full Name is required");
            customerNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(mobile) || !mobile.matches("^[6-9]\\d{9}$")) {
            customerMobileEditText.setError("Enter a valid 10-digit mobile number");
            customerMobileEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            customerEmailEditText.setError("Enter a valid email address");
            customerEmailEditText.requestFocus();
            return;
        }

        boolean isUpdated = databaseHelper.updateCustomer(customerId, name, mobile, email, address);

        if (isUpdated) {
            Toast.makeText(this, "Customer updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update customer. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
