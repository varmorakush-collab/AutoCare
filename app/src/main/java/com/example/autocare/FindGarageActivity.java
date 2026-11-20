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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FindGarageActivity extends AppCompatActivity {

    private ImageView backImageView;
    private Button btnMyRequestsHeader;
    private EditText searchEditText;
    private LinearLayout garagesContainer;
    private TextView emptyStateTextView;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_garage);

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
        btnMyRequestsHeader = findViewById(R.id.btnMyRequestsHeader);
        searchEditText = findViewById(R.id.searchEditText);
        garagesContainer = findViewById(R.id.garagesContainer);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        backImageView.setOnClickListener(v -> finish());

        btnMyRequestsHeader.setOnClickListener(v -> {
            Intent intent = new Intent(FindGarageActivity.this, MyGarageRequestsActivity.class);
            startActivity(intent);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadGarages(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String garageName = cursor.getString(cursor.getColumnIndexOrThrow("garage_name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                View cardView = LayoutInflater.from(this).inflate(R.layout.item_garage_card, garagesContainer, false);
                TextView tvGarageName = cardView.findViewById(R.id.tvGarageName);
                TextView tvConnectionBadge = cardView.findViewById(R.id.tvConnectionBadge);
                TextView tvOwnerName = cardView.findViewById(R.id.tvOwnerName);
                TextView tvGarageAddress = cardView.findViewById(R.id.tvGarageAddress);
                TextView tvGarageContact = cardView.findViewById(R.id.tvGarageContact);
                Button btnRequestToJoin = cardView.findViewById(R.id.btnRequestToJoin);

                tvGarageName.setText(garageName);
                tvOwnerName.setText("Owner: " + fullName);
                tvGarageAddress.setText("📍 " + address);
                tvGarageContact.setText("📞 " + phone + " | ✉️ " + email);

                String status = databaseHelper.getGarageRequestStatus(loggedInMechanicId, garageId);

                if ("CONNECTED".equalsIgnoreCase(status)) {
                    tvConnectionBadge.setText("Connected");
                    tvConnectionBadge.setBackgroundColor(0x3364FFDA);
                    tvConnectionBadge.setTextColor(getResources().getColor(R.color.splash_accent_teal));
                    btnRequestToJoin.setText("Connected");
                    btnRequestToJoin.setEnabled(false);
                    btnRequestToJoin.setAlpha(0.6f);
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    tvConnectionBadge.setText("Request Pending");
                    tvConnectionBadge.setBackgroundColor(0x33FF9800);
                    tvConnectionBadge.setTextColor(0xFFFF9800);
                    btnRequestToJoin.setText("Request Pending");
                    btnRequestToJoin.setEnabled(false);
                    btnRequestToJoin.setAlpha(0.6f);
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    tvConnectionBadge.setText("Request Rejected");
                    tvConnectionBadge.setBackgroundColor(0x33FF5555);
                    tvConnectionBadge.setTextColor(0xFFFF5555);
                    btnRequestToJoin.setText("Request Again");
                    btnRequestToJoin.setEnabled(true);
                    btnRequestToJoin.setAlpha(1.0f);
                } else {
                    tvConnectionBadge.setText("Not Connected");
                    tvConnectionBadge.setBackgroundColor(0x1AFFFFFF);
                    tvConnectionBadge.setTextColor(getResources().getColor(R.color.splash_text_secondary));
                    btnRequestToJoin.setText("Request to Join");
                    btnRequestToJoin.setEnabled(true);
                    btnRequestToJoin.setAlpha(1.0f);
                }

                btnRequestToJoin.setOnClickListener(v -> {
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    boolean sent = databaseHelper.sendGarageRequest(loggedInMechanicId, garageId, currentDate);
                    if (sent) {
                        Toast.makeText(this, "Connection request sent to " + garageName, Toast.LENGTH_SHORT).show();
                        loadGarages(searchEditText.getText().toString().trim());
                    } else {
                        Toast.makeText(this, "You already have an active or pending request for this garage.", Toast.LENGTH_SHORT).show();
                    }
                });

                garagesContainer.addView(cardView);
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