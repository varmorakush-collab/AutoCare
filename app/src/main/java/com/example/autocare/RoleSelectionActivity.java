package com.example.autocare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private LinearLayout cardGarageOwner;
    private LinearLayout cardMechanic;
    private LinearLayout cardCustomer;

    private ImageView checkGarageOwner;
    private ImageView checkMechanic;
    private ImageView checkCustomer;

    private Button continueRoleButton;

    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        cardGarageOwner = findViewById(R.id.cardGarageOwner);
        cardMechanic = findViewById(R.id.cardMechanic);
        cardCustomer = findViewById(R.id.cardCustomer);

        checkGarageOwner = findViewById(R.id.checkGarageOwner);
        checkMechanic = findViewById(R.id.checkMechanic);
        checkCustomer = findViewById(R.id.checkCustomer);

        continueRoleButton = findViewById(R.id.continueRoleButton);

        cardGarageOwner.setOnClickListener(v -> selectRole("Garage Owner"));
        cardMechanic.setOnClickListener(v -> selectRole("Mechanic"));
        cardCustomer.setOnClickListener(v -> selectRole("Customer"));

        continueRoleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Garage Owner".equals(selectedRole)) {
                    Intent intent = new Intent(RoleSelectionActivity.this, GarageOwnerRegisterActivity.class);
                    intent.putExtra("selected_role", selectedRole);
                    startActivity(intent);
                } else if ("Mechanic".equals(selectedRole)) {
                    Intent intent = new Intent(RoleSelectionActivity.this, MechanicRegisterActivity.class);
                    intent.putExtra("selected_role", selectedRole);
                    startActivity(intent);
                } else if ("Customer".equals(selectedRole)) {
                    Intent intent = new Intent(RoleSelectionActivity.this, CustomerRegisterActivity.class);
                    intent.putExtra("selected_role", selectedRole);
                    startActivity(intent);
                }
            }
        });
    }

    private void selectRole(String role) {
        selectedRole = role;

        cardGarageOwner.setBackgroundResource(R.drawable.role_card_default);
        cardMechanic.setBackgroundResource(R.drawable.role_card_default);
        cardCustomer.setBackgroundResource(R.drawable.role_card_default);

        checkGarageOwner.setVisibility(View.GONE);
        checkMechanic.setVisibility(View.GONE);
        checkCustomer.setVisibility(View.GONE);

        if ("Garage Owner".equals(role)) {
            cardGarageOwner.setBackgroundResource(R.drawable.role_card_selected);
            checkGarageOwner.setVisibility(View.VISIBLE);
        } else if ("Mechanic".equals(role)) {
            cardMechanic.setBackgroundResource(R.drawable.role_card_selected);
            checkMechanic.setVisibility(View.VISIBLE);
        } else if ("Customer".equals(role)) {
            cardCustomer.setBackgroundResource(R.drawable.role_card_selected);
            checkCustomer.setVisibility(View.VISIBLE);
        }

        continueRoleButton.setEnabled(true);
        continueRoleButton.setBackgroundResource(R.drawable.btn_primary_bg);
        continueRoleButton.setTextColor(getResources().getColor(R.color.white));
    }
}