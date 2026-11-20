package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MechanicDashboardActivity extends AppCompatActivity {

    private TextView mechanicNameTextView;
    private TextView connectionStatusTextView;
    private TextView myCustomersCountTextView, myVehiclesCountTextView;
    private TextView mechanicPendingServicesTextView, mechanicCompletedServicesTextView;
    
    private LinearLayout actionAddCustomer, actionMyCustomers, actionMyServices, actionFindGarages;
    private LinearLayout navHome, navCustomers, navServices, navProfile;
    private View logoutImageView;
    
    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_dashboard);
        BottomNavHelper.setupBottomNav(this, R.id.navHome);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseHelper = new DatabaseHelper(this);

        mechanicNameTextView = findViewById(R.id.mechanicNameTextView);
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        
        myCustomersCountTextView = findViewById(R.id.myCustomersCountTextView);
        myVehiclesCountTextView = findViewById(R.id.myVehiclesCountTextView);
        mechanicPendingServicesTextView = findViewById(R.id.mechanicPendingServicesTextView);
        mechanicCompletedServicesTextView = findViewById(R.id.mechanicCompletedServicesTextView);

        actionAddCustomer = findViewById(R.id.actionAddCustomer);
        actionMyCustomers = findViewById(R.id.actionMyCustomers);
        actionMyServices = findViewById(R.id.actionMyServices);
        actionFindGarages = findViewById(R.id.actionFindGarages);

        navHome = findViewById(R.id.navHome);
        navCustomers = findViewById(R.id.navCustomers);
        navServices = findViewById(R.id.navServices);
        navProfile = findViewById(R.id.navProfile);
        
        logoutImageView = findViewById(R.id.logoutImageView);

        SharedPreferences prefs = getSharedPreferences("AutoCarePrefs", MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", null);
        
        if (userEmail == null || !"Mechanic".equals(userRole)) {
            Intent intent = new Intent(MechanicDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String mechanicName = databaseHelper.getMechanicName(userEmail);
        mechanicNameTextView.setText("Welcome, " + mechanicName);

        actionAddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, AddCustomerActivity.class);
            startActivity(intent);
        });

        actionMyCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, MyCustomersActivity.class);
            startActivity(intent);
        });

        actionMyServices.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, MyServicesActivity.class);
            startActivity(intent);
        });

        actionFindGarages.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, FindGarageActivity.class);
            startActivity(intent);
        });

        navHome.setOnClickListener(v -> {});

        navCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, MyCustomersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navServices.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, MyServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MechanicDashboardActivity.this, MechanicProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        logoutImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                        Intent intent = new Intent(MechanicDashboardActivity.this, LoginActivity.class);
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
        loggedInMechanicId = databaseHelper.getMechanicId(userEmail);

        if (loggedInMechanicId != -1) {
            int cCount = databaseHelper.getMechanicCustomerCount(loggedInMechanicId);
            int vCount = databaseHelper.getMechanicVehicleCount(loggedInMechanicId);

            myCustomersCountTextView.setText(String.valueOf(cCount));
            myVehiclesCountTextView.setText(String.valueOf(vCount));

            int pendingCount = 0;
            int completedCount = 0;
            Cursor cursor = databaseHelper.getServicesByMechanicId(loggedInMechanicId);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    if ("Completed".equalsIgnoreCase(status)) {
                        completedCount++;
                    } else {
                        pendingCount++;
                    }
                }
                cursor.close();
            }

            mechanicPendingServicesTextView.setText(String.valueOf(pendingCount));
            mechanicCompletedServicesTextView.setText(String.valueOf(completedCount));

            String connInfo = databaseHelper.getMechanicConnectionInfo(loggedInMechanicId);
            connectionStatusTextView.setText(connInfo);

            if (connInfo.startsWith("Connected")) {
                connectionStatusTextView.setTextColor(getResources().getColor(R.color.splash_accent_teal));
            } else if (connInfo.startsWith("Pending")) {
                connectionStatusTextView.setTextColor(0xFFFF9800);
            } else {
                connectionStatusTextView.setTextColor(0xFFEF4444);
            }
        }
    }
}
