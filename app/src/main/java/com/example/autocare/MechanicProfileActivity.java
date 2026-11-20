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

public class MechanicProfileActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView mechanicNameTextView, mechanicCityTextView, tvMechanicEmail;
    private EditText etMechanicName, etMechanicPhone, etMechanicCity;
    private TextView myCustomersCountTextView, myVehiclesCountTextView, mechanicCompletedServicesTextView;
    private Button btnEditProfile, btnSaveProfile, btnCancelEdit, LogoutButton;
    private LinearLayout btnNavCustomers, btnNavServices;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int mechanicId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_profile);
        BottomNavHelper.setupBottomNav(this, R.id.navProfile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);

        if (userEmail != null) {
            mechanicId = databaseHelper.getMechanicId(userEmail);
        }

        backImageView = findViewById(R.id.backImageView);
        mechanicNameTextView = findViewById(R.id.mechanicNameTextView);
        mechanicCityTextView = findViewById(R.id.mechanicCityTextView);
        
        myCustomersCountTextView = findViewById(R.id.myCustomersCountTextView);
        myVehiclesCountTextView = findViewById(R.id.myVehiclesCountTextView);
        mechanicCompletedServicesTextView = findViewById(R.id.mechanicCompletedServicesTextView);
        
        etMechanicName = findViewById(R.id.etMechanicName);
        tvMechanicEmail = findViewById(R.id.tvMechanicEmail);
        etMechanicPhone = findViewById(R.id.etMechanicPhone);
        etMechanicCity = findViewById(R.id.etMechanicCity);
        
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnNavCustomers = findViewById(R.id.btnNavCustomers);
        btnNavServices = findViewById(R.id.btnNavServices);
        LogoutButton = findViewById(R.id.btnLogoutButton);

        backImageView.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> setEditingMode(true));
        btnCancelEdit.setOnClickListener(v -> {
            setEditingMode(false);
            loadProfileData();
        });

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        btnNavCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicProfileActivity.this, MyCustomersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnNavServices.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicProfileActivity.this, MyServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCustomersTab = findViewById(R.id.navCustomers);
        LinearLayout navServicesTab = findViewById(R.id.navServices);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(MechanicProfileActivity.this, MechanicDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navCustomersTab != null) {
            navCustomersTab.setOnClickListener(v -> {
                Intent intent = new Intent(MechanicProfileActivity.this, MyCustomersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navServicesTab != null) {
            navServicesTab.setOnClickListener(v -> {
                Intent intent = new Intent(MechanicProfileActivity.this, MyServicesActivity.class);
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

        Button btnDeleteAccountButton = findViewById(R.id.btnDeleteAccountButton);

        LogoutButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(MechanicProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show());

        btnDeleteAccountButton.setOnClickListener(v -> {
            int linkedCustomers = databaseHelper.getMechanicLinkedCustomersCount(mechanicId);
            if (linkedCustomers > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Cannot Delete Account")
                        .setMessage("You have " + linkedCustomers + " linked customer(s). Please remove your linked customers first before deleting your mechanic account.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your mechanic account?")
                    .setPositiveButton("Delete Account", (dialog, which) -> {
                        if (mechanicId != -1) {
                            boolean deleted = databaseHelper.deleteMechanic(mechanicId);
                            if (deleted) {
                                Toast.makeText(this, "Your account has been deleted.", Toast.LENGTH_SHORT).show();
                                getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                                Intent intent = new Intent(MechanicProfileActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to delete account. Please remove linked customers first.", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEditingMode(false);
        loadProfileData();
    }

    private void setEditingMode(boolean enabled) {
        etMechanicName.setEnabled(enabled);
        etMechanicName.setFocusable(enabled);
        etMechanicName.setFocusableInTouchMode(enabled);
        etMechanicName.setBackgroundResource(enabled ? R.drawable.input_bg : android.R.color.transparent);

        etMechanicPhone.setEnabled(enabled);
        etMechanicPhone.setFocusable(enabled);
        etMechanicPhone.setFocusableInTouchMode(enabled);
        etMechanicPhone.setBackgroundResource(enabled ? R.drawable.input_bg : android.R.color.transparent);

        etMechanicCity.setEnabled(enabled);
        etMechanicCity.setFocusable(enabled);
        etMechanicCity.setFocusableInTouchMode(enabled);
        etMechanicCity.setBackgroundResource(enabled ? R.drawable.input_bg : android.R.color.transparent);

        if (enabled) {
            btnEditProfile.setVisibility(View.GONE);
            btnSaveProfile.setVisibility(View.VISIBLE);
            btnCancelEdit.setVisibility(View.VISIBLE);
            etMechanicName.requestFocus();
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
            btnSaveProfile.setVisibility(View.GONE);
            btnCancelEdit.setVisibility(View.GONE);
        }
    }

    private void loadProfileData() {
        if (userEmail == null || mechanicId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery("SELECT full_name, email, phone, city FROM mechanics WHERE id = ?", new String[]{String.valueOf(mechanicId)});
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(0);
            String email = cursor.getString(1);
            String phone = cursor.getString(2);
            String city = cursor.getString(3);

            mechanicNameTextView.setText(fullName);
            etMechanicName.setText(fullName);
            tvMechanicEmail.setText(email);
            etMechanicPhone.setText(phone != null && !phone.equals("Not specified") ? phone : "");
            etMechanicCity.setText(city != null && !city.equals("Not specified") ? city : "");
            mechanicCityTextView.setText(city != null && !city.isEmpty() ? city : "Location not set");

            cursor.close();
        }

        // Load stats
        int customerCount = databaseHelper.getMechanicCustomerCount(mechanicId);
        int vehicleCount = databaseHelper.getMechanicVehicleCount(mechanicId);
        int serviceCount = databaseHelper.getMechanicServiceCount(mechanicId);

        myCustomersCountTextView.setText(String.valueOf(customerCount));
        myVehiclesCountTextView.setText(String.valueOf(vehicleCount));
        mechanicCompletedServicesTextView.setText(String.valueOf(serviceCount));
    }

    private void saveProfileChanges() {
        String newName = etMechanicName.getText() != null ? etMechanicName.getText().toString().trim() : "";
        String newPhone = etMechanicPhone.getText() != null ? etMechanicPhone.getText().toString().trim() : "";
        String newCity = etMechanicCity.getText() != null ? etMechanicCity.getText().toString().trim() : "";

        if (newName.isEmpty()) {
            Toast.makeText(this, "Full Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = databaseHelper.updateMechanicProfile(mechanicId, newName, newPhone, newCity);
        if (updated) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            setEditingMode(false);
            loadProfileData();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}
