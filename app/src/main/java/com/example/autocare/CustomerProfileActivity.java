package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerProfileActivity extends AppCompatActivity {

    private ImageView backImageView;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etProfileAddress;
    private TextView tvStatVehicles, tvStatServices;
    private Button btnEditCustomerProfile, btnSaveCustomerProfile, btnCancelCustomerEdit, btnLogoutButton;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);
        BottomNavHelper.setupBottomNav(this, R.id.navProfile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail == null || !"Customer".equals(userRole)) {
            Intent intent = new Intent(CustomerProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileAddress = findViewById(R.id.etProfileAddress);

        tvStatVehicles = findViewById(R.id.tvStatVehicles);
        tvStatServices = findViewById(R.id.tvStatServices);
        
        btnEditCustomerProfile = findViewById(R.id.btnEditCustomerProfile);
        btnSaveCustomerProfile = findViewById(R.id.btnSaveCustomerProfile);
        btnCancelCustomerEdit = findViewById(R.id.btnCancelCustomerEdit);
        btnLogoutButton = findViewById(R.id.btnLogoutButton);
        Button btnDeleteCustomerAccount = findViewById(R.id.btnDeleteCustomerAccount);

        backImageView.setOnClickListener(v -> finish());

        btnEditCustomerProfile.setOnClickListener(v -> setEditingMode(true));
        btnCancelCustomerEdit.setOnClickListener(v -> {
            setEditingMode(false);
            loadProfileData();
        });
        btnSaveCustomerProfile.setOnClickListener(v -> saveProfileData());

        btnLogoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                        Intent intent = new Intent(CustomerProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnDeleteCustomerAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your customer account? All your linked vehicles and service history will also be deleted.")
                    .setPositiveButton("Delete Account", (dialog, which) -> {
                        if (customerId != -1) {
                            boolean deleted = databaseHelper.deleteCustomer(customerId);
                            if (deleted) {
                                Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                                Intent intent = new Intent(CustomerProfileActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navVehicles = findViewById(R.id.navVehicles);
        LinearLayout navWarranty = findViewById(R.id.navWarranty);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerProfileActivity.this, CustomerDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navVehicles != null) {
            navVehicles.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerProfileActivity.this, CustomerVehiclesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navWarranty != null) {
            navWarranty.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerProfileActivity.this, CustomerWarrantyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // Already here
            });
        }
    }

    private void setEditingMode(boolean isEditing) {
        etProfileName.setEnabled(isEditing);
        etProfilePhone.setEnabled(isEditing);
        etProfileAddress.setEnabled(isEditing);
        
        // Let's keep email read-only for now, since it is used as the key for logging in
        etProfileEmail.setEnabled(false); 

        if (isEditing) {
            btnEditCustomerProfile.setVisibility(View.GONE);
            btnSaveCustomerProfile.setVisibility(View.VISIBLE);
            btnCancelCustomerEdit.setVisibility(View.VISIBLE);
            etProfileName.setBackgroundResource(R.drawable.input_bg);
            etProfilePhone.setBackgroundResource(R.drawable.input_bg);
            etProfileAddress.setBackgroundResource(R.drawable.input_bg);
        } else {
            btnEditCustomerProfile.setVisibility(View.VISIBLE);
            btnSaveCustomerProfile.setVisibility(View.GONE);
            btnCancelCustomerEdit.setVisibility(View.GONE);
            etProfileName.setBackground(null);
            etProfilePhone.setBackground(null);
            etProfileAddress.setBackground(null);
        }
    }

    private void saveProfileData() {
        String name = etProfileName.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String address = etProfileAddress.getText().toString().trim();
        
        if (name.isEmpty()) {
            etProfileName.setError("Name cannot be empty");
            return;
        }

        if (phone.isEmpty()) {
            phone = null;
        }

        if (address.isEmpty()) {
            address = null;
        }

        boolean updated = databaseHelper.updateCustomerProfile(customerId, name, phone, address);
        
        if (updated) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            setEditingMode(false);
            loadProfileData();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        Cursor cursor = databaseHelper.getCustomerDetailsByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));

            etProfileName.setText(fullName);
            etProfileEmail.setText(email);
            etProfilePhone.setText(mobile != null && !mobile.isEmpty() ? mobile : "");
            etProfileAddress.setText(address != null && !address.isEmpty() ? address : "");

            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Session error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int vehicleCount = databaseHelper.getCustomerVehicleCount(customerId);
        int serviceCount = databaseHelper.getCustomerServiceCount(customerId);

        tvStatVehicles.setText(String.valueOf(vehicleCount));
        tvStatServices.setText(String.valueOf(serviceCount));
    }
}