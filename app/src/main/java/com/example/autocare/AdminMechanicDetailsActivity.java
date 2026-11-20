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

public class AdminMechanicDetailsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private EditText etMechanicName, etMechanicEmail, etMechanicPhone, etMechanicCity;
    private Button btnUpdate, btnDelete;
    private DatabaseHelper databaseHelper;
    private int mechanicId = -1;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mechanic_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);
        mechanicId = getIntent().getIntExtra("mechanic_id", -1);

        if (mechanicId == -1) {
            Toast.makeText(this, "Mechanic ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        etMechanicName = findViewById(R.id.etMechanicName);
        etMechanicEmail = findViewById(R.id.etMechanicEmail);
        etMechanicPhone = findViewById(R.id.etMechanicPhone);
        etMechanicCity = findViewById(R.id.etMechanicCity);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        backImageView.setOnClickListener(v -> finish());
        loadMechanicData();
        loadMechanicStatistics();

        setFieldsEnabled(false);
        btnUpdate.setText("Edit Mechanic");

        btnUpdate.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                setFieldsEnabled(true);
                btnUpdate.setText("Save Changes");
                etMechanicName.requestFocus();
            } else {
                updateMechanic();
            }
        });
        btnDelete.setOnClickListener(v -> confirmDeleteMechanic());
    }

    private void setFieldsEnabled(boolean enabled) {
        etMechanicName.setEnabled(enabled);
        etMechanicEmail.setEnabled(enabled);
        etMechanicPhone.setEnabled(enabled);
        etMechanicCity.setEnabled(enabled);
    }

    private void loadMechanicStatistics() {
        android.widget.TextView tvConnectionInfo = findViewById(R.id.tvConnectionInfo);
        android.widget.TextView tvCustomerCount = findViewById(R.id.tvCustomerCount);
        android.widget.TextView tvServiceCount = findViewById(R.id.tvServiceCount);
        android.widget.LinearLayout servicesContainer = findViewById(R.id.servicesContainer);

        String connectionInfo = databaseHelper.getMechanicConnectionInfo(mechanicId);
        tvConnectionInfo.setText(connectionInfo);

        int customerCount = databaseHelper.getMechanicCustomerCount(mechanicId);
        tvCustomerCount.setText(String.valueOf(customerCount));

        int serviceCount = 0;
        Cursor cursor = databaseHelper.getServicesByMechanicId(mechanicId);
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
                
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                if (customerName != null && !customerName.isEmpty()) {
                    tvHistCustomer.setText("Customer: " + customerName);
                } else {
                    tvHistCustomer.setVisibility(android.view.View.GONE);
                }
                
                tvHistCost.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", totalCost));
                btnViewService.setVisibility(android.view.View.VISIBLE);
                
                btnViewService.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(AdminMechanicDetailsActivity.this, AdminServiceDetailsActivity.class);
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

    private void loadMechanicData() {
        Cursor cursor = databaseHelper.getMechanicById(mechanicId);
        if (cursor != null && cursor.moveToFirst()) {
            etMechanicName.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            etMechanicEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            etMechanicPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            etMechanicCity.setText(cursor.getString(cursor.getColumnIndexOrThrow("city")));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Mechanic not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateMechanic() {
        String name = etMechanicName.getText().toString().trim();
        String phone = etMechanicPhone.getText().toString().trim();
        String city = etMechanicCity.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Mechanic name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.updateMechanicProfile(mechanicId, name, phone, city);
        if (success) {
            Toast.makeText(this, "Mechanic updated successfully!", Toast.LENGTH_SHORT).show();
            isEditing = false;
            setFieldsEnabled(false);
            btnUpdate.setText("Edit Mechanic");
        } else {
            Toast.makeText(this, "Failed to update mechanic.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteMechanic() {
        int linkedCustomers = databaseHelper.getMechanicLinkedCustomersCount(mechanicId);
        if (linkedCustomers > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Mechanic")
                    .setMessage("This mechanic has " + linkedCustomers + " linked customer(s). Please remove or reassign the mechanic's customers first before deleting this mechanic.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Mechanic")
                .setMessage("Are you sure you want to delete this mechanic?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteMechanic(mechanicId);
                    if (success) {
                        Toast.makeText(this, "Mechanic deleted successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Cannot delete mechanic. Please remove linked customers first.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
