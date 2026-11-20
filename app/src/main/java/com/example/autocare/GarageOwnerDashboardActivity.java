package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GarageOwnerDashboardActivity extends AppCompatActivity {

    private TextView garageNameTextView;
    private TextView mechanicsCountTextView, customersCountTextView, pendingServicesTextView, completedServicesTextView;
    private LinearLayout manageMechanicsAction, connectionRequestsAction, serviceRecordsAction, garageProfileAction, customersAction;
    private View logoutImageView;
    
    private DatabaseHelper databaseHelper;
    private String userEmail;
    private int garageOwnerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_owner_dashboard);
        BottomNavHelper.setupBottomNav(this, R.id.navHome);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        garageNameTextView = findViewById(R.id.garageNameTextView);
        mechanicsCountTextView = findViewById(R.id.mechanicsCountTextView);
        customersCountTextView = findViewById(R.id.customersCountTextView);
        pendingServicesTextView = findViewById(R.id.pendingServicesTextView);
        completedServicesTextView = findViewById(R.id.completedServicesTextView);
        
        manageMechanicsAction = findViewById(R.id.manageMechanicsAction);
        connectionRequestsAction = findViewById(R.id.connectionRequestsAction);
        serviceRecordsAction = findViewById(R.id.serviceRecordsAction);
        garageProfileAction = findViewById(R.id.garageProfileAction);
        customersAction = findViewById(R.id.customersAction);
        logoutImageView = findViewById(R.id.logoutImageView);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);

        if (userEmail == null || !"Garage Owner".equals(userRole)) {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String garageName = databaseHelper.getGarageName(userEmail);
        garageNameTextView.setText(garageName);

        manageMechanicsAction.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, ConnectedMechanicsActivity.class);
            startActivity(intent);
        });

        connectionRequestsAction.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageConnectionRequestsActivity.class);
            startActivity(intent);
        });

        serviceRecordsAction.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageOwnerServicesActivity.class);
            startActivity(intent);
        });

        customersAction.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageOwnerCustomersActivity.class);
            startActivity(intent);
        });

        garageProfileAction.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageOwnerProfileActivity.class);
            startActivity(intent);
        });

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navServices = findViewById(R.id.navServices);

        navHome.setOnClickListener(v -> {
            // Already here
        });

        navMechanics.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, ConnectedMechanicsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageOwnerCustomersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navServices.setOnClickListener(v -> {
            Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageOwnerServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        LinearLayout navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(GarageOwnerDashboardActivity.this, GarageOwnerProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        logoutImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                        Intent intent = new Intent(GarageOwnerDashboardActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (userEmail == null) return;
        garageOwnerId = databaseHelper.getGarageOwnerId(userEmail);

        if (garageOwnerId != -1) {
            int mechanicCount = databaseHelper.getGarageConnectedMechanicCount(garageOwnerId);
            int pendingReqCount = databaseHelper.getGaragePendingRequestCount(garageOwnerId);

            mechanicsCountTextView.setText(String.valueOf(mechanicCount));
            customersCountTextView.setText(pendingReqCount > 0 ? pendingReqCount + " Req" : "0 Req");

            int totalServices = 0;
            int pendingServices = 0;
            int completedServices = 0;

            Cursor cMech = databaseHelper.getConnectedMechanicsForGarage(garageOwnerId);
            if (cMech != null) {
                while (cMech.moveToNext()) {
                    int mechId = cMech.getInt(cMech.getColumnIndexOrThrow("mechanic_id"));
                    Cursor cServ = databaseHelper.getServicesByMechanicId(mechId);
                    if (cServ != null) {
                        while (cServ.moveToNext()) {
                            totalServices++;
                            String st = cServ.getString(cServ.getColumnIndexOrThrow("status"));
                            if ("Completed".equalsIgnoreCase(st)) {
                                completedServices++;
                            } else {
                                pendingServices++;
                            }
                        }
                        cServ.close();
                    }
                }
                cMech.close();
            }

            pendingServicesTextView.setText(String.valueOf(pendingServices));
            completedServicesTextView.setText(String.valueOf(completedServices));
        }
    }
}
