package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerVehiclesListActivity extends AppCompatActivity {

    private ImageView backImageView;
    private LinearLayout vehiclesContainer;
    private TextView emptyStateTextView;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_vehicles_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        BottomNavHelper.setupBottomNav(this, R.id.navVehicles);

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail == null || !"Customer".equals(userRole)) {
            Intent intent = new Intent(CustomerVehiclesListActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        vehiclesContainer = findViewById(R.id.vehiclesContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());
        
        android.widget.EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadVehicles(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navVehicles = findViewById(R.id.navVehicles);
        LinearLayout navWarranty = findViewById(R.id.navWarranty);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerVehiclesListActivity.this, CustomerDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navVehicles != null) {
            navVehicles.setOnClickListener(v -> {
                // Already here
            });
        }

        if (navWarranty != null) {
            navWarranty.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerVehiclesListActivity.this, CustomerWarrantyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerVehiclesListActivity.this, CustomerProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicles("");
    }

    private void loadVehicles(String query) {
        Cursor cCustomer = databaseHelper.getCustomerDetailsByEmail(userEmail);
        if (cCustomer != null && cCustomer.moveToFirst()) {
            customerId = cCustomer.getInt(cCustomer.getColumnIndexOrThrow("id"));
            cCustomer.close();
        } else {
            if (cCustomer != null) cCustomer.close();
            Toast.makeText(this, "Session error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        vehiclesContainer.removeAllViews();
        boolean hasVehicles = false;

        Cursor cursor = databaseHelper.getCustomerVehicles(customerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                final int vehicleId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
                String year = cursor.getString(cursor.getColumnIndexOrThrow("year"));
                String color = cursor.getString(cursor.getColumnIndexOrThrow("color"));

                if (query != null && !query.isEmpty()) {
                    String combined = (brand + " " + model + " " + regNumber + " " + type).toLowerCase(java.util.Locale.US);
                    if (!combined.contains(query.toLowerCase(java.util.Locale.US))) {
                        continue;
                    }
                }

                hasVehicles = true;

                View vehicleView = LayoutInflater.from(this).inflate(R.layout.item_vehicle_card, vehiclesContainer, false);
                View vehicleCardRoot = vehicleView.findViewById(R.id.vehicleCardRoot);
                TextView tvVehicleModel = vehicleView.findViewById(R.id.tvVehicleModel);
                TextView tvVehicleRegBadge = vehicleView.findViewById(R.id.tvVehicleRegBadge);
                TextView tvVehicleYearChip = vehicleView.findViewById(R.id.tvVehicleYearChip);
                TextView tvVehicleColorChip = vehicleView.findViewById(R.id.tvVehicleColorChip);
                TextView tvVehicleStatus = vehicleView.findViewById(R.id.tvVehicleStatus);
                Button btnEditVehicle = vehicleView.findViewById(R.id.btnEditVehicle);
                Button btnDeleteVehicle = vehicleView.findViewById(R.id.btnDeleteVehicle);
                Button btnAddService = vehicleView.findViewById(R.id.btnAddService);
                Button btnViewHistory = vehicleView.findViewById(R.id.btnViewHistory);

                btnEditVehicle.setVisibility(View.GONE);
                btnDeleteVehicle.setVisibility(View.GONE);
                btnAddService.setVisibility(View.GONE);
                btnViewHistory.setText("View Service History ➔");

                // Check for car or bike based on type, adjust icon and text
                ImageView ivVehicleIcon = vehicleView.findViewById(R.id.ivVehicleIcon);
                if (ivVehicleIcon != null) {
                    ivVehicleIcon.setImageResource(R.drawable.ic_vehicles);
                }

                tvVehicleModel.setText(brand + " " + model);

                tvVehicleRegBadge.setText("Reg: " + regNumber);
                if (year != null && !year.isEmpty()) {
                    tvVehicleYearChip.setText("Year: " + year);
                    tvVehicleYearChip.setVisibility(View.VISIBLE);
                } else {
                    tvVehicleYearChip.setVisibility(View.GONE);
                }

                if (color != null && !color.isEmpty()) {
                    tvVehicleColorChip.setText("Color: " + color);
                    tvVehicleColorChip.setVisibility(View.VISIBLE);
                } else {
                    tvVehicleColorChip.setVisibility(View.GONE);
                }

                Cursor cServices = databaseHelper.getServicesByVehicleId(vehicleId);
                if (cServices != null && cServices.moveToFirst()) {
                    String lastDate = cServices.getString(cServices.getColumnIndexOrThrow("service_date"));
                    tvVehicleStatus.setText("Last Service: " + lastDate);
                    cServices.close();
                } else {
                    if (cServices != null) cServices.close();
                    tvVehicleStatus.setText("Last Service: None");
                }

                View.OnClickListener openHistoryListener = v -> {
                    Intent intent = new Intent(CustomerVehiclesListActivity.this, VehicleServicesActivity.class);
                    intent.putExtra("vehicle_id", vehicleId);
                    startActivity(intent);
                };

                btnViewHistory.setOnClickListener(openHistoryListener);
                vehicleCardRoot.setOnClickListener(openHistoryListener);

                vehiclesContainer.addView(vehicleView);
            }
            cursor.close();
        }

        if (hasVehicles) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}