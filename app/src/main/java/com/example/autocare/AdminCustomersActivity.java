package com.example.autocare;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminCustomersActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout customersContainer;
    private TextView emptyStateTextView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customers);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavHelper.setupBottomNav(this, R.id.navCustomers);

        databaseHelper = new DatabaseHelper(this);

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

        // Setup bottom nav for admin
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navGarages = findViewById(R.id.navGarages);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCustomersActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navGarages != null) {
            navGarages.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCustomersActivity.this, AdminGaragesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navCustomers != null) {
            navCustomers.setOnClickListener(v -> {});
        }
        if (navMechanics != null) {
            navMechanics.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCustomersActivity.this, AdminMechanicsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers(searchEditText.getText().toString().trim());
    }

    private void loadCustomers(String query) {
        customersContainer.removeAllViews();
        boolean hasCustomers = false;

        Cursor cursor = databaseHelper.getAllCustomers(query);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasCustomers = true;
                final int customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
                String email = cursor.isNull(cursor.getColumnIndexOrThrow("email")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String mechanicName = cursor.isNull(cursor.getColumnIndexOrThrow("mechanic_name")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("mechanic_name"));

                View custView = LayoutInflater.from(this).inflate(R.layout.item_mechanic_customer, customersContainer, false);
                TextView tvName = custView.findViewById(R.id.tvCustomerName);
                TextView tvMobile = custView.findViewById(R.id.tvCustomerMobile);
                TextView tvEmail = custView.findViewById(R.id.tvCustomerEmail);
                LinearLayout layoutEmail = custView.findViewById(R.id.layoutEmailContainer);
                TextView tvVehicleChip = custView.findViewById(R.id.tvVehicleChip);
                android.widget.Button btnViewDetails = custView.findViewById(R.id.btnViewDetails);

                tvName.setText(name);
                tvMobile.setText(mobile);
                tvEmail.setText(email + (!mechanicName.isEmpty() ? " • Added by: " + mechanicName : ""));
                layoutEmail.setVisibility(View.VISIBLE);
                tvVehicleChip.setText("Customer");
                btnViewDetails.setVisibility(View.VISIBLE);

                custView.setOnClickListener(null); // Disable full card click
                custView.setClickable(false);

                btnViewDetails.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminCustomersActivity.this, AdminCustomerDetailsActivity.class);
                    intent.putExtra("customer_id", customerId);
                    startActivity(intent);
                });

                customersContainer.addView(custView);
            }
            cursor.close();
        }

        if (hasCustomers) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
