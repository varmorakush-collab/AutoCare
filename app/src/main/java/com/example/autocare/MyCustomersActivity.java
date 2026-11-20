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

public class MyCustomersActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout customersContainer;
    private TextView emptyStateTextView;
    private Button btnAddCustomerFab;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_customers);
        BottomNavHelper.setupBottomNav(this, R.id.navCustomers);

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

        if (loggedInMechanicId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        searchEditText = findViewById(R.id.searchEditText);
        customersContainer = findViewById(R.id.customersContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        btnAddCustomerFab = findViewById(R.id.btnAddCustomerFab);

        backImageView.setOnClickListener(v -> finish());

        btnAddCustomerFab.setOnClickListener(v -> {
            Intent intent = new Intent(MyCustomersActivity.this, AddCustomerActivity.class);
            startActivity(intent);
        });

        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navServices = findViewById(R.id.navServices);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(MyCustomersActivity.this, MechanicDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCustomers.setOnClickListener(v -> {
            // Already here
        });

        navServices.setOnClickListener(v -> {
            Intent intent = new Intent(MyCustomersActivity.this, MyServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MyCustomersActivity.this, MechanicProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

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
                loadData(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(searchEditText.getText().toString().trim());
    }

    private void loadData(String query) {
        customersContainer.removeAllViews();
        customersContainer.addView(emptyStateTextView);

        Cursor cursor = databaseHelper.searchCustomers(loggedInMechanicId, query);
        boolean hasData = false;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasData = true;
                final int customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String address = cursor.isNull(cursor.getColumnIndexOrThrow("address")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("address"));

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

                tvCustomerName.setText(fullName);
                tvCustomerMobile.setText(mobile);

                StringBuilder extraDetails = new StringBuilder();
                if (!email.isEmpty()) extraDetails.append(email);
                if (!address.isEmpty()) {
                    if (extraDetails.length() > 0) extraDetails.append(" • ");
                    extraDetails.append(address);
                }

                if (extraDetails.length() > 0) {
                    tvCustomerEmailAddress.setText(extraDetails.toString());
                    layoutEmailItemContainer.setVisibility(View.VISIBLE);
                } else {
                    layoutEmailItemContainer.setVisibility(View.GONE);
                }

                int vehicleCount = 0;
                Cursor vehicleCursor = databaseHelper.getVehiclesByCustomerId(customerId);
                if (vehicleCursor != null) {
                    vehicleCount = vehicleCursor.getCount();
                    vehicleCursor.close();
                }

                tvVehicleCount.setText(vehicleCount + (vehicleCount == 1 ? " Vehicle Linked" : " Vehicles Linked"));

                btnEditCustomer.setOnClickListener(v -> {
                    Intent intent = new Intent(MyCustomersActivity.this, AddCustomerActivity.class);
                    intent.putExtra("customer_id", customerId);
                    startActivity(intent);
                });

                btnDeleteCustomer.setOnClickListener(v -> {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Delete Customer");
                    builder.setMessage("Are you sure you want to delete " + fullName + "? Note: Customers with linked vehicles cannot be deleted.");
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = databaseHelper.deleteCustomer(customerId);
                        if (deleted) {
                            Toast.makeText(this, "Customer deleted", Toast.LENGTH_SHORT).show();
                            loadData(searchEditText.getText().toString().trim());
                        } else {
                            Toast.makeText(this, "Cannot delete customer. They have linked vehicles or history.", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                });

                btnAddVehicle.setOnClickListener(v -> {
                    Intent intent = new Intent(MyCustomersActivity.this, AddVehicleActivity.class);
                    intent.putExtra("customer_id", customerId);
                    startActivity(intent);
                });

                View.OnClickListener openVehiclesListener = v -> {
                    Intent intent = new Intent(MyCustomersActivity.this, CustomerVehiclesActivity.class);
                    intent.putExtra("customer_id", customerId);
                    startActivity(intent);
                };

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
