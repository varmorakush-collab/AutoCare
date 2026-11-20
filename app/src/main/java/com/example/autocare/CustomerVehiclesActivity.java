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

public class CustomerVehiclesActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvCustomerNameHeader, tvCustomerMobileHeader, tvCustomerEmailHeader, tvCustomerAddressHeader;
    private LinearLayout layoutEmailContainer, layoutAddressContainer;
    private Button btnAddVehicleHeader;
    private LinearLayout vehiclesContainer;
    private TextView emptyVehiclesTextView;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_vehicles);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if ("Mechanic".equals(userRole)) {
            loggedInMechanicId = databaseHelper.getMechanicId(userEmail);
        }

        customerId = getIntent().getIntExtra("customer_id", -1);

        if (customerId == -1) {
            Toast.makeText(this, "Customer data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvCustomerNameHeader = findViewById(R.id.tvCustomerNameHeader);
        tvCustomerMobileHeader = findViewById(R.id.tvCustomerMobileHeader);
        tvCustomerEmailHeader = findViewById(R.id.tvCustomerEmailHeader);
        tvCustomerAddressHeader = findViewById(R.id.tvCustomerAddressHeader);
        layoutEmailContainer = findViewById(R.id.layoutEmailContainer);
        layoutAddressContainer = findViewById(R.id.layoutAddressContainer);
        btnAddVehicleHeader = findViewById(R.id.btnAddVehicleHeader);
        vehiclesContainer = findViewById(R.id.vehiclesContainer);
        emptyVehiclesTextView = findViewById(R.id.emptyVehiclesTextView);

        if (!"Mechanic".equals(userRole)) {
            btnAddVehicleHeader.setVisibility(View.GONE);
        }

        backImageView.setOnClickListener(v -> finish());

        btnAddVehicleHeader.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerVehiclesActivity.this, AddVehicleActivity.class);
            intent.putExtra("customer_id", customerId);
            startActivity(intent);
        });

        loadCustomerInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomerInfo();
        loadVehicles();
    }

    private void loadCustomerInfo() {
        Cursor cursor = databaseHelper.getCustomerById(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));

            tvCustomerNameHeader.setText(name);
            tvCustomerMobileHeader.setText(mobile);

            if (!email.isEmpty()) {
                tvCustomerEmailHeader.setText(email);
                layoutEmailContainer.setVisibility(View.VISIBLE);
            } else {
                layoutEmailContainer.setVisibility(View.GONE);
            }

            if (!address.isEmpty()) {
                tvCustomerAddressHeader.setText(address);
                layoutAddressContainer.setVisibility(View.VISIBLE);
            } else {
                layoutAddressContainer.setVisibility(View.GONE);
            }
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Customer details not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadVehicles() {
        vehiclesContainer.removeAllViews();
        boolean hasVehicles = false;

        Cursor cursor = databaseHelper.getVehiclesByCustomerId(customerId);
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

                View vehicleView = LayoutInflater.from(this).inflate(R.layout.item_vehicle_card, vehiclesContainer, false);
                View vehicleCardRoot = vehicleView.findViewById(R.id.vehicleCardRoot);
                TextView tvVehicleModel = vehicleView.findViewById(R.id.tvVehicleModel);
                TextView tvVehicleRegBadge = vehicleView.findViewById(R.id.tvVehicleRegBadge);
                TextView tvVehicleYearChip = vehicleView.findViewById(R.id.tvVehicleYearChip);
                TextView tvVehicleColorChip = vehicleView.findViewById(R.id.tvVehicleColorChip);
                Button btnEditVehicle = vehicleView.findViewById(R.id.btnEditVehicle);
                Button btnDeleteVehicle = vehicleView.findViewById(R.id.btnDeleteVehicle);
                Button btnAddService = vehicleView.findViewById(R.id.btnAddService);
                Button btnViewHistory = vehicleView.findViewById(R.id.btnViewHistory);

                tvVehicleModel.setText(brand + " " + model + " (" + type + ")");
                tvVehicleRegBadge.setText(regNumber);

                if (year != null && !year.isEmpty()) {
                    tvVehicleYearChip.setText(year);
                    tvVehicleYearChip.setVisibility(View.VISIBLE);
                } else {
                    tvVehicleYearChip.setVisibility(View.GONE);
                }

                if (color != null && !color.isEmpty()) {
                    tvVehicleColorChip.setText(color);
                    tvVehicleColorChip.setVisibility(View.VISIBLE);
                } else {
                    tvVehicleColorChip.setVisibility(View.GONE);
                }

                SharedPreferences sPrefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
                String currentRole = sPrefs.getString("user_role", null);
                if (!"Mechanic".equals(currentRole)) {
                    btnEditVehicle.setVisibility(View.GONE);
                    btnDeleteVehicle.setVisibility(View.GONE);
                    btnAddService.setVisibility(View.GONE);
                }

                btnEditVehicle.setOnClickListener(ev -> {
                    Intent intent = new Intent(CustomerVehiclesActivity.this, AddVehicleActivity.class);
                    intent.putExtra("customer_id", customerId);
                    intent.putExtra("vehicle_id", vehicleId);
                    startActivity(intent);
                });

                btnDeleteVehicle.setOnClickListener(dv -> {
                    android.app.AlertDialog.Builder vBuilder = new android.app.AlertDialog.Builder(this);
                    vBuilder.setTitle("Delete Vehicle");
                    vBuilder.setMessage("Are you sure you want to delete vehicle " + regNumber + "? Note: Vehicles with service history cannot be deleted.");
                    vBuilder.setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = databaseHelper.deleteVehicle(vehicleId);
                        if (deleted) {
                            Toast.makeText(this, "Vehicle deleted", Toast.LENGTH_SHORT).show();
                            loadVehicles();
                        } else {
                            Toast.makeText(this, "Cannot delete vehicle. It has service history.", Toast.LENGTH_LONG).show();
                        }
                    });
                    vBuilder.setNegativeButton("Cancel", null);
                    vBuilder.show();
                });

                btnAddService.setOnClickListener(v -> {
                    Intent intent = new Intent(CustomerVehiclesActivity.this, AddServiceActivity.class);
                    intent.putExtra("vehicle_id", vehicleId);
                    startActivity(intent);
                });

                View.OnClickListener openServiceHistoryListener = v -> {
                    Intent intent = new Intent(CustomerVehiclesActivity.this, ServiceDetailsActivity.class);
                    intent.putExtra("vehicle_id", vehicleId);
                    startActivity(intent);
                };

                btnViewHistory.setOnClickListener(openServiceHistoryListener);
                vehicleCardRoot.setOnClickListener(openServiceHistoryListener);

                vehiclesContainer.addView(vehicleView);
            }
            cursor.close();
        }

        if (hasVehicles) {
            emptyVehiclesTextView.setVisibility(View.GONE);
        } else {
            emptyVehiclesTextView.setVisibility(View.VISIBLE);
        }
    }
}
