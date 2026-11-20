package com.example.autocare;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class AddVehicleActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvHeaderTitle, tvCustomerNameSummary, tvCustomerMobileSummary, tvCustomerEmailSummary, tvCustomerAddressSummary;
    private Spinner vehicleTypeSpinner;
    private EditText regNumberEditText, brandEditText, modelEditText, yearEditText, colorEditText;
    private Button saveVehicleButton, deleteVehicleButton;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int customerId = -1;
    private int vehicleId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail != null && "Mechanic".equals(userRole)) {
            loggedInMechanicId = databaseHelper.getMechanicId(userEmail);
        }

        customerId = getIntent().getIntExtra("customer_id", -1);
        vehicleId = getIntent().getIntExtra("vehicle_id", -1);

        if (loggedInMechanicId == -1 || customerId == -1) {
            Toast.makeText(this, "Session or customer data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to manage vehicles.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvCustomerNameSummary = findViewById(R.id.tvCustomerNameSummary);
        tvCustomerMobileSummary = findViewById(R.id.tvCustomerMobileSummary);
        tvCustomerEmailSummary = findViewById(R.id.tvCustomerEmailSummary);
        tvCustomerAddressSummary = findViewById(R.id.tvCustomerAddressSummary);
        vehicleTypeSpinner = findViewById(R.id.vehicleTypeSpinner);
        regNumberEditText = findViewById(R.id.regNumberEditText);
        brandEditText = findViewById(R.id.brandEditText);
        modelEditText = findViewById(R.id.modelEditText);
        yearEditText = findViewById(R.id.yearEditText);
        colorEditText = findViewById(R.id.colorEditText);
        saveVehicleButton = findViewById(R.id.saveVehicleButton);
        deleteVehicleButton = findViewById(R.id.deleteVehicleButton);

        loadCustomerDetails();

        String[] types = {"Car", "Bike", "Scooter", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, types);
        typeAdapter.setDropDownViewResource(R.layout.item_spinner);
        vehicleTypeSpinner.setAdapter(typeAdapter);

        if (vehicleId != -1) {
            tvHeaderTitle.setText("Edit Vehicle");
            saveVehicleButton.setText("Update Vehicle Details");
            deleteVehicleButton.setVisibility(View.VISIBLE);
            loadVehicleData();
        } else {
            tvHeaderTitle.setText("Add Vehicle");
            saveVehicleButton.setText("Save Vehicle");
            deleteVehicleButton.setVisibility(View.GONE);
        }

        backImageView.setOnClickListener(v -> finish());

        saveVehicleButton.setOnClickListener(v -> saveOrUpdateVehicle());

        deleteVehicleButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to delete this vehicle? Note: Vehicles with service history cannot be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteVehicle(vehicleId);
                    if (deleted) {
                        Toast.makeText(this, "Vehicle deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Cannot delete vehicle. It has service history.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    private void loadCustomerDetails() {
        Cursor cursor = databaseHelper.getCustomerById(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));

            tvCustomerNameSummary.setText(customerName);
            tvCustomerMobileSummary.setText(mobile);

            if (!email.isEmpty()) {
                tvCustomerEmailSummary.setText(email);
                findViewById(R.id.layoutEmailContainer).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.layoutEmailContainer).setVisibility(View.GONE);
            }

            if (!address.isEmpty()) {
                tvCustomerAddressSummary.setText(address);
                findViewById(R.id.layoutAddressContainer).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.layoutAddressContainer).setVisibility(View.GONE);
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Customer not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void loadVehicleData() {
        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery("SELECT type, reg_number, brand, model, year, color FROM vehicles WHERE id = ?", new String[]{String.valueOf(vehicleId)});
        if (cursor != null && cursor.moveToFirst()) {
            String type = cursor.getString(0);
            String regNumber = cursor.getString(1);
            String brand = cursor.getString(2);
            String model = cursor.getString(3);
            String year = cursor.getString(4);
            String color = cursor.getString(5);

            List<String> typesList = Arrays.asList("Car", "Bike", "Scooter", "Other");
            int typeIndex = typesList.indexOf(type);
            if (typeIndex != -1) {
                vehicleTypeSpinner.setSelection(typeIndex);
            }
            regNumberEditText.setText(regNumber);
            brandEditText.setText(brand);
            modelEditText.setText(model);
            yearEditText.setText(year != null ? year : "");
            colorEditText.setText(color != null ? color : "");

            cursor.close();
        } else {
            Toast.makeText(this, "Vehicle not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void saveOrUpdateVehicle() {
        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to manage vehicles.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String vType = vehicleTypeSpinner.getSelectedItem() != null ? vehicleTypeSpinner.getSelectedItem().toString() : "Car";

        String regNumber = regNumberEditText.getText() != null ? regNumberEditText.getText().toString().trim().toUpperCase() : "";
        String brand = brandEditText.getText() != null ? brandEditText.getText().toString().trim() : "";
        String model = modelEditText.getText() != null ? modelEditText.getText().toString().trim() : "";
        String year = yearEditText.getText() != null ? yearEditText.getText().toString().trim() : "";
        String color = colorEditText.getText() != null ? colorEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(regNumber) || regNumber.length() < 4) {
            regNumberEditText.setError("Valid Registration Number is required");
            regNumberEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(brand)) {
            brandEditText.setError("Vehicle Brand is required");
            brandEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(model)) {
            modelEditText.setError("Vehicle Model is required");
            modelEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(year) || year.length() != 4) {
            yearEditText.setError("Valid 4-digit Year is required");
            yearEditText.requestFocus();
            return;
        }

        boolean success;
        if (vehicleId != -1) {
            success = databaseHelper.updateVehicle(vehicleId, vType, regNumber, brand, model, year, color);
        } else {
            if (databaseHelper.checkVehicleExists(regNumber)) {
                Toast.makeText(this, "A vehicle with this registration number already exists.", Toast.LENGTH_LONG).show();
                return;
            }
            success = databaseHelper.addVehicle(customerId, loggedInMechanicId, vType, regNumber, brand, model, year, color);
        }

        if (success) {
            Toast.makeText(this, vehicleId != -1 ? "Vehicle updated successfully!" : "Vehicle added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save vehicle. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
