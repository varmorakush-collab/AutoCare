package com.example.autocare;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class AdminServicesActivity extends AppCompatActivity {

    private ImageView backImageView;
    private LinearLayout servicesContainer;
    private TextView emptyStateTextView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        backImageView = findViewById(R.id.backImageView);
        servicesContainer = findViewById(R.id.servicesContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());

        // Setup bottom nav for admin
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navGarages = findViewById(R.id.navGarages);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(AdminServicesActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navGarages != null) {
            navGarages.setOnClickListener(v -> {
                Intent intent = new Intent(AdminServicesActivity.this, AdminGaragesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navCustomers != null) {
            navCustomers.setOnClickListener(v -> {
                Intent intent = new Intent(AdminServicesActivity.this, AdminCustomersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navMechanics != null) {
            navMechanics.setOnClickListener(v -> {
                Intent intent = new Intent(AdminServicesActivity.this, AdminMechanicsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices();
    }

    private void loadServices() {
        servicesContainer.removeAllViews();
        boolean hasServices = false;

        Cursor cursor = databaseHelper.getAllServices();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasServices = true;
                final int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String serviceType = cursor.getString(cursor.getColumnIndexOrThrow("service_type"));
                String serviceDate = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
                String mechanicName = cursor.getString(cursor.getColumnIndexOrThrow("mechanic_name"));

                View serviceView = LayoutInflater.from(this).inflate(R.layout.item_service_history, servicesContainer, false);
                TextView tvHistType = serviceView.findViewById(R.id.tvHistType);
                TextView tvHistStatus = serviceView.findViewById(R.id.tvHistStatus);
                TextView tvHistDate = serviceView.findViewById(R.id.tvHistDate);
                TextView tvHistVehicle = serviceView.findViewById(R.id.tvHistVehicle);
                TextView tvHistCustomer = serviceView.findViewById(R.id.tvHistCustomer);
                TextView tvHistCost = serviceView.findViewById(R.id.tvHistCost);
                Button btnViewService = serviceView.findViewById(R.id.btnViewService);

                tvHistType.setText(serviceType);
                tvHistStatus.setText(status != null ? status : "Completed");
                tvHistDate.setText("Service Date: " + serviceDate);
                tvHistCustomer.setText("Customer: " + customerName + " | Mechanic: " + mechanicName);
                tvHistCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                tvHistVehicle.setText("System Service Record #" + serviceId);

                btnViewService.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminServicesActivity.this, ServiceDetailsActivity.class);
                    intent.putExtra("service_id", serviceId);
                    startActivity(intent);
                });

                servicesContainer.addView(serviceView);
            }
            cursor.close();
        }

        if (hasServices) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
