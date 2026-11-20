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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomerWarrantyActivity extends AppCompatActivity {

    private ImageView backImageView;
    private LinearLayout warrantiesContainer;
    private TextView emptyStateTextView;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int customerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_warranty);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        BottomNavHelper.setupBottomNav(this, R.id.navWarranty);

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail == null || !"Customer".equals(userRole)) {
            Intent intent = new Intent(CustomerWarrantyActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        warrantiesContainer = findViewById(R.id.warrantiesContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());
        
        android.widget.EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadWarranties(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Bottom Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navVehicles = findViewById(R.id.navVehicles);
        LinearLayout navWarranty = findViewById(R.id.navWarranty);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerWarrantyActivity.this, CustomerDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navVehicles != null) {
            navVehicles.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerWarrantyActivity.this, CustomerVehiclesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navWarranty != null) {
            navWarranty.setOnClickListener(v -> {
                // Already here
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(CustomerWarrantyActivity.this, CustomerProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWarranties("");
    }

    private void loadWarranties(String query) {
        Cursor cCustomer = databaseHelper.getCustomerDetailsByEmail(userEmail);
        if (cCustomer != null && cCustomer.moveToFirst()) {
            customerId = cCustomer.getInt(cCustomer.getColumnIndexOrThrow("id"));
            cCustomer.close();
        } else {
            if (cCustomer != null) cCustomer.close();
            Toast.makeText(this, "Session error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        warrantiesContainer.removeAllViews();
        boolean hasWarranties = false;

        Cursor cursor = databaseHelper.getCustomerWarranties(customerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String partName = cursor.getString(cursor.getColumnIndexOrThrow("part_name"));
                String partBrand = cursor.getString(cursor.getColumnIndexOrThrow("part_brand"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
                String wUnit = cursor.getString(cursor.getColumnIndexOrThrow("warranty_unit"));
                String wExpiry = cursor.getString(cursor.getColumnIndexOrThrow("warranty_expiry"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));

                String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
                String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
                String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));

                if (query != null && !query.isEmpty()) {
                    String combined = (partName + " " + partBrand + " " + brand + " " + model + " " + regNumber).toLowerCase(java.util.Locale.US);
                    if (!combined.contains(query.toLowerCase(java.util.Locale.US))) {
                        continue;
                    }
                }

                hasWarranties = true;

                View partView = LayoutInflater.from(this).inflate(R.layout.item_replaced_part, warrantiesContainer, false);
                TextView tvPartName = partView.findViewById(R.id.tvPartName);
                TextView tvPartTotalCost = partView.findViewById(R.id.tvPartTotalCost);
                TextView tvPartDetails = partView.findViewById(R.id.tvPartDetails);
                TextView tvPartWarranty = partView.findViewById(R.id.tvPartWarranty);
                TextView tvPartWarrantyStatus = partView.findViewById(R.id.tvPartWarrantyStatus);
                Button btnDeletePart = partView.findViewById(R.id.btnDeletePart);

                btnDeletePart.setVisibility(View.GONE);

                tvPartName.setText(partName + " (" + brand + " " + model + ")");
                tvPartTotalCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                String details = "Vehicle Reg: " + regNumber + " | Brand: " + partBrand + " | Qty: " + quantity;
                if (notes != null && !notes.isEmpty()) {
                    details += "\nNotes: " + notes;
                }
                tvPartDetails.setText(details);

                if (wUnit == null || wUnit.equals("No Warranty") || wUnit.isEmpty()) {
                    tvPartWarranty.setText("No Warranty Specified");
                    tvPartWarrantyStatus.setVisibility(View.GONE);
                } else {
                    tvPartWarranty.setText("Warranty Expiry: " + wExpiry);
                    tvPartWarrantyStatus.setVisibility(View.VISIBLE);
                    calculateWarrantyStatus(wExpiry, tvPartWarrantyStatus);
                }

                warrantiesContainer.addView(partView);
            }
            cursor.close();
        }

        if (hasWarranties) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }

    private void calculateWarrantyStatus(String expiryDateStr, TextView tvStatus) {
        if (expiryDateStr == null || expiryDateStr.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date expiryDate = sdf.parse(expiryDateStr);

            Calendar expiryCal = Calendar.getInstance();
            if (expiryDate != null) expiryCal.setTime(expiryDate);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            if (expiryCal.before(today)) {
                tvStatus.setText("Expired");
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF2F2")));
                tvStatus.setTextColor(android.graphics.Color.parseColor("#DC2626"));
            } else {
                tvStatus.setText("Active");
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                tvStatus.setTextColor(android.graphics.Color.parseColor("#15803D"));
            }
        } catch (ParseException e) {
            tvStatus.setText("Error");
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9")));
            tvStatus.setTextColor(android.graphics.Color.parseColor("#0F172A"));
        }
    }
}