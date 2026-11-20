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

public class AdminCustomerDetailsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private EditText etCustomerName, etCustomerMobile, etCustomerEmail, etCustomerAddress;
    private Button btnUpdate, btnDelete;
    private DatabaseHelper databaseHelper;
    private int customerId = -1;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customer_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);
        customerId = getIntent().getIntExtra("customer_id", -1);

        if (customerId == -1) {
            Toast.makeText(this, "Customer ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerMobile = findViewById(R.id.etCustomerMobile);
        etCustomerEmail = findViewById(R.id.etCustomerEmail);
        etCustomerAddress = findViewById(R.id.etCustomerAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        backImageView.setOnClickListener(v -> finish());
        loadCustomerData();
        loadCustomerStatistics();

        setFieldsEnabled(false);
        btnUpdate.setText("Edit Customer");

        btnUpdate.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                setFieldsEnabled(true);
                btnUpdate.setText("Save Changes");
                etCustomerName.requestFocus();
            } else {
                updateCustomer();
            }
        });
        btnDelete.setOnClickListener(v -> confirmDeleteCustomer());
    }

    private void setFieldsEnabled(boolean enabled) {
        etCustomerName.setEnabled(enabled);
        etCustomerMobile.setEnabled(enabled);
        etCustomerEmail.setEnabled(enabled);
        etCustomerAddress.setEnabled(enabled);
    }

    private void loadCustomerStatistics() {
        android.widget.TextView tvVehicleCount = findViewById(R.id.tvVehicleCount);
        android.widget.TextView tvServiceCount = findViewById(R.id.tvServiceCount);
        android.widget.LinearLayout servicesContainer = findViewById(R.id.servicesContainer);

        int vehicleCount = 0;
        Cursor vCursor = databaseHelper.getVehiclesByCustomerId(customerId);
        if (vCursor != null) {
            vehicleCount = vCursor.getCount();
            vCursor.close();
        }
        tvVehicleCount.setText(String.valueOf(vehicleCount));

        int serviceCount = 0;
        Cursor cursor = databaseHelper.getCustomerServices(customerId);
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
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));

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
                
                // Get Mechanic Name is not available in getCustomerServices directly, we will fetch it if possible or hide
                tvHistCustomer.setVisibility(android.view.View.GONE);
                
                tvHistCost.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", totalCost));
                btnViewService.setVisibility(android.view.View.VISIBLE);
                
                btnViewService.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(AdminCustomerDetailsActivity.this, AdminServiceDetailsActivity.class);
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

    private void loadCustomerData() {
        Cursor cursor = databaseHelper.getCustomerById(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            etCustomerName.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            etCustomerMobile.setText(cursor.getString(cursor.getColumnIndexOrThrow("mobile")));
            etCustomerEmail.setText(cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email")));
            etCustomerAddress.setText(cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address")));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Customer not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateCustomer() {
        String name = etCustomerName.getText().toString().trim();
        String mobile = etCustomerMobile.getText().toString().trim();
        String email = etCustomerEmail.getText().toString().trim();
        String address = etCustomerAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Name and mobile number are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.updateCustomerAdmin(customerId, name, mobile, email, address);
        if (success) {
            Toast.makeText(this, "Customer updated successfully!", Toast.LENGTH_SHORT).show();
            isEditing = false;
            setFieldsEnabled(false);
            btnUpdate.setText("Edit Customer");
        } else {
            Toast.makeText(this, "Failed to update customer.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteCustomer() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Are you sure you want to delete this customer?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteCustomer(customerId);
                    if (success) {
                        Toast.makeText(this, "Customer deleted successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete customer.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
