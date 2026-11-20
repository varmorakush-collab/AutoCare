package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class GarageOwnerServicesActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout servicesContainer;
    private TextView emptyServicesTextView;
    private Button chipAll, chipPending, chipInProgress, chipCompleted;

    private DatabaseHelper databaseHelper;
    private int garageId = -1;
    private String selectedStatusFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_owner_services);
        BottomNavHelper.setupBottomNav(this, R.id.navServices);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail != null && "Garage Owner".equals(userRole)) {
            garageId = databaseHelper.getGarageOwnerId(userEmail);
        }

        if (garageId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        searchEditText = findViewById(R.id.searchEditText);
        servicesContainer = findViewById(R.id.servicesContainer);
        emptyServicesTextView = findViewById(R.id.emptyServicesTextView);
        
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipInProgress = findViewById(R.id.chipInProgress);
        chipCompleted = findViewById(R.id.chipCompleted);

        backImageView.setOnClickListener(v -> finish());
        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));

        setupFilterChips();

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
                loadServices(s.toString().trim());
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
            Intent intent = new Intent(GarageOwnerServicesActivity.this, GarageOwnerDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navMechanics.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerServicesActivity.this, ConnectedMechanicsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerServicesActivity.this, GarageOwnerCustomersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navServices.setOnClickListener(v -> {
            // Already here
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerServicesActivity.this, GarageOwnerProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setupFilterChips() {
        View.OnClickListener chipListener = v -> {
            resetChipStyles();
            Button b = (Button) v;
            b.setBackgroundResource(R.drawable.chip_selected_bg);
            b.setTextColor(Color.WHITE);
            selectedStatusFilter = b.getText().toString();
            if ("All Services".equals(selectedStatusFilter)) {
                selectedStatusFilter = "All";
            }
            loadServices(searchEditText.getText().toString().trim());
        };

        chipAll.setOnClickListener(chipListener);
        chipPending.setOnClickListener(chipListener);
        chipInProgress.setOnClickListener(chipListener);
        chipCompleted.setOnClickListener(chipListener);
    }

    private void resetChipStyles() {
        Button[] chips = {chipAll, chipPending, chipInProgress, chipCompleted};
        for (Button c : chips) {
            c.setBackgroundResource(R.drawable.chip_unselected_bg);
            c.setTextColor(Color.parseColor("#1E293B"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices(searchEditText.getText().toString().trim());
    }

    private void loadServices(String query) {
        servicesContainer.removeAllViews();
        servicesContainer.addView(emptyServicesTextView);
        boolean hasServices = false;

        Cursor cursor = databaseHelper.getServicesByGarageId(garageId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                final int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                final int vehicleId = cursor.getInt(cursor.getColumnIndexOrThrow("vehicle_id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("service_type"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
                String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String mechanicName = cursor.getString(cursor.getColumnIndexOrThrow("mechanic_name"));

                // Apply search query filter if any
                if (!query.isEmpty()) {
                    String combined = (mechanicName + " " + customerName + " " + brand + " " + model + " " + regNumber + " " + type + " " + status).toLowerCase(Locale.US);
                    if (!combined.contains(query.toLowerCase(Locale.US))) {
                        continue;
                    }
                }

                // Apply status filter
                if (!"All".equals(selectedStatusFilter)) {
                    if (!selectedStatusFilter.equalsIgnoreCase(status)) {
                        continue;
                    }
                }

                hasServices = true;

                View serviceView = LayoutInflater.from(this).inflate(R.layout.item_service_history, servicesContainer, false);
                TextView tvHistDate = serviceView.findViewById(R.id.tvHistDate);
                TextView tvHistStatus = serviceView.findViewById(R.id.tvHistStatus);
                TextView tvHistType = serviceView.findViewById(R.id.tvHistType);
                TextView tvHistCost = serviceView.findViewById(R.id.tvHistCost);
                TextView tvHistVehicle = serviceView.findViewById(R.id.tvHistVehicle);
                TextView tvHistCustomer = serviceView.findViewById(R.id.tvHistCustomer);
                Button btnViewService = serviceView.findViewById(R.id.btnViewService);

                tvHistType.setText(type);
                tvHistStatus.setText(status);

                if ("Completed".equalsIgnoreCase(status)) {
                    tvHistStatus.setBackgroundColor(0xFFDCFCE7);
                    tvHistStatus.setTextColor(0xFF15803D);
                } else if ("Pending".equalsIgnoreCase(status)) {
                    tvHistStatus.setBackgroundColor(0xFFFEF3C7);
                    tvHistStatus.setTextColor(0xFFD97706);
                } else {
                    tvHistStatus.setBackgroundColor(0xFFDBEAFE);
                    tvHistStatus.setTextColor(0xFF1D4ED8);
                }

                tvHistVehicle.setText(brand + " " + model + " • " + regNumber);
                tvHistCustomer.setText("Customer: " + customerName + " | Mechanic: " + mechanicName);
                tvHistDate.setText(date);
                tvHistCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                View.OnClickListener openDetailsListener = v -> {
                    Intent intent = new Intent(GarageOwnerServicesActivity.this, ServiceDetailsActivity.class);
                    intent.putExtra("vehicle_id", vehicleId);
                    intent.putExtra("service_id", serviceId);
                    startActivity(intent);
                };

                btnViewService.setOnClickListener(openDetailsListener);
                serviceView.setOnClickListener(openDetailsListener);

                servicesContainer.addView(serviceView);
            }
            cursor.close();
        }

        if (hasServices) {
            emptyServicesTextView.setVisibility(View.GONE);
        } else {
            emptyServicesTextView.setVisibility(View.VISIBLE);
        }
    }
}
