package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ConnectedMechanicsActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout mechanicsContainer;
    private TextView emptyStateTextView;

    private DatabaseHelper databaseHelper;
    private int garageOwnerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_mechanics);
        BottomNavHelper.setupBottomNav(this, R.id.navMechanics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail != null && "Garage Owner".equals(userRole)) {
            garageOwnerId = databaseHelper.getGarageOwnerId(userEmail);
        }

        if (garageOwnerId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        searchEditText = findViewById(R.id.searchEditText);
        mechanicsContainer = findViewById(R.id.mechanicsContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());
        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }
                loadMechanics(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navServices = findViewById(R.id.navServices);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectedMechanicsActivity.this, GarageOwnerDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navMechanics.setOnClickListener(v -> {
            // Already here
        });

        navCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectedMechanicsActivity.this, GarageOwnerCustomersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navServices.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectedMechanicsActivity.this, GarageOwnerServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectedMechanicsActivity.this, GarageOwnerProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMechanics(searchEditText.getText().toString().trim());
    }

    private void loadMechanics(String query) {
        mechanicsContainer.removeAllViews();
        boolean hasMechanics = false;

        Cursor cursor = databaseHelper.getConnectedMechanicsForGarage(garageOwnerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasMechanics = true;
                final int requestId = cursor.getInt(cursor.getColumnIndexOrThrow("request_id"));
                final int mechanicId = cursor.getInt(cursor.getColumnIndexOrThrow("mechanic_id"));
                final String mechanicName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                String joinedDate = cursor.getString(cursor.getColumnIndexOrThrow("request_date"));

                if (!query.isEmpty()) {
                    String combined = (mechanicName + " " + email + " " + phone + " " + city).toLowerCase(Locale.US);
                    if (!combined.contains(query.toLowerCase(Locale.US))) {
                        continue;
                    }
                }

                View cardView = LayoutInflater.from(this).inflate(R.layout.item_connected_mechanic_card, mechanicsContainer, false);
                TextView tvMechanicName = cardView.findViewById(R.id.tvMechanicName);
                TextView tvMechanicPhone = cardView.findViewById(R.id.tvMechanicPhone);
                TextView tvMechanicEmail = cardView.findViewById(R.id.tvMechanicEmail);
                TextView tvMechanicCity = cardView.findViewById(R.id.tvMechanicCity);
                TextView tvJoinedDate = cardView.findViewById(R.id.tvJoinedDate);
                Button btnViewActivity = cardView.findViewById(R.id.btnViewActivity);
                Button btnDisconnect = cardView.findViewById(R.id.btnDisconnect);

                tvMechanicName.setText(mechanicName);
                tvMechanicPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not specified");
                tvMechanicEmail.setText(email != null && !email.isEmpty() ? email : "No email");
                tvMechanicCity.setText("City: " + (city != null && !city.isEmpty() ? city : "Not specified"));
                tvJoinedDate.setText("Joined: " + joinedDate);

                btnViewActivity.setOnClickListener(v -> {
                    Intent intent = new Intent(ConnectedMechanicsActivity.this, MechanicActivityDetailsActivity.class);
                    intent.putExtra("mechanic_id", mechanicId);
                    startActivity(intent);
                });

                btnDisconnect.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Disconnect Mechanic")
                            .setMessage("Are you sure you want to disconnect " + mechanicName + "?\n\nNote: All historical customer, vehicle, and service records created by this mechanic will be preserved.")
                            .setPositiveButton("Disconnect", (dialog, which) -> {
                                boolean disconnected = databaseHelper.disconnectMechanic(requestId, garageOwnerId);
                                if (disconnected) {
                                    Toast.makeText(this, mechanicName + " disconnected.", Toast.LENGTH_SHORT).show();
                                    loadMechanics(searchEditText.getText().toString().trim());
                                } else {
                                    Toast.makeText(this, "Failed to disconnect mechanic.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                mechanicsContainer.addView(cardView);
            }
            cursor.close();
        }

        if (hasMechanics) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
