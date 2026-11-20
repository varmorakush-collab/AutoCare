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

public class MechanicActivityDetailsActivity extends AppCompatActivity {

    private TextView tvMechanicName, tvMechanicPhone, tvMechanicEmail, tvMechanicCity;
    private TextView tvStatCustomers, tvStatVehicles, tvStatServices;
    private LinearLayout customersContainer, servicesContainer;
    private TextView emptyCustomersTextView, emptyServicesTextView;

    private DatabaseHelper databaseHelper;
    private int mechanicId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_activity_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);
        mechanicId = getIntent().getIntExtra("mechanic_id", -1);

        if (mechanicId == -1) {
            Toast.makeText(this, "Mechanic data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView backImageView = findViewById(R.id.backImageView);
        tvMechanicName = findViewById(R.id.tvMechanicName);
        tvMechanicPhone = findViewById(R.id.tvMechanicPhone);
        tvMechanicEmail = findViewById(R.id.tvMechanicEmail);
        tvMechanicCity = findViewById(R.id.tvMechanicCity);

        tvStatCustomers = findViewById(R.id.tvStatCustomers);
        tvStatVehicles = findViewById(R.id.tvStatVehicles);
        tvStatServices = findViewById(R.id.tvStatServices);

        customersContainer = findViewById(R.id.customersContainer);
        servicesContainer = findViewById(R.id.servicesContainer);
        emptyCustomersTextView = findViewById(R.id.emptyCustomersTextView);
        emptyServicesTextView = findViewById(R.id.emptyServicesTextView);

        backImageView.setOnClickListener(v -> finish());

        loadMechanicProfile();
        loadCustomers();
        loadServices();
    }

    private void loadMechanicProfile() {
        Cursor cursor = databaseHelper.getMechanicById(mechanicId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));

            tvMechanicName.setText(name);
            tvMechanicPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not specified");
            tvMechanicEmail.setText(email != null && !email.isEmpty() ? email : "No email");
            tvMechanicCity.setText(city != null && !city.isEmpty() ? city : "Location not set");
            cursor.close();
        }

        int cCount = databaseHelper.getMechanicCustomerCount(mechanicId);
        int vCount = databaseHelper.getMechanicVehicleCount(mechanicId);
        int sCount = databaseHelper.getMechanicServiceCount(mechanicId);

        tvStatCustomers.setText(String.valueOf(cCount));
        tvStatVehicles.setText(String.valueOf(vCount));
        tvStatServices.setText(String.valueOf(sCount));
    }

    private void loadCustomers() {
        customersContainer.removeAllViews();
        boolean hasCustomers = false;

        Cursor cursor = databaseHelper.getCustomersByMechanicId(mechanicId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasCustomers = true;
                final int customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));

                View customerView = LayoutInflater.from(this).inflate(R.layout.item_mechanic_customer, customersContainer, false);
                TextView tvCustomerName = customerView.findViewById(R.id.tvCustomerName);
                TextView tvCustomerMobile = customerView.findViewById(R.id.tvCustomerMobile);
                TextView tvCustomerEmail = customerView.findViewById(R.id.tvCustomerEmail);
                LinearLayout layoutEmailContainer = customerView.findViewById(R.id.layoutEmailContainer);
                TextView tvVehicleChip = customerView.findViewById(R.id.tvVehicleChip);

                tvCustomerName.setText(name);
                tvCustomerMobile.setText(mobile);

                if (!email.isEmpty()) {
                    tvCustomerEmail.setText(email);
                    layoutEmailContainer.setVisibility(View.VISIBLE);
                } else {
                    layoutEmailContainer.setVisibility(View.GONE);
                }

                int vCount = 0;
                Cursor vCursor = databaseHelper.getVehiclesByCustomerId(customerId);
                if (vCursor != null) {
                    vCount = vCursor.getCount();
                    vCursor.close();
                }

                tvVehicleChip.setText(vCount + (vCount == 1 ? " Vehicle Linked" : " Vehicles Linked"));

                customerView.setOnClickListener(v -> {
                    Intent intent = new Intent(MechanicActivityDetailsActivity.this, CustomerVehiclesActivity.class);
                    intent.putExtra("customer_id", customerId);
                    startActivity(intent);
                });

                customersContainer.addView(customerView);
            }
            cursor.close();
        }

        if (hasCustomers) {
            emptyCustomersTextView.setVisibility(View.GONE);
        } else {
            emptyCustomersTextView.setVisibility(View.VISIBLE);
        }
    }

    private void loadServices() {
        servicesContainer.removeAllViews();
        boolean hasServices = false;

        Cursor cursor = databaseHelper.getServicesByMechanicId(mechanicId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasServices = true;
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
                tvHistCustomer.setText("Customer: " + customerName);
                tvHistDate.setText(date);
                tvHistCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                View.OnClickListener openDetailsListener = v -> {
                    Intent intent = new Intent(MechanicActivityDetailsActivity.this, ServiceDetailsActivity.class);
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
