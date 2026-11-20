package com.example.autocare;

import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

public class AdminGaragesActivity extends AppCompatActivity {

    private ImageView backImageView, btnClearSearch;
    private EditText searchEditText;
    private LinearLayout garagesContainer;
    private TextView emptyStateTextView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_garages);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavHelper.setupBottomNav(this, R.id.navGarages);

        databaseHelper = new DatabaseHelper(this);

        backImageView = findViewById(R.id.backImageView);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        searchEditText = findViewById(R.id.searchEditText);
        garagesContainer = findViewById(R.id.garagesContainer);
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
                loadGarages(s.toString().trim());
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
                Intent intent = new Intent(AdminGaragesActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navGarages != null) {
            navGarages.setOnClickListener(v -> {});
        }
        if (navCustomers != null) {
            navCustomers.setOnClickListener(v -> {
                Intent intent = new Intent(AdminGaragesActivity.this, AdminCustomersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navMechanics != null) {
            navMechanics.setOnClickListener(v -> {
                Intent intent = new Intent(AdminGaragesActivity.this, AdminMechanicsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGarages(searchEditText.getText().toString().trim());
    }

    private void loadGarages(String query) {
        garagesContainer.removeAllViews();
        boolean hasGarages = false;

        Cursor cursor = databaseHelper.getAllGarages(query);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasGarages = true;
                final int garageId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String garageName = cursor.getString(cursor.getColumnIndexOrThrow("garage_name"));
                String ownerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                View garageView = LayoutInflater.from(this).inflate(R.layout.item_garage_card, garagesContainer, false);
                TextView tvGarageName = garageView.findViewById(R.id.tvGarageName);
                TextView tvOwnerName = garageView.findViewById(R.id.tvOwnerName);
                TextView tvGarageAddress = garageView.findViewById(R.id.tvGarageAddress);
                TextView tvGarageContact = garageView.findViewById(R.id.tvGarageContact);
                TextView tvConnectionBadge = garageView.findViewById(R.id.tvConnectionBadge);
                Button btnRequestToJoin = garageView.findViewById(R.id.btnRequestToJoin);
                Button btnViewDetails = garageView.findViewById(R.id.btnViewDetails);

                tvGarageName.setText(garageName);
                tvOwnerName.setText("Owner: " + ownerName);
                tvGarageAddress.setText("Address: " + address);
                tvGarageContact.setText("Email: " + email + " | Phone: " + phone);
                tvConnectionBadge.setText("Active Garage");
                btnRequestToJoin.setVisibility(View.GONE);
                btnViewDetails.setVisibility(View.VISIBLE);

                garageView.setOnClickListener(null); // Disable full card click
                garageView.setClickable(false);

                btnViewDetails.setOnClickListener(v -> {
                    Intent intent = new Intent(AdminGaragesActivity.this, AdminGarageDetailsActivity.class);
                    intent.putExtra("garage_id", garageId);
                    startActivity(intent);
                });

                garagesContainer.addView(garageView);
            }
            cursor.close();
        }

        if (hasGarages) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
