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

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomerServiceDetailsActivity extends AppCompatActivity implements PaymentResultListener {

    private ImageView backImageView;
    private TextView tvVehicleSummary, tvVehicleRegSummary;
    private TextView tvServiceType, tvServiceStatus, tvPaymentStatus, tvServiceDate;
    private TextView tvComplaint, tvWorkDone, lblNotes, tvNotes, tvCostBreakdown, tvRazorpayPaymentId;
    private Button btnPayOnline, btnPayCash;
    private LinearLayout partsContainer;
    private TextView emptyPartsTextView;

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private String customerMobile = "";
    private int customerId = -1;
    private int serviceId = -1;
    private double currentTotalCost = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        serviceId = getIntent().getIntExtra("service_id", -1);

        if (userEmail == null || !"Customer".equals(userRole) || serviceId == -1) {
            Intent intent = new Intent(CustomerServiceDetailsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvVehicleSummary = findViewById(R.id.tvVehicleSummary);
        tvVehicleRegSummary = findViewById(R.id.tvVehicleRegSummary);

        tvServiceType = findViewById(R.id.tvServiceType);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvServiceDate = findViewById(R.id.tvServiceDate);

        tvComplaint = findViewById(R.id.tvComplaint);
        tvWorkDone = findViewById(R.id.tvWorkDone);
        lblNotes = findViewById(R.id.lblNotes);
        tvNotes = findViewById(R.id.tvNotes);
        tvCostBreakdown = findViewById(R.id.tvCostBreakdown);
        tvRazorpayPaymentId = findViewById(R.id.tvRazorpayPaymentId);

        btnPayOnline = findViewById(R.id.btnPayOnline);
        btnPayCash = findViewById(R.id.btnPayCash);
        partsContainer = findViewById(R.id.partsContainer);
        emptyPartsTextView = findViewById(R.id.emptyPartsTextView);

        // Preload Razorpay Checkout
        try {
            Checkout.preload(getApplicationContext());
        } catch (Exception ignored) {}

        backImageView.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServiceDetails();
    }

    private void loadServiceDetails() {
        Cursor cCustomer = databaseHelper.getCustomerDetailsByEmail(userEmail);
        if (cCustomer != null && cCustomer.moveToFirst()) {
            customerId = cCustomer.getInt(cCustomer.getColumnIndexOrThrow("id"));
            int phoneIdx = cCustomer.getColumnIndex("mobile");
            if (phoneIdx != -1 && !cCustomer.isNull(phoneIdx)) {
                customerMobile = cCustomer.getString(phoneIdx);
            }
            cCustomer.close();
        } else {
            if (cCustomer != null) cCustomer.close();
            Toast.makeText(this, "Session error.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = databaseHelper.getServiceDetailsForCustomer(serviceId, customerId);
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

            currentTotalCost = totalCost;

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

            tvPaymentStatus.setText("Pay: " + paymentStatus.toUpperCase(Locale.US));
            if ("Paid".equalsIgnoreCase(paymentStatus)) {
                tvPaymentStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#15803D"));
                
                btnPayOnline.setText("✓ Paid (Payment Received)");
                btnPayOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#16A34A")));
                btnPayOnline.setEnabled(false);
                
                btnPayCash.setText("✓ Cash Paid & Confirmed");
                btnPayCash.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#16A34A")));
                btnPayCash.setEnabled(false);

                if (razorpayId != null && !razorpayId.isEmpty()) {
                    tvRazorpayPaymentId.setVisibility(View.VISIBLE);
                    tvRazorpayPaymentId.setText("Razorpay Payment ID: " + razorpayId);
                } else {
                    tvRazorpayPaymentId.setVisibility(View.GONE);
                }
            } else if ("Cash Pending".equalsIgnoreCase(paymentStatus) || "Cash Requested".equalsIgnoreCase(paymentStatus)) {
                tvPaymentStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7")));
                tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#D97706"));
                tvPaymentStatus.setText("Pay: CASH PENDING");

                btnPayOnline.setText("Pay Online (Razorpay) - ₹ " + String.format(Locale.US, "%.2f", totalCost));
                btnPayOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0D9488")));
                btnPayOnline.setEnabled(true);
                btnPayOnline.setOnClickListener(v -> startRazorpayPayment(currentTotalCost));

                btnPayCash.setText("⏳ Cash Payment Requested (Awaiting Mechanic)");
                btnPayCash.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D97706")));
                btnPayCash.setEnabled(false);

                tvRazorpayPaymentId.setVisibility(View.GONE);
            } else {
                tvPaymentStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF3C7")));
                tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#D97706"));
                
                btnPayOnline.setText("Pay Online (Razorpay) - ₹ " + String.format(Locale.US, "%.2f", totalCost));
                btnPayOnline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0D9488")));
                btnPayOnline.setEnabled(true);
                btnPayOnline.setOnClickListener(v -> startRazorpayPayment(currentTotalCost));

                btnPayCash.setText("Pay by Cash (Pay at Garage)");
                btnPayCash.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D97706")));
                btnPayCash.setEnabled(true);
                btnPayCash.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Pay by Cash")
                            .setMessage("Are you sure you want to request cash payment at the garage? Your mechanic will confirm once cash is received.")
                            .setPositiveButton("Request Cash Payment", (dialog, which) -> {
                                boolean updated = databaseHelper.updateServicePaymentStatus(serviceId, "Cash Pending", null);
                                if (updated) {
                                    Toast.makeText(this, "Cash payment requested. Please pay cash at the garage.", Toast.LENGTH_LONG).show();
                                    loadServiceDetails();
                                } else {
                                    Toast.makeText(this, "Failed to request cash payment.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                tvRazorpayPaymentId.setVisibility(View.GONE);
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

    private void startRazorpayPayment(double amountInRupees) {
        if (amountInRupees <= 0) {
            Toast.makeText(this, "Bill amount is ₹ 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        Checkout checkout = new Checkout();
        checkout.setKeyID(RazorpayConfig.RAZORPAY_KEY_ID);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "AutoCare Garage Services");
            options.put("description", "Bill Payment for Service #" + serviceId);
            options.put("theme.color", "#0D9488");
            options.put("currency", "INR");
            options.put("amount", Math.round(amountInRupees * 100)); // Amount in paise

            JSONObject prefill = new JSONObject();
            if (userEmail != null && !userEmail.isEmpty()) {
                prefill.put("email", userEmail);
            }
            if (customerMobile != null && !customerMobile.isEmpty()) {
                prefill.put("contact", customerMobile);
            }
            options.put("prefill", prefill);

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting Razorpay checkout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        boolean updated = databaseHelper.updateServicePaymentStatus(serviceId, "Paid", razorpayPaymentID);
        if (updated) {
            Toast.makeText(this, "Payment Successful! Payment ID: " + razorpayPaymentID, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Payment Received! ID: " + razorpayPaymentID, Toast.LENGTH_LONG).show();
        }
        loadServiceDetails();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Cancelled or Failed.", Toast.LENGTH_SHORT).show();
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