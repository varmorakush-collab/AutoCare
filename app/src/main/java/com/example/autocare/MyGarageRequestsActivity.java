package com.example.autocare;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MyGarageRequestsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private LinearLayout requestsContainer;
    private TextView emptyStateTextView;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garage_requests);

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
        requestsContainer = findViewById(R.id.requestsContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private void loadRequests() {
        requestsContainer.removeAllViews();
        boolean hasRequests = false;

        Cursor cursor = databaseHelper.getMechanicRequests(loggedInMechanicId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasRequests = true;
                final int requestId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String requestDate = cursor.getString(cursor.getColumnIndexOrThrow("request_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                int isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active"));

                String garageName = cursor.getString(cursor.getColumnIndexOrThrow("garage_name"));
                String ownerName = cursor.getString(cursor.getColumnIndexOrThrow("owner_name"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                View cardView = LayoutInflater.from(this).inflate(R.layout.item_mechanic_request_card, requestsContainer, false);
                TextView tvGarageName = cardView.findViewById(R.id.tvGarageName);
                TextView tvStatusBadge = cardView.findViewById(R.id.tvStatusBadge);
                TextView tvOwnerName = cardView.findViewById(R.id.tvOwnerName);
                TextView tvGarageAddress = cardView.findViewById(R.id.tvGarageAddress);
                TextView tvRequestDate = cardView.findViewById(R.id.tvRequestDate);
                Button btnCancelRequest = cardView.findViewById(R.id.btnCancelRequest);

                tvGarageName.setText(garageName);
                tvOwnerName.setText("Owner: " + ownerName);
                tvGarageAddress.setText("📍 " + address);
                tvRequestDate.setText("📅 Request Date: " + requestDate);

                if ("APPROVED".equalsIgnoreCase(status) && isActive == 1) {
                    tvStatusBadge.setText("Connected");
                    tvStatusBadge.setBackgroundColor(0x3364FFDA);
                    tvStatusBadge.setTextColor(getResources().getColor(R.color.splash_accent_teal));
                    btnCancelRequest.setVisibility(View.GONE);
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    tvStatusBadge.setText("Pending Approval");
                    tvStatusBadge.setBackgroundColor(0x33FF9800);
                    tvStatusBadge.setTextColor(0xFFFF9800);
                    btnCancelRequest.setVisibility(View.VISIBLE);
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    tvStatusBadge.setText("Rejected");
                    tvStatusBadge.setBackgroundColor(0x33FF5555);
                    tvStatusBadge.setTextColor(0xFFFF5555);
                    btnCancelRequest.setVisibility(View.GONE);
                } else {
                    tvStatusBadge.setText("Cancelled");
                    tvStatusBadge.setBackgroundColor(0x1AFFFFFF);
                    tvStatusBadge.setTextColor(getResources().getColor(R.color.splash_text_secondary));
                    btnCancelRequest.setVisibility(View.GONE);
                }

                btnCancelRequest.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Cancel Request")
                            .setMessage("Are you sure you want to cancel your request to join " + garageName + "?")
                            .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                                boolean cancelled = databaseHelper.cancelGarageRequest(requestId, loggedInMechanicId);
                                if (cancelled) {
                                    Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show();
                                    loadRequests();
                                } else {
                                    Toast.makeText(this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                });

                requestsContainer.addView(cardView);
            }
            cursor.close();
        }

        if (hasRequests) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}