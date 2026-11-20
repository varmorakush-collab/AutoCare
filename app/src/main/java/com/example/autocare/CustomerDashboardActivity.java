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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class CustomerDashboardActivity extends AppCompatActivity {

    private TextView customerWelcomeTextView;
    private TextView customerVehiclesCountTextView, customerServicesCountTextView, customerPendingServicesTextView;
    private View btnNavVehicles, btnNavServiceHistory, btnNavWarranty, btnNavProfile;
    private LinearLayout customerVehiclesContainer, recentServicesContainer;
    private TextView emptyVehiclesTextView, emptyServicesTextView;
    private View logoutImageView;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        BottomNavHelper.setupBottomNav(this, R.id.navHome);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail == null || !"Customer".equals(userRole)) {
            Intent intent = new Intent(CustomerDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        customerWelcomeTextView = findViewById(R.id.customerWelcomeTextView);
        customerVehiclesCountTextView = findViewById(R.id.customerVehiclesCountTextView);
        customerServicesCountTextView = findViewById(R.id.customerServicesCountTextView);
        customerPendingServicesTextView = findViewById(R.id.customerPendingServicesTextView);

        btnNavVehicles = findViewById(R.id.btnNavVehicles);
        btnNavServiceHistory = findViewById(R.id.btnNavServiceHistory);
        btnNavWarranty = findViewById(R.id.btnNavWarranty);
        btnNavProfile = findViewById(R.id.btnNavProfile);

        customerVehiclesContainer = findViewById(R.id.customerVehiclesContainer);
        recentServicesContainer = findViewById(R.id.recentServicesContainer);
        emptyVehiclesTextView = findViewById(R.id.emptyVehiclesTextView);
        emptyServicesTextView = findViewById(R.id.emptyServicesTextView);
        logoutImageView = findViewById(R.id.logoutImageView);

        logoutImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                        Intent intent = new Intent(CustomerDashboardActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnNavVehicles.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CustomerVehiclesListActivity.class);
            startActivity(intent);
        });

        btnNavServiceHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CustomerVehiclesListActivity.class);
            startActivity(intent);
        });

        btnNavWarranty.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CustomerWarrantyActivity.class);
            startActivity(intent);
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CustomerProfileActivity.class);
            startActivity(intent);
        });

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navVehicles = findViewById(R.id.navVehicles);
        LinearLayout navWarranty = findViewById(R.id.navWarranty);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on home
            });
        }

        if (navVehicles != null) {
            navVehicles.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerDashboardActivity.this, CustomerVehiclesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navWarranty != null) {
            navWarranty.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerDashboardActivity.this, CustomerWarrantyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerDashboardActivity.this, CustomerProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomerData();
    }

    private void loadCustomerData() {
        Cursor cursor = databaseHelper.getCustomerDetailsByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            // Try to extract first name
            String firstName = fullName;
            if (fullName != null && fullName.contains(" ")) {
                firstName = fullName.substring(0, fullName.indexOf(" "));
            }
            customerWelcomeTextView.setText("Hello, " + firstName + "! 👋");
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Customer profile error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int vehicleCount = databaseHelper.getCustomerVehicleCount(customerId);
        int serviceCount = databaseHelper.getCustomerServiceCount(customerId);
        int pendingCount = databaseHelper.getCustomerPendingServiceCount(customerId);

        customerVehiclesCountTextView.setText(String.valueOf(vehicleCount));
        customerServicesCountTextView.setText(String.valueOf(serviceCount));
        customerPendingServicesTextView.setText(String.valueOf(pendingCount));

        loadCustomerVehicles();
        loadRecentServices();
    }

    private void loadCustomerVehicles() {
        customerVehiclesContainer.removeAllViews();
        boolean hasVehicles = false;

        Cursor cursor = databaseHelper.getCustomerVehicles(customerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasVehicles = true;
                final int vehicleId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
                String year = cursor.getString(cursor.getColumnIndexOrThrow("year"));
                String color = cursor.getString(cursor.getColumnIndexOrThrow("color"));

                View vehicleView = LayoutInflater.from(this).inflate(R.layout.item_vehicle_card, customerVehiclesContainer, false);
                View vehicleCardRoot = vehicleView.findViewById(R.id.vehicleCardRoot);
                ImageView ivVehicleIcon = vehicleView.findViewById(R.id.ivVehicleIcon);
                TextView tvVehicleModel = vehicleView.findViewById(R.id.tvVehicleModel);
                TextView tvVehicleStatus = vehicleView.findViewById(R.id.tvVehicleStatus);
                TextView tvVehicleRegBadge = vehicleView.findViewById(R.id.tvVehicleRegBadge);
                TextView tvVehicleYearChip = vehicleView.findViewById(R.id.tvVehicleYearChip);
                TextView tvVehicleColorChip = vehicleView.findViewById(R.id.tvVehicleColorChip);
                Button btnViewHistory = vehicleView.findViewById(R.id.btnViewHistory);

                ivVehicleIcon.setImageResource(R.drawable.ic_vehicles);

                tvVehicleModel.setText(brand + " " + model);
                tvVehicleStatus.setText("Active"); // Or status from DB if you have it
                tvVehicleRegBadge.setText(regNumber);
                
                if (year != null && !year.isEmpty()) {
                    tvVehicleYearChip.setVisibility(View.VISIBLE);
                    tvVehicleYearChip.setText(year);
                }
                
                if (color != null && !color.isEmpty()) {
                    tvVehicleColorChip.setVisibility(View.VISIBLE);
                    tvVehicleColorChip.setText(color);
                }

                View.OnClickListener openHistoryListener = v -> {
                    Intent intent = new Intent(CustomerDashboardActivity.this, CustomerVehiclesListActivity.class);
                    intent.putExtra("vehicle_id", vehicleId);
                    startActivity(intent);
                };

                btnViewHistory.setOnClickListener(openHistoryListener);
                vehicleCardRoot.setOnClickListener(openHistoryListener);

                customerVehiclesContainer.addView(vehicleView);
            }
            cursor.close();
        }

        if (hasVehicles) {
            emptyVehiclesTextView.setVisibility(View.GONE);
        } else {
            emptyVehiclesTextView.setVisibility(View.VISIBLE);
        }
    }

    private void loadRecentServices() {
        recentServicesContainer.removeAllViews();
        boolean hasServices = false;

        Cursor cursor = databaseHelper.getCustomerServices(customerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasServices = true;
                final int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("service_type"));
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));

                View serviceView = LayoutInflater.from(this).inflate(R.layout.item_service_history, recentServicesContainer, false);
                TextView tvHistDate = serviceView.findViewById(R.id.tvHistDate);
                TextView tvHistStatus = serviceView.findViewById(R.id.tvHistStatus);
                TextView tvHistType = serviceView.findViewById(R.id.tvHistType);
                TextView tvHistCost = serviceView.findViewById(R.id.tvHistCost);
                TextView tvHistVehicle = serviceView.findViewById(R.id.tvHistVehicle);
                TextView tvHistCustomer = serviceView.findViewById(R.id.tvHistCustomer);
                Button btnViewService = serviceView.findViewById(R.id.btnViewService);

                tvHistDate.setText(date);
                tvHistStatus.setText(status);
                
                // Color the status badge
                if ("Completed".equalsIgnoreCase(status)) {
                    tvHistStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                    tvHistStatus.setTextColor(android.graphics.Color.parseColor("#15803D"));
                } else if ("Pending".equalsIgnoreCase(status)) {
                    tvHistStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7")));
                    tvHistStatus.setTextColor(android.graphics.Color.parseColor("#D97706"));
                } else {
                    tvHistStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9")));
                    tvHistStatus.setTextColor(android.graphics.Color.parseColor("#0F172A"));
                }

                tvHistType.setText(type);
                tvHistVehicle.setText(brand + " " + model);
                tvHistCustomer.setVisibility(View.GONE); // Hide customer info on customer dashboard
                tvHistCost.setText(String.format(Locale.US, "Total: ₹ %.2f", totalCost));

                btnViewService.setOnClickListener(v -> {
                    Intent intent = new Intent(CustomerDashboardActivity.this, CustomerServiceDetailsActivity.class);
                    intent.putExtra("service_id", serviceId);
                    startActivity(intent);
                });

                recentServicesContainer.addView(serviceView);
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