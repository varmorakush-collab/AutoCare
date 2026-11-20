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

public class AdminMechanicsActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout mechanicsContainer;
    private TextView emptyStateTextView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mechanics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavHelper.setupBottomNav(this, R.id.navMechanics);

        databaseHelper = new DatabaseHelper(this);

        backImageView = findViewById(R.id.backImageView);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        searchEditText = findViewById(R.id.searchEditText);
        mechanicsContainer = findViewById(R.id.mechanicsContainer);
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
                loadMechanics(s.toString().trim());
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
                Intent intent = new Intent(AdminMechanicsActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navGarages != null) {
            navGarages.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMechanicsActivity.this, AdminGaragesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navCustomers != null) {
            navCustomers.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMechanicsActivity.this, AdminCustomersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navMechanics != null) {
            navMechanics.setOnClickListener(v -> {});
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMechanics(searchEditText.getText().toString().trim());
    }

    private void loadMechanics(String query) {
        mechanicsContainer.removeAllViews();
        boolean hasMechanics = false;

        Cursor cursor = databaseHelper.getAllMechanics(query);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasMechanics = true;
                final int mechanicId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));

                View mechView = LayoutInflater.from(this).inflate(R.layout.item_mechanic_customer, mechanicsContainer, false);
                TextView tvName = mechView.findViewById(R.id.tvCustomerName);
                TextView tvMobile = mechView.findViewById(R.id.tvCustomerMobile);
                TextView tvEmail = mechView.findViewById(R.id.tvCustomerEmail);
                LinearLayout layoutEmail = mechView.findViewById(R.id.layoutEmailContainer);
                TextView tvVehicleChip = mechView.findViewById(R.id.tvVehicleChip);
                android.widget.Button btnViewDetails = mechView.findViewById(R.id.btnViewDetails);

                tvName.setText(name);
                tvMobile.setText(phone);
                tvEmail.setText(email + " • City: " + city);
                layoutEmail.setVisibility(View.VISIBLE);
                tvVehicleChip.setText("Mechanic");
                btnViewDetails.setVisibility(View.VISIBLE);

                mechView.setOnClickListener(null); // Disable full card click
                mechView.setClickable(false);

                btnViewDetails.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminMechanicsActivity.this, AdminMechanicDetailsActivity.class);
                    intent.putExtra("mechanic_id", mechanicId);
                    startActivity(intent);
                });

                mechanicsContainer.addView(mechView);
            }
            cursor.close();
        }

        if (hasMechanics) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
