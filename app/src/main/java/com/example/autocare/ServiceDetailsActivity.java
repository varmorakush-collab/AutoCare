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

public class ServiceDetailsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvVehicleDetailsSummary, tvCustomerNameSummary, tvServiceStatsSummary;
    private Button btnAddServiceHeader;
    private LinearLayout servicesContainer;
    private TextView emptyServicesTextView;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int vehicleId = -1;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        userRole = prefs.getString("user_role", null);

        if (userEmail != null && "Mechanic".equals(userRole)) {
            loggedInMechanicId = databaseHelper.getMechanicId(userEmail);
        }

        vehicleId = getIntent().getIntExtra("vehicle_id", -1);

        if (userEmail == null || vehicleId == -1) {
            Toast.makeText(this, "Session or vehicle data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if ("Mechanic".equals(userRole) && loggedInMechanicId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvVehicleDetailsSummary = findViewById(R.id.tvVehicleDetailsSummary);
        tvCustomerNameSummary = findViewById(R.id.tvCustomerNameSummary);
        tvServiceStatsSummary = findViewById(R.id.tvServiceStatsSummary);
        btnAddServiceHeader = findViewById(R.id.btnAddServiceHeader);
        servicesContainer = findViewById(R.id.servicesContainer);
        emptyServicesTextView = findViewById(R.id.emptyServicesTextView);

        backImageView.setOnClickListener(v -> finish());

        btnAddServiceHeader.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceDetailsActivity.this, AddServiceActivity.class);
            intent.putExtra("vehicle_id", vehicleId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicleData();
        loadServices();
    }

    private void loadVehicleData() {
        Cursor cursor = databaseHelper.getVehicleAndCustomerDetails(vehicleId);
        if (cursor != null && cursor.moveToFirst()) {
            String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
            String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
            String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            int vMechanicId = cursor.getInt(cursor.getColumnIndexOrThrow("mechanic_id"));

            if ("Mechanic".equals(userRole) && loggedInMechanicId != -1 && vMechanicId != loggedInMechanicId) {
                Toast.makeText(this, "Permission denied. You do not own this vehicle.", Toast.LENGTH_SHORT).show();
                cursor.close();
                finish();
                return;
            }

            tvVehicleDetailsSummary.setText(brand + " " + model + " (" + regNumber + ")");
            tvCustomerNameSummary.setText("Owner: " + customerName);
            cursor.close();
        } else {
            Toast.makeText(this, "Vehicle details not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void loadServices() {
        servicesContainer.removeAllViews();
        boolean hasServices = false;
        int totalServicesCount = 0;
        double overallSpent = 0.0;

        Cursor cursor = databaseHelper.getServicesByVehicleId(vehicleId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasServices = true;
                totalServicesCount++;

                final int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
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

                String paymentStatus = "Pending";
                int payIdx = cursor.getColumnIndex("payment_status");
                if (payIdx != -1 && !cursor.isNull(payIdx)) {
                    paymentStatus = cursor.getString(payIdx);
                }

                String razorpayId = null;
                int rzpIdx = cursor.getColumnIndex("razorpay_payment_id");
                if (rzpIdx != -1 && !cursor.isNull(rzpIdx)) {
                    razorpayId = cursor.getString(rzpIdx);
                }

                overallSpent += totalCost;

                View serviceCard = LayoutInflater.from(this).inflate(R.layout.item_service_detail_card, servicesContainer, false);
                TextView tvServiceDate = serviceCard.findViewById(R.id.tvServiceDate);
                TextView tvServiceStatus = serviceCard.findViewById(R.id.tvServiceStatus);
                TextView tvPaymentStatus = serviceCard.findViewById(R.id.tvPaymentStatus);
                TextView tvServiceType = serviceCard.findViewById(R.id.tvServiceType);
                TextView tvTotalCost = serviceCard.findViewById(R.id.tvTotalCost);
                TextView tvComplaint = serviceCard.findViewById(R.id.tvComplaint);
                TextView tvWorkDone = serviceCard.findViewById(R.id.tvWorkDone);
                TextView lblNotes = serviceCard.findViewById(R.id.lblNotes);
                TextView tvNotes = serviceCard.findViewById(R.id.tvNotes);
                
                TextView tvLabourCost = serviceCard.findViewById(R.id.tvLabourCost);
                TextView tvPartsCost = serviceCard.findViewById(R.id.tvPartsCost);
                TextView tvOtherCost = serviceCard.findViewById(R.id.tvOtherCost);
                TextView tvDiscount = serviceCard.findViewById(R.id.tvDiscount);
                TextView tvBreakdownTotal = serviceCard.findViewById(R.id.tvBreakdownTotal);

                LinearLayout partsContainer = serviceCard.findViewById(R.id.partsContainer);
                TextView emptyPartsTextView = serviceCard.findViewById(R.id.emptyPartsTextView);
                Button btnAddPart = serviceCard.findViewById(R.id.btnAddPart);
                Button btnDeleteService = serviceCard.findViewById(R.id.btnDeleteService);
                Button btnConfirmCash = serviceCard.findViewById(R.id.btnConfirmCash);

                tvPaymentStatus.setText("Pay: " + paymentStatus);
                if ("Paid".equalsIgnoreCase(paymentStatus)) {
                    tvPaymentStatus.setBackgroundColor(0x3300A86B);
                    tvPaymentStatus.setTextColor(0xFF00A86B);
                    btnConfirmCash.setVisibility(View.GONE);
                } else if ("Cash Pending".equalsIgnoreCase(paymentStatus) || "Cash Requested".equalsIgnoreCase(paymentStatus)) {
                    tvPaymentStatus.setBackgroundColor(0xFFFEF3C7);
                    tvPaymentStatus.setTextColor(0xFFD97706);
                    btnConfirmCash.setVisibility(View.VISIBLE);
                    btnConfirmCash.setText("✓ Confirm Cash Received (Mark Paid)");
                    btnConfirmCash.setOnClickListener(v -> {
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Confirm Cash Payment")
                                .setMessage("Do you confirm that you have received cash payment for Service #" + serviceId + "?")
                                .setPositiveButton("Confirm Paid", (dialog, which) -> {
                                    boolean updated = databaseHelper.updateServicePaymentStatus(serviceId, "Paid", null);
                                    if (updated) {
                                        Toast.makeText(this, "Cash payment confirmed and marked as Paid!", Toast.LENGTH_SHORT).show();
                                        loadServices();
                                    } else {
                                        Toast.makeText(this, "Failed to confirm payment", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });
                } else {
                    tvPaymentStatus.setBackgroundColor(0xFFFEF3C7);
                    tvPaymentStatus.setTextColor(0xFFD97706);
                    btnConfirmCash.setVisibility(View.GONE);
                }

                tvServiceType.setText(type);
                tvTotalCost.setText(String.format(Locale.US, "₹ %.2f", totalCost));
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

                tvLabourCost.setText(String.format(Locale.US, "₹ %.2f", labour));
                tvPartsCost.setText(String.format(Locale.US, "₹ %.2f", partsCost));
                tvOtherCost.setText(String.format(Locale.US, "₹ %.2f", other));
                tvDiscount.setText(String.format(Locale.US, "-₹ %.2f", discount));
                tvBreakdownTotal.setText(String.format(Locale.US, "₹ %.2f", totalCost));

                // Load replaced parts for this service
                loadReplacedPartsForService(serviceId, partsContainer, emptyPartsTextView);

                Button btnCompleteService = serviceCard.findViewById(R.id.btnCompleteService);
                if (!"Completed".equalsIgnoreCase(status)) {
                    btnCompleteService.setVisibility(View.VISIBLE);
                    btnCompleteService.setOnClickListener(v -> {
                        boolean updated = databaseHelper.updateServiceStatus(serviceId, "Completed");
                        if (updated) {
                            Toast.makeText(this, "Service marked as completed!", Toast.LENGTH_SHORT).show();
                            loadServices();
                        } else {
                            Toast.makeText(this, "Failed to update service status", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    btnCompleteService.setVisibility(View.GONE);
                }

                btnAddPart.setOnClickListener(v -> {
                    Intent intent = new Intent(ServiceDetailsActivity.this, AddReplacedPartActivity.class);
                    intent.putExtra("service_id", serviceId);
                    startActivity(intent);
                });

                btnDeleteService.setOnClickListener(v -> {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Delete Service");
                    builder.setMessage("Are you sure you want to delete this service record and its replaced parts?");
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = databaseHelper.deleteService(serviceId);
                        if (deleted) {
                            Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show();
                            loadServices();
                        } else {
                            Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                });

                servicesContainer.addView(serviceCard);
            }
            cursor.close();
        }

        tvServiceStatsSummary.setText(String.format(Locale.US, "Total Services: %d | Total Spent: ₹ %.2f", totalServicesCount, overallSpent));

        if (hasServices) {
            emptyServicesTextView.setVisibility(View.GONE);
        } else {
            emptyServicesTextView.setVisibility(View.VISIBLE);
        }
    }

    private void loadReplacedPartsForService(int serviceId, LinearLayout partsContainer, TextView emptyPartsTextView) {
        partsContainer.removeAllViews();
        boolean hasParts = false;

        Cursor cursor = databaseHelper.getReplacedPartsByServiceId(serviceId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasParts = true;
                final int partId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
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

                btnDeletePart.setOnClickListener(v -> {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Delete Part");
                    builder.setMessage("Are you sure you want to delete " + partName + "?");
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = databaseHelper.deleteReplacedPart(partId);
                        if (deleted) {
                            Toast.makeText(this, "Part deleted", Toast.LENGTH_SHORT).show();
                            recalculateServiceCosts(serviceId);
                            loadServices();
                        } else {
                            Toast.makeText(this, "Failed to delete part", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                });

                partsContainer.addView(partView);
            }
            cursor.close();
        }

        if (hasParts) {
            emptyPartsTextView.setVisibility(View.GONE);
        } else {
            emptyPartsTextView.setVisibility(View.VISIBLE);
        }
    }

    private void recalculateServiceCosts(int serviceId) {
        Cursor cParts = databaseHelper.getReplacedPartsByServiceId(serviceId);
        double partsTotal = 0;
        if (cParts != null) {
            while (cParts.moveToNext()) {
                partsTotal += cParts.getDouble(cParts.getColumnIndexOrThrow("total_cost"));
            }
            cParts.close();
        }

        Cursor cService = databaseHelper.getServiceById(serviceId);
        if (cService != null && cService.moveToFirst()) {
            double labour = cService.getDouble(cService.getColumnIndexOrThrow("labour_charges"));
            double other = cService.getDouble(cService.getColumnIndexOrThrow("other_charges"));
            double discount = cService.getDouble(cService.getColumnIndexOrThrow("discount"));
            cService.close();

            double newTotal = labour + other + partsTotal - discount;
            databaseHelper.updateServiceTotals(serviceId, partsTotal, newTotal);
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
