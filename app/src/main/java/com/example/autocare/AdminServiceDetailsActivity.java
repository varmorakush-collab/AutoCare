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

public class AdminServiceDetailsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvVehicleSummary, tvVehicleRegSummary;
    private TextView tvServiceType, tvServiceStatus, tvServiceDate;
    private TextView tvComplaint, tvWorkDone, lblNotes, tvNotes, tvCostBreakdown;
    private LinearLayout partsContainer;
    private TextView emptyPartsTextView;

    private DatabaseHelper databaseHelper;
    private int serviceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_service_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        serviceId = getIntent().getIntExtra("SERVICE_ID", -1);

        if (serviceId == -1) {
            Toast.makeText(this, "Service ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvVehicleSummary = findViewById(R.id.tvVehicleSummary);
        tvVehicleRegSummary = findViewById(R.id.tvVehicleRegSummary);

        tvServiceType = findViewById(R.id.tvServiceType);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        tvServiceDate = findViewById(R.id.tvServiceDate);

        tvComplaint = findViewById(R.id.tvComplaint);
        tvWorkDone = findViewById(R.id.tvWorkDone);
        lblNotes = findViewById(R.id.lblNotes);
        tvNotes = findViewById(R.id.tvNotes);
        tvCostBreakdown = findViewById(R.id.tvCostBreakdown);

        partsContainer = findViewById(R.id.partsContainer);
        emptyPartsTextView = findViewById(R.id.emptyPartsTextView);

        backImageView.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServiceDetails();
    }

    private void loadServiceDetails() {
        Cursor cursor = databaseHelper.getServiceDetailsWithVehicle(serviceId);
        if (cursor != null && cursor.moveToFirst()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("service_type"));
            String complaint = cursor.getString(cursor.getColumnIndexOrThrow("complaint"));
            String workDone = cursor.getString(cursor.getColumnIndexOrThrow("work_done"));
            String notes = cursor.isNull(cursor.getColumnIndexOrThrow("mechanic_notes")) ? "" : cursor.getString(cursor.getColumnIndexOrThrow("mechanic_notes"));

            double labour = cursor.getDouble(cursor.getColumnIndexOrThrow("labour_charges"));
            double other = cursor.getDouble(cursor.getColumnIndexOrThrow("other_charges"));
            double discount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount"));
            double partsCost = cursor.getDouble(cursor.getColumnIndexOrThrow("parts_cost"));
            double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

            String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
            String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
            String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
            String vType = cursor.getString(cursor.getColumnIndexOrThrow("type"));

            tvVehicleSummary.setText(brand + " " + model + " (" + vType + ")");
            tvVehicleRegSummary.setText("Registration No: " + regNumber);

            tvServiceType.setText(type);
            tvServiceStatus.setText(status);

            if ("Completed".equalsIgnoreCase(status)) {
                tvServiceStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                tvServiceStatus.setTextColor(android.graphics.Color.parseColor("#15803D"));
            } else if ("Pending".equalsIgnoreCase(status)) {
                tvServiceStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7")));
                tvServiceStatus.setTextColor(android.graphics.Color.parseColor("#D97706"));
            } else {
                tvServiceStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F1F5F9")));
                tvServiceStatus.setTextColor(android.graphics.Color.parseColor("#0F172A"));
            }

            tvServiceDate.setText(date);
            tvComplaint.setText(complaint);
            tvWorkDone.setText(workDone);

            if (notes != null && !notes.trim().isEmpty()) {
                lblNotes.setVisibility(View.VISIBLE);
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText(notes);
            } else {
                lblNotes.setVisibility(View.GONE);
                tvNotes.setVisibility(View.GONE);
            }

            String costText = String.format(Locale.US, "Labour Charges : ₹ %.2f\nOther Charges  : ₹ %.2f\nParts Cost     : ₹ %.2f\nDiscount       :-₹ %.2f\n-------------------------\nFinal Total    : ₹ %.2f", labour, other, partsCost, discount, totalCost);
            tvCostBreakdown.setText(costText);

            cursor.close();
            loadReplacedParts();
        } else {
            if (cursor != null) cursor.close();
            Toast.makeText(this, "Access denied or service record not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadReplacedParts() {
        partsContainer.removeAllViews();
        boolean hasParts = false;

        Cursor cursor = databaseHelper.getReplacedPartsByServiceId(serviceId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasParts = true;
                String partName = cursor.getString(cursor.getColumnIndexOrThrow("part_name"));
                String partBrand = cursor.getString(cursor.getColumnIndexOrThrow("part_brand"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                double totalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("total_cost"));
                String wUnit = cursor.getString(cursor.getColumnIndexOrThrow("warranty_unit"));
                String wExpiry = cursor.getString(cursor.getColumnIndexOrThrow("warranty_expiry"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));

                View partView = LayoutInflater.from(this).inflate(R.layout.item_replaced_part, partsContainer, false);
                TextView tvPartName = partView.findViewById(R.id.tvPartName);
                TextView tvPartTotalCost = partView.findViewById(R.id.tvPartTotalCost);
                TextView tvPartDetails = partView.findViewById(R.id.tvPartDetails);
                TextView tvPartWarranty = partView.findViewById(R.id.tvPartWarranty);
                TextView tvPartWarrantyStatus = partView.findViewById(R.id.tvPartWarrantyStatus);
                Button btnDeletePart = partView.findViewById(R.id.btnDeletePart);

                btnDeletePart.setVisibility(View.GONE);

                tvPartName.setText(partName);
                tvPartTotalCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                String details = "Brand: " + partBrand + " | Qty: " + quantity;
                if (notes != null && !notes.isEmpty()) {
                    details += "\nNotes: " + notes;
                }
                tvPartDetails.setText(details);

                if (wUnit == null || wUnit.equals("No Warranty") || wUnit.isEmpty()) {
                    tvPartWarranty.setText("No Warranty");
                    tvPartWarrantyStatus.setVisibility(View.GONE);
                } else {
                    tvPartWarranty.setText("Warranty Expiry: " + wExpiry);
                    tvPartWarrantyStatus.setVisibility(View.VISIBLE);
                    calculateWarrantyStatus(wExpiry, tvPartWarrantyStatus);
                }

                partsContainer.addView(partView);
            }
            cursor.close();
        }

        if (hasParts) {
            emptyPartsTextView.setVisibility(View.GONE);
        } else {
            emptyPartsTextView.setText("No parts replaced");
            emptyPartsTextView.setVisibility(View.VISIBLE);
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
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else {
                tvStatus.setText("Active");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            }
        } catch (ParseException e) {
            tvStatus.setText("Error parsing date");
        }
    }
}