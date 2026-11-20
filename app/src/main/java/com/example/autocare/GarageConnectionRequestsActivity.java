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

public class GarageConnectionRequestsActivity extends AppCompatActivity {

    private ImageView backImageView;
    private LinearLayout requestsContainer;
    private TextView emptyStateTextView, tvPendingCountHeader;

    private DatabaseHelper databaseHelper;
    private int garageOwnerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_connection_requests);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail != null && "Garage Owner".equals(userRole)) {
            garageOwnerId = databaseHelper.getGarageOwnerId(userEmail);
        }

        if (garageOwnerId == -1) {
            Toast.makeText(this, "Session error. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        requestsContainer = findViewById(R.id.requestsContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        tvPendingCountHeader = findViewById(R.id.tvPendingCountHeader);

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
        int pendingCount = 0;

        Cursor cursor = databaseHelper.getRequestsForGarageOwner(garageOwnerId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasRequests = true;
                final int requestId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                final String mechanicName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                String requestDate = cursor.getString(cursor.getColumnIndexOrThrow("request_date"));

                pendingCount++;

                View cardView = LayoutInflater.from(this).inflate(R.layout.item_owner_request_card, requestsContainer, false);
                TextView tvMechanicName = cardView.findViewById(R.id.tvMechanicName);
                TextView tvStatusBadge = cardView.findViewById(R.id.tvStatusBadge);
                TextView tvMechanicPhone = cardView.findViewById(R.id.tvMechanicPhone);
                TextView tvMechanicEmail = cardView.findViewById(R.id.tvMechanicEmail);
                TextView tvMechanicCity = cardView.findViewById(R.id.tvMechanicCity);
                TextView tvRequestDate = cardView.findViewById(R.id.tvRequestDate);
                Button btnApprove = cardView.findViewById(R.id.btnApprove);
                Button btnReject = cardView.findViewById(R.id.btnReject);

                tvMechanicName.setText(mechanicName);
                tvStatusBadge.setText("Pending Approval");
                tvStatusBadge.setBackgroundColor(0xFFFEF3C7);
                tvStatusBadge.setTextColor(0xFFD97706);

                tvMechanicPhone.setText(phone != null && !phone.isEmpty() ? phone : "Not specified");
                tvMechanicEmail.setText(email != null && !email.isEmpty() ? email : "No email");
                tvMechanicCity.setText(city != null && !city.isEmpty() ? city : "Not specified");
                tvRequestDate.setText("Received on " + requestDate);

                btnApprove.setOnClickListener(v -> new AlertDialog.Builder(this)
                        .setTitle("Approve Mechanic")
                        .setMessage("Are you sure you want to approve " + mechanicName + " to join your garage?")
                        .setPositiveButton("Approve", (dialog, which) -> {
                            boolean approved = databaseHelper.updateRequestStatus(requestId, garageOwnerId, "APPROVED", 1);
                            if (approved) {
                                Toast.makeText(this, mechanicName + " approved and connected!", Toast.LENGTH_SHORT).show();
                                loadRequests();
                            } else {
                                Toast.makeText(this, "Failed to approve request.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

                btnReject.setOnClickListener(v -> new AlertDialog.Builder(this)
                        .setTitle("Reject Request")
                        .setMessage("Are you sure you want to reject " + mechanicName + "'s request?")
                        .setPositiveButton("Reject", (dialog, which) -> {
                            boolean rejected = databaseHelper.updateRequestStatus(requestId, garageOwnerId, "REJECTED", 0);
                            if (rejected) {
                                Toast.makeText(this, "Request rejected.", Toast.LENGTH_SHORT).show();
                                loadRequests();
                            } else {
                                Toast.makeText(this, "Failed to reject request.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

                requestsContainer.addView(cardView);
            }
            cursor.close();
        }

        if (pendingCount > 0) {
            tvPendingCountHeader.setText(pendingCount + (pendingCount == 1 ? " Pending Request" : " Pending Requests"));
            tvPendingCountHeader.setVisibility(View.VISIBLE);
        } else {
            tvPendingCountHeader.setVisibility(View.GONE);
        }

        if (hasRequests) {
            emptyStateTextView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
    }
}
