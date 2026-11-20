package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

public class GarageOwnerCustomersActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout customersContainer;
    private TextView emptyStateTextView;

    private DatabaseHelper databaseHelper;
    private int garageId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_owner_customers);
        BottomNavHelper.setupBottomNav(this, R.id.navCustomers);

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
        customersContainer = findViewById(R.id.customersContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());
        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));

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
                loadCustomers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bottom Navigation (Home, Mechanics, Customers, Services, Profile)
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navServices = findViewById(R.id.navServices);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerCustomersActivity.this, GarageOwnerDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navMechanics.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerCustomersActivity.this, ConnectedMechanicsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCustomers.setOnClickListener(v -> {
            // Already here
        });

        navServices.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerCustomersActivity.this, GarageOwnerServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerCustomersActivity.this, GarageOwnerProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers(searchEditText.getText().toString().trim());
    }

    private void loadCustomers(String query) {
        customersContainer.removeAllViews();
        boolean hasData = false;

        Cursor cursor = query.isEmpty() ? databaseHelper.getCustomersByGarageId(garageId) : databaseHelper.searchCustomersForGarage(garageId, query);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasData = true;
                final int customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String mechanicName = cursor.getString(cursor.getColumnIndexOrThrow("mechanic_name"));

                View customerView = LayoutInflater.from(this).inflate(R.layout.item_customer_card, customersContainer, false);
                View customerCardRoot = customerView.findViewById(R.id.customerCardRoot);
                TextView tvCustomerName = customerView.findViewById(R.id.tvCustomerName);
                TextView tvCustomerMobile = customerView.findViewById(R.id.tvCustomerMobile);
                TextView tvCustomerEmailAddress = customerView.findViewById(R.id.tvCustomerEmailAddress);
                LinearLayout layoutEmailItemContainer = customerView.findViewById(R.id.layoutEmailItemContainer);
                TextView tvVehicleCount = customerView.findViewById(R.id.tvVehicleCount);
                Button btnEditCustomer = customerView.findViewById(R.id.btnEditCustomer);
                Button btnDeleteCustomer = customerView.findViewById(R.id.btnDeleteCustomer);
                Button btnAddVehicle = customerView.findViewById(R.id.btnAddVehicle);
                Button btnViewVehicles = customerView.findViewById(R.id.btnViewVehicles);

                btnEditCustomer.setVisibility(View.GONE);
                btnDeleteCustomer.setVisibility(View.GONE);
                btnAddVehicle.setVisibility(View.GONE);

                tvCustomerName.setText(fullName);
                tvCustomerMobile.setText(mobile);

                StringBuilder extraDetails = new StringBuilder("Added by: " + mechanicName);
                if (!email.isEmpty()) extraDetails.append(" • ").append(email);
                if (!address.isEmpty()) extraDetails.append(" • ").append(address);

                tvCustomerEmailAddress.setText(extraDetails.toString());
                layoutEmailItemContainer.setVisibility(View.VISIBLE);

                int vehicleCount = 0;
                Cursor vehicleCursor = databaseHelper.getVehiclesByCustomerId(customerId);
                if (vehicleCursor != null) {
                    vehicleCount = vehicleCursor.getCount();
                    vehicleCursor.close();
                }

                tvVehicleCount.setText(vehicleCount + (vehicleCount == 1 ? " Vehicle Linked" : " Vehicles Linked"));

                View.OnClickListener openVehiclesListener = v -> {
                    Intent intent = new Intent(GarageOwnerCustomersActivity.this, CustomerVehiclesActivity.class);
                    intent.putExtra("customer_id", customerId);
                    startActivity(intent);
                };

                btnViewVehicles.setText("View Vehicles");
                btnViewVehicles.setOnClickListener(openVehiclesListener);
                customerCardRoot.setOnClickListener(openVehiclesListener);

                customersContainer.addView(customerView);
            }
            cursor.close();
        }

        if (hasData) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
