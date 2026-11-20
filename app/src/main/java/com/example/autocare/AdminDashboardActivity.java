package com.example.autocare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalGarages, tvTotalMechanics, tvTotalCustomers, tvTotalServices;
    private View logoutImageView, btnManageGarages, btnManageCustomers, btnManageMechanics;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        BottomNavHelper.setupBottomNav(this, R.id.navHome);

        databaseHelper = new DatabaseHelper(this);

        tvTotalGarages = findViewById(R.id.tvTotalGarages);
        tvTotalMechanics = findViewById(R.id.tvTotalMechanics);
        tvTotalCustomers = findViewById(R.id.tvTotalCustomers);
        tvTotalServices = findViewById(R.id.tvTotalServices);
        logoutImageView = findViewById(R.id.logoutImageView);
        btnManageGarages = findViewById(R.id.btnManageGarages);
        btnManageCustomers = findViewById(R.id.btnManageCustomers);
        btnManageMechanics = findViewById(R.id.btnManageMechanics);

        logoutImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out from Admin Panel?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        getSharedPreferences("AutoCarePrefs", MODE_PRIVATE).edit().clear().apply();
                        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnManageGarages.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminGaragesActivity.class);
            startActivity(intent);
        });

        btnManageCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminCustomersActivity.class);
            startActivity(intent);
        });

        btnManageMechanics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminMechanicsActivity.class);
            startActivity(intent);
        });

        // Setup bottom nav for admin
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navGarages = findViewById(R.id.navGarages);
        LinearLayout navCustomers = findViewById(R.id.navCustomers);
        LinearLayout navMechanics = findViewById(R.id.navMechanics);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {});
        }
        if (navGarages != null) {
            navGarages.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminGaragesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navCustomers != null) {
            navCustomers.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminCustomersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navMechanics != null) {
            navMechanics.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminMechanicsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminStats();
        BottomNavHelper.setupBottomNav(this, R.id.navHome);
    }

    private void loadAdminStats() {
        int garages = databaseHelper.getTotalGarageOwnersCount();
        int mechanics = databaseHelper.getTotalMechanicsCount();
        int customers = databaseHelper.getTotalCustomersCount();
        int services = databaseHelper.getTotalServicesCount();

        tvTotalGarages.setText(String.valueOf(garages));
        tvTotalMechanics.setText(String.valueOf(mechanics));
        tvTotalCustomers.setText(String.valueOf(customers));
        tvTotalServices.setText(String.valueOf(services));
    }
}
