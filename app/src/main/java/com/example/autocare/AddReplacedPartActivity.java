package com.example.autocare;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddReplacedPartActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvCustomerNameSummary, tvVehicleDetailsSummary, tvServiceDateSummary;
    private EditText partNameEditText, partBrandEditText, quantityEditText, priceEditText;
    private TextView totalPartCostTextView, tvWarrantyStartDate, tvWarrantyExpiryDate;
    private Spinner warrantyUnitSpinner;
    private LinearLayout warrantyContainer;
    private EditText warrantyDurationEditText, partNotesEditText;
    private Button savePartButton;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int serviceId = -1;
    
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_replaced_part);

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

        serviceId = getIntent().getIntExtra("service_id", -1);

        if (loggedInMechanicId == -1 || serviceId == -1) {
            Toast.makeText(this, "Session or service data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvCustomerNameSummary = findViewById(R.id.tvCustomerNameSummary);
        tvVehicleDetailsSummary = findViewById(R.id.tvVehicleDetailsSummary);
        tvServiceDateSummary = findViewById(R.id.tvServiceDateSummary);
        
        partNameEditText = findViewById(R.id.partNameEditText);
        partBrandEditText = findViewById(R.id.partBrandEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        totalPartCostTextView = findViewById(R.id.totalPartCostTextView);
        
        warrantyUnitSpinner = findViewById(R.id.warrantyUnitSpinner);
        warrantyContainer = findViewById(R.id.warrantyContainer);
        warrantyDurationEditText = findViewById(R.id.warrantyDurationEditText);
        tvWarrantyStartDate = findViewById(R.id.tvWarrantyStartDate);
        tvWarrantyExpiryDate = findViewById(R.id.tvWarrantyExpiryDate);
        
        partNotesEditText = findViewById(R.id.partNotesEditText);
        savePartButton = findViewById(R.id.savePartButton);

        loadServiceSummary();

        String[] warrantyUnits = {"No Warranty", "Months", "Years"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, warrantyUnits);
        unitAdapter.setDropDownViewResource(R.layout.item_spinner);
        warrantyUnitSpinner.setAdapter(unitAdapter);

        updateStartDateLabel();

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateStartDateLabel();
                calculateWarranty();
            }
        };

        tvWarrantyStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddReplacedPartActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        warrantyUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = warrantyUnits[position];
                if (selected.equals("No Warranty")) {
                    warrantyContainer.setVisibility(View.GONE);
                } else {
                    warrantyContainer.setVisibility(View.VISIBLE);
                    calculateWarranty();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextWatcher costWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalCost();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        quantityEditText.addTextChangedListener(costWatcher);
        priceEditText.addTextChangedListener(costWatcher);

        warrantyDurationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateWarranty();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        savePartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePart();
            }
        });
    }

    private void loadServiceSummary() {
        Cursor cursor = databaseHelper.getServiceSummary(serviceId);
        if (cursor != null && cursor.moveToFirst()) {
            String brand = cursor.getString(cursor.getColumnIndexOrThrow("brand"));
            String model = cursor.getString(cursor.getColumnIndexOrThrow("model"));
            String regNumber = cursor.getString(cursor.getColumnIndexOrThrow("reg_number"));
            String customerName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
            String serviceDate = cursor.getString(cursor.getColumnIndexOrThrow("service_date"));
            int sMechanicId = cursor.getInt(cursor.getColumnIndexOrThrow("mechanic_id"));

            if (sMechanicId != loggedInMechanicId) {
                Toast.makeText(this, "Permission denied. You do not own this record.", Toast.LENGTH_SHORT).show();
                cursor.close();
                finish();
                return;
            }

            tvCustomerNameSummary.setText(customerName);
            tvVehicleDetailsSummary.setText(brand + " " + model + " - " + regNumber);
            tvServiceDateSummary.setText("Service Date: " + serviceDate);
            cursor.close();
        } else {
            Toast.makeText(this, "Service details not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void updateStartDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        tvWarrantyStartDate.setText(sdf.format(calendar.getTime()));
    }

    private void calculateTotalCost() {
        int qty = 0;
        double price = 0;
        try {
            if (!TextUtils.isEmpty(quantityEditText.getText().toString())) {
                qty = Integer.parseInt(quantityEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(priceEditText.getText().toString())) {
                price = Double.parseDouble(priceEditText.getText().toString());
            }
        } catch (NumberFormatException e) {
            // Ignore parse errors, default to 0
        }
        double total = qty * price;
        totalPartCostTextView.setText(String.format(Locale.US, "%.2f", total));
    }

    private void calculateWarranty() {
        String unit = warrantyUnitSpinner.getSelectedItem().toString();
        if (unit.equals("No Warranty")) {
            return;
        }

        int duration = 0;
        try {
            if (!TextUtils.isEmpty(warrantyDurationEditText.getText().toString())) {
                duration = Integer.parseInt(warrantyDurationEditText.getText().toString());
            }
        } catch (NumberFormatException e) {
            // default 0
        }

        if (duration <= 0) {
            tvWarrantyExpiryDate.setText("-");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String startDateStr = tvWarrantyStartDate.getText().toString();
        try {
            Date start = sdf.parse(startDateStr);
            Calendar cal = Calendar.getInstance();
            if (start != null) {
                cal.setTime(start);
            }
            
            if (unit.equals("Months")) {
                cal.add(Calendar.MONTH, duration);
            } else if (unit.equals("Years")) {
                cal.add(Calendar.YEAR, duration);
            }
            
            String expiryStr = sdf.format(cal.getTime());
            tvWarrantyExpiryDate.setText(expiryStr);
        } catch (ParseException e) {
            tvWarrantyExpiryDate.setText("Error");
        }
    }

    private void savePart() {
        String partName = partNameEditText.getText().toString().trim();
        String partBrand = partBrandEditText.getText().toString().trim();
        String notes = partNotesEditText.getText().toString().trim();
        String unit = warrantyUnitSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(partName)) {
            partNameEditText.setError("Part name is required");
            partNameEditText.requestFocus();
            return;
        }

        int qty = 0;
        double price = 0;
        double total = 0;
        try {
            if (!TextUtils.isEmpty(quantityEditText.getText().toString())) {
                qty = Integer.parseInt(quantityEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(priceEditText.getText().toString())) {
                price = Double.parseDouble(priceEditText.getText().toString());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric format.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qty <= 0) {
            quantityEditText.setError("Quantity must be greater than zero");
            quantityEditText.requestFocus();
            return;
        }

        if (price < 0) {
            priceEditText.setError("Price cannot be negative");
            priceEditText.requestFocus();
            return;
        }

        total = qty * price;

        int duration = 0;
        String startStr = null;
        String expiryStr = null;

        if (!unit.equals("No Warranty")) {
            try {
                if (!TextUtils.isEmpty(warrantyDurationEditText.getText().toString())) {
                    duration = Integer.parseInt(warrantyDurationEditText.getText().toString());
                }
            } catch (NumberFormatException e) {
                warrantyDurationEditText.setError("Invalid duration");
                warrantyDurationEditText.requestFocus();
                return;
            }

            if (duration <= 0) {
                warrantyDurationEditText.setError("Duration must be greater than zero");
                warrantyDurationEditText.requestFocus();
                return;
            }
            startStr = tvWarrantyStartDate.getText().toString();
            expiryStr = tvWarrantyExpiryDate.getText().toString();
        } else {
            startStr = "";
            expiryStr = "";
            duration = 0;
        }

        boolean isSaved = databaseHelper.addReplacedPart(serviceId, partName, partBrand, qty, price, total, duration, unit, startStr, expiryStr, notes);

        if (isSaved) {
            Toast.makeText(this, "Part saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save part. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
