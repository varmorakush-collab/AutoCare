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

public class GarageOwnerProfileActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvGarageHeaderName, tvGarageHeaderAddress, tvGarageEmail;
    private EditText etGarageName, etOwnerName, etGaragePhone, etGarageAddress;
    private Button btnEditGarageProfile, btnSaveGarageProfile, btnCancelGarageEdit, btnLogoutButton;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int garageOwnerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_owner_profile);
        BottomNavHelper.setupBottomNav(this, R.id.navProfile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);

        if (userEmail != null) {
            garageOwnerId = databaseHelper.getGarageOwnerId(userEmail);
        }

        backImageView = findViewById(R.id.backImageView);
        tvGarageHeaderName = findViewById(R.id.tvGarageHeaderName);
        tvGarageHeaderAddress = findViewById(R.id.tvGarageHeaderAddress);
        tvGarageEmail = findViewById(R.id.tvGarageEmail);

        etGarageName = findViewById(R.id.etGarageName);
        etOwnerName = findViewById(R.id.etOwnerName);
        etGaragePhone = findViewById(R.id.etGaragePhone);
        etGarageAddress = findViewById(R.id.etGarageAddress);

        btnEditGarageProfile = findViewById(R.id.btnEditGarageProfile);
        btnSaveGarageProfile = findViewById(R.id.btnSaveGarageProfile);
        btnCancelGarageEdit = findViewById(R.id.btnCancelGarageEdit);
        btnLogoutButton = findViewById(R.id.btnLogoutButton);

        backImageView.setOnClickListener(v -> finish());

        btnEditGarageProfile.setOnClickListener(v -> setEditingMode(true));
        btnCancelGarageEdit.setOnClickListener(v -> {
            setEditingMode(false);
            loadProfileData();
        });

        btnSaveGarageProfile.setOnClickListener(v -> saveProfileChanges());

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navServices = findViewById(R.id.navServices);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerProfileActivity.this, GarageOwnerDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navMechanics.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerProfileActivity.this, ConnectedMechanicsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerProfileActivity.this, GarageOwnerCustomersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navServices.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerProfileActivity.this, GarageOwnerServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            // Already here
        });

        Button btnDeleteAccountButton = findViewById(R.id.btnDeleteAccountButton);

        btnLogoutButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(GarageOwnerProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show());

        btnDeleteAccountButton.setOnClickListener(v -> {
            int mechanicCount = databaseHelper.getGarageOwnerConnectedMechanicsCount(garageOwnerId);
            if (mechanicCount > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Cannot Delete Account")
                        .setMessage("Your garage has " + mechanicCount + " connected mechanic(s). Please disconnect all mechanics from your garage before deleting your account.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your garage owner account?")
                    .setPositiveButton("Delete Account", (dialog, which) -> {
                        if (garageOwnerId != -1) {
                            boolean deleted = databaseHelper.deleteGarageOwner(garageOwnerId);
                            if (deleted) {
                                Toast.makeText(this, "Your account has been deleted.", Toast.LENGTH_SHORT).show();
                                getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                                Intent intent = new Intent(GarageOwnerProfileActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to delete account. Please disconnect all mechanics first.", Toast.LENGTH_LONG).show();
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
        etGarageName.setEnabled(enabled);
        etGarageName.setFocusable(enabled);
        etGarageName.setFocusableInTouchMode(enabled);

        etOwnerName.setEnabled(enabled);
        etOwnerName.setFocusable(enabled);
        etOwnerName.setFocusableInTouchMode(enabled);

        etGaragePhone.setEnabled(enabled);
        etGaragePhone.setFocusable(enabled);
        etGaragePhone.setFocusableInTouchMode(enabled);

        etGarageAddress.setEnabled(enabled);
        etGarageAddress.setFocusable(enabled);
        etGarageAddress.setFocusableInTouchMode(enabled);

        if (enabled) {
            btnEditGarageProfile.setVisibility(View.GONE);
            btnSaveGarageProfile.setVisibility(View.VISIBLE);
            btnCancelGarageEdit.setVisibility(View.VISIBLE);
            etGarageName.requestFocus();
        } else {
            btnEditGarageProfile.setVisibility(View.VISIBLE);
            btnSaveGarageProfile.setVisibility(View.GONE);
            btnCancelGarageEdit.setVisibility(View.GONE);
        }
    }

    private void loadProfileData() {
        if (userEmail == null || garageOwnerId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = databaseHelper.getGarageOwnerById(garageOwnerId);
        if (cursor != null && cursor.moveToFirst()) {
            String ownerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String garageName = cursor.getString(cursor.getColumnIndexOrThrow("garage_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));

            tvGarageHeaderName.setText(garageName);
            etGarageName.setText(garageName);
            etOwnerName.setText(ownerName);
            tvGarageEmail.setText(email);
            etGaragePhone.setText(phone != null ? phone : "");
            etGarageAddress.setText(address);
            tvGarageHeaderAddress.setText(!address.isEmpty() ? address : "Location not set");

            cursor.close();
        }
    }

    private void saveProfileChanges() {
        String garageName = etGarageName.getText() != null ? etGarageName.getText().toString().trim() : "";
        String ownerName = etOwnerName.getText() != null ? etOwnerName.getText().toString().trim() : "";
        String phone = etGaragePhone.getText() != null ? etGaragePhone.getText().toString().trim() : "";
        String address = etGarageAddress.getText() != null ? etGarageAddress.getText().toString().trim() : "";

        if (garageName.isEmpty() || ownerName.isEmpty()) {
            Toast.makeText(this, "Garage Name and Owner Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = databaseHelper.updateGarageOwnerProfile(garageOwnerId, ownerName, garageName, phone, address);
        if (updated) {
            Toast.makeText(this, "Garage profile updated successfully!", Toast.LENGTH_SHORT).show();
            setEditingMode(false);
            loadProfileData();
        } else {
            Toast.makeText(this, "Failed to update garage profile", Toast.LENGTH_SHORT).show();
        }
    }
}
