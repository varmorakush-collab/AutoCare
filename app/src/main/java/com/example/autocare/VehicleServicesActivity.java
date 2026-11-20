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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class VehicleServicesActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvHeaderTitle, emptyStateTextView;
    private LinearLayout servicesContainer;

    private DatabaseHelper databaseHelper;
    private int vehicleId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_services);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavHelper.setupBottomNav(this, R.id.navVehicles);
        databaseHelper = new DatabaseHelper(this);

        vehicleId = getIntent().getIntExtra("vehicle_id", -1);
        if (vehicleId == -1) {
            Toast.makeText(this, "Vehicle data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        servicesContainer = findViewById(R.id.servicesContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());
        loadVehicleServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicleServices();
    }

    private void loadVehicleServices() {
        servicesContainer.removeAllViews();
        boolean hasServices = false;

        Cursor vCursor = databaseHelper.getReadableDatabase().rawQuery("SELECT brand, model, reg_number FROM vehicles WHERE id = ?", new String[]{String.valueOf(vehicleId)});
        if (vCursor != null && vCursor.moveToFirst()) {
            String brand = vCursor.getString(0);
            String model = vCursor.getString(1);
            String regNumber = vCursor.getString(2);
            tvHeaderTitle.setText(brand + " " + model + " (" + regNumber + ")");
            vCursor.close();
        }

        Cursor cursor = databaseHelper.getServicesByVehicleId(vehicleId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasServices = true;
                final int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("service_type"));
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                String paymentStatus = "Pending";
                int payIdx = cursor.getColumnIndex("payment_status");
                if (payIdx != -1 && !cursor.isNull(payIdx)) {
                    paymentStatus = cursor.getString(payIdx);
                }

                View serviceView = LayoutInflater.from(this).inflate(R.layout.item_service_history, servicesContainer, false);
                TextView tvHistType = serviceView.findViewById(R.id.tvHistType);
                TextView tvHistStatus = serviceView.findViewById(R.id.tvHistStatus);
                TextView tvHistDate = serviceView.findViewById(R.id.tvHistDate);
                TextView tvHistVehicle = serviceView.findViewById(R.id.tvHistVehicle);
                TextView tvHistCustomer = serviceView.findViewById(R.id.tvHistCustomer);
                TextView tvHistCost = serviceView.findViewById(R.id.tvHistCost);
                Button btnViewService = serviceView.findViewById(R.id.btnViewService);

                tvHistType.setText(type);
                tvHistStatus.setText(status + " • Pay: " + paymentStatus);

                if ("Completed".equalsIgnoreCase(status) || "Paid".equalsIgnoreCase(paymentStatus)) {
                    tvHistStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                    tvHistStatus.setTextColor(android.graphics.Color.parseColor("#15803D"));
                } else {
                    tvHistStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7")));
                    tvHistStatus.setTextColor(android.graphics.Color.parseColor("#D97706"));
                }

                tvHistDate.setText("Service Date: " + date);
                tvHistVehicle.setText("Job Order #" + serviceId);
                tvHistCustomer.setVisibility(View.GONE);
                tvHistCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                View.OnClickListener openDetailListener = v -> {
                    Intent intent = new Intent(VehicleServicesActivity.this, CustomerServiceDetailsActivity.class);
                    intent.putExtra("service_id", serviceId);
                    startActivity(intent);
                };

                btnViewService.setOnClickListener(openDetailListener);
                serviceView.setOnClickListener(openDetailListener);

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