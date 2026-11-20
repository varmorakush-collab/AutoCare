package com.example.autocare;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AdminGarageDetailsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private EditText etGarageName, etOwnerName, etEmail, etPhone, etAddress;
    private Button btnUpdate, btnDelete;
    private DatabaseHelper databaseHelper;
    private int garageId = -1;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_garage_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);
        garageId = getIntent().getIntExtra("garage_id", -1);

        if (garageId == -1) {
            Toast.makeText(this, "Garage ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        etGarageName = findViewById(R.id.etGarageName);
        etOwnerName = findViewById(R.id.etOwnerName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        backImageView.setOnClickListener(v -> finish());
        loadGarageData();
        loadGarageStatistics();

        setFieldsEnabled(false);
        btnUpdate.setText("Edit Garage");

        btnUpdate.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                setFieldsEnabled(true);
                btnUpdate.setText("Save Changes");
                etGarageName.requestFocus();
            } else {
                updateGarage();
            }
        });
        btnDelete.setOnClickListener(v -> confirmDeleteGarage());
    }

    private void setFieldsEnabled(boolean enabled) {
        etGarageName.setEnabled(enabled);
        etOwnerName.setEnabled(enabled);
        etEmail.setEnabled(enabled); // Depending on your need, maybe keep email disabled if it's unique ID, but we enable it here if you want.
        etPhone.setEnabled(enabled);
        etAddress.setEnabled(enabled);
    }

    private void loadGarageStatistics() {
        android.widget.TextView tvMechanicCount = findViewById(R.id.tvMechanicCount);
        android.widget.TextView tvCustomerCount = findViewById(R.id.tvCustomerCount);
        android.widget.TextView tvServiceCount = findViewById(R.id.tvServiceCount);
        android.widget.LinearLayout servicesContainer = findViewById(R.id.servicesContainer);

        int mechanicCount = databaseHelper.getGarageConnectedMechanicCount(garageId);
        tvMechanicCount.setText(String.valueOf(mechanicCount));
        
        int customerCount = databaseHelper.getGarageCustomerCount(garageId);
        tvCustomerCount.setText(String.valueOf(customerCount));

        int serviceCount = 0;
        Cursor cursor = databaseHelper.getServicesByGarageId(garageId);
        if (cursor != null) {
            serviceCount = cursor.getCount();
            tvServiceCount.setText(String.valueOf(serviceCount));

            servicesContainer.removeAllViews();
            while (cursor.moveToNext()) {
                int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String serviceType = cursor.getString(cursor.getColumnIndexOrThrow("service_type"));
                String serviceDate = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String vehicleNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
                double laborCost = cursor.getDouble(cursor.getColumnIndexOrThrow("labour_charges"));
                double partsCost = cursor.getDouble(cursor.getColumnIndexOrThrow("parts_cost"));
                double totalCost = laborCost + partsCost;

                android.view.View serviceView = android.view.LayoutInflater.from(this).inflate(R.layout.item_service_history, servicesContainer, false);

                android.widget.TextView tvHistType = serviceView.findViewById(R.id.tvHistType);
                android.widget.TextView tvHistStatus = serviceView.findViewById(R.id.tvHistStatus);
                android.widget.TextView tvHistDate = serviceView.findViewById(R.id.tvHistDate);
                android.widget.TextView tvHistVehicle = serviceView.findViewById(R.id.tvHistVehicle);
                android.widget.TextView tvHistCustomer = serviceView.findViewById(R.id.tvHistCustomer);
                android.widget.TextView tvHistCost = serviceView.findViewById(R.id.tvHistCost);
                android.widget.Button btnViewService = serviceView.findViewById(R.id.btnViewService);

                tvHistType.setText(serviceType);
                tvHistStatus.setText(status);
                tvHistDate.setText(serviceDate);
                tvHistVehicle.setText(getString(R.string.vehicle_number_format, vehicleNumber));
                
                String mechanicName = cursor.getString(cursor.getColumnIndexOrThrow("mechanic_name"));
                if (mechanicName != null && !mechanicName.isEmpty()) {
                    tvHistCustomer.setText("Mechanic: " + mechanicName);
                    tvHistCustomer.setVisibility(android.view.View.VISIBLE);
                } else {
                    tvHistCustomer.setVisibility(android.view.View.GONE);
                }
                
                tvHistCost.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", totalCost));
                btnViewService.setVisibility(android.view.View.VISIBLE);
                
                btnViewService.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(AdminGarageDetailsActivity.this, AdminServiceDetailsActivity.class);
                    intent.putExtra("SERVICE_ID", serviceId);
                    startActivity(intent);
                });

                servicesContainer.addView(serviceView);
            }
            cursor.close();
        } else {
            tvServiceCount.setText("0");
        }
    }

    private void loadGarageData() {
        Cursor cursor = databaseHelper.getGarageOwnerById(garageId);
        if (cursor != null && cursor.moveToFirst()) {
            etGarageName.setText(cursor.getString(cursor.getColumnIndexOrThrow("garage_name")));
            etOwnerName.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Garage not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateGarage() {
        String garageName = etGarageName.getText().toString().trim();
        String ownerName = etOwnerName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(garageName) || TextUtils.isEmpty(ownerName)) {
            Toast.makeText(this, "Garage name and Owner name are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.updateGarageOwnerProfile(garageId, ownerName, garageName, phone, address);
        if (success) {
            Toast.makeText(this, "Garage updated successfully!", Toast.LENGTH_SHORT).show();
            isEditing = false;
            setFieldsEnabled(false);
            btnUpdate.setText("Edit Garage");
        } else {
            Toast.makeText(this, "Failed to update garage.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteGarage() {
        int connectedMechanics = databaseHelper.getGarageOwnerConnectedMechanicsCount(garageId);
        if (connectedMechanics > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Garage")
                    .setMessage("This garage has " + connectedMechanics + " connected mechanic(s). Please disconnect all mechanics from this garage first before deleting it.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Garage")
                .setMessage("Are you sure you want to delete this garage?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteGarageOwner(garageId);
                    if (success) {
                        Toast.makeText(this, "Garage deleted successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Cannot delete garage. Please disconnect all mechanics first.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
