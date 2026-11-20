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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AddCustomerActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvHeaderTitle;
    private EditText customerNameEditText, customerMobileEditText, customerEmailEditText, customerAddressEditText;
    private Button saveCustomerButton, deleteCustomerButton;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

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

        if (loggedInMechanicId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to manage customers.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        customerId = getIntent().getIntExtra("customer_id", -1);

        backImageView = findViewById(R.id.backImageView);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        customerNameEditText = findViewById(R.id.customerNameEditText);
        customerMobileEditText = findViewById(R.id.customerMobileEditText);
        customerEmailEditText = findViewById(R.id.customerEmailEditText);
        customerAddressEditText = findViewById(R.id.customerAddressEditText);
        saveCustomerButton = findViewById(R.id.saveCustomerButton);
        deleteCustomerButton = findViewById(R.id.deleteCustomerButton);

        if (customerId != -1) {
            tvHeaderTitle.setText("Edit Customer");
            saveCustomerButton.setText("Update Customer Details");
            deleteCustomerButton.setVisibility(View.VISIBLE);
            loadCustomerData();
        } else {
            tvHeaderTitle.setText("Add Customer");
            saveCustomerButton.setText("Save Customer");
            deleteCustomerButton.setVisibility(View.GONE);
        }

        backImageView.setOnClickListener(v -> finish());

        saveCustomerButton.setOnClickListener(v -> saveOrUpdateCustomer());

        deleteCustomerButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Are you sure you want to delete this customer? Note: Customers with linked vehicles cannot be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteCustomer(customerId);
                    if (deleted) {
                        Toast.makeText(this, "Customer deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Cannot delete customer. They have linked vehicles or history.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void loadCustomerData() {
        Cursor cursor = databaseHelper.getCustomerById(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));

            customerNameEditText.setText(name);
            customerMobileEditText.setText(mobile);
            customerEmailEditText.setText(email);
            customerAddressEditText.setText(address);
            cursor.close();
        } else {
            Toast.makeText(this, "Customer not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void saveOrUpdateCustomer() {
        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to manage customers.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String name = customerNameEditText.getText() != null ? customerNameEditText.getText().toString().trim() : "";
        String mobile = customerMobileEditText.getText() != null ? customerMobileEditText.getText().toString().trim() : "";
        String email = customerEmailEditText.getText() != null ? customerEmailEditText.getText().toString().trim() : "";
        String address = customerAddressEditText.getText() != null ? customerAddressEditText.getText().toString().trim() : "";

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

        if (customerId == -1 && databaseHelper.checkCustomerExists(loggedInMechanicId, email, mobile)) {
            Toast.makeText(this, "A customer with this email or mobile already exists in your list.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean success;
        if (customerId != -1) {
            success = databaseHelper.updateCustomer(customerId, name, mobile, email, address);
        } else {
            success = databaseHelper.addCustomer(loggedInMechanicId, name, mobile, email, address);
        }

        if (success) {
            Toast.makeText(this, customerId != -1 ? "Customer updated successfully!" : "Customer added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save customer. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
