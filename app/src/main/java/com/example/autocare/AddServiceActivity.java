package com.example.autocare;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import java.util.ArrayList;

public class AddServiceActivity extends AppCompatActivity {

    private ImageView backImageView;
    private TextView tvCustomerNameSummary, tvVehicleDetailsSummary, tvServiceDate, totalCostTextView;
    private Spinner serviceTypeSpinner, serviceStatusSpinner, paymentStatusSpinner;
    private EditText complaintEditText, workDoneEditText, notesEditText, labourEditText, otherChargesEditText, discountEditText;
    private Button saveServiceButton;

    private DatabaseHelper databaseHelper;
    private int loggedInMechanicId = -1;
    private int vehicleId = -1;
    private int customerId = -1;
    
    private final Calendar calendar = Calendar.getInstance();
    private ArrayList<View> partViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

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

        vehicleId = getIntent().getIntExtra("vehicle_id", -1);

        if (loggedInMechanicId == -1 || vehicleId == -1) {
            Toast.makeText(this, "Session or vehicle data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to add services.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backImageView = findViewById(R.id.backImageView);
        tvCustomerNameSummary = findViewById(R.id.tvCustomerNameSummary);
        tvVehicleDetailsSummary = findViewById(R.id.tvVehicleDetailsSummary);
        tvServiceDate = findViewById(R.id.tvServiceDate);
        totalCostTextView = findViewById(R.id.totalCostTextView);
        
        serviceTypeSpinner = findViewById(R.id.serviceTypeSpinner);
        serviceStatusSpinner = findViewById(R.id.serviceStatusSpinner);
        paymentStatusSpinner = findViewById(R.id.paymentStatusSpinner);

        complaintEditText = findViewById(R.id.complaintEditText);
        workDoneEditText = findViewById(R.id.workDoneEditText);
        notesEditText = findViewById(R.id.notesEditText);
        labourEditText = findViewById(R.id.labourEditText);
        otherChargesEditText = findViewById(R.id.otherChargesEditText);
        discountEditText = findViewById(R.id.discountEditText);
        
        saveServiceButton = findViewById(R.id.saveServiceButton);

        loadVehicleDetails();

        String[] serviceTypes = {"General Service", "Repair", "Maintenance", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, serviceTypes);
        typeAdapter.setDropDownViewResource(R.layout.item_spinner);
        serviceTypeSpinner.setAdapter(typeAdapter);

        String[] statuses = {"Pending", "In Progress", "Completed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, statuses);
        statusAdapter.setDropDownViewResource(R.layout.item_spinner);
        serviceStatusSpinner.setAdapter(statusAdapter);

        String[] paymentStatuses = {"Pending", "Paid"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, paymentStatuses);
        paymentAdapter.setDropDownViewResource(R.layout.item_spinner);
        paymentStatusSpinner.setAdapter(paymentAdapter);

        updateDateLabel();

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            }
        };

        tvServiceDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddServiceActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
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
        
        labourEditText.addTextChangedListener(costWatcher);
        otherChargesEditText.addTextChangedListener(costWatcher);
        discountEditText.addTextChangedListener(costWatcher);

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveService();
            }
        });
    }

    private void loadVehicleDetails() {
        Cursor cursor = databaseHelper.getVehicleAndCustomerDetails(vehicleId);
        if (cursor != null && cursor.moveToFirst()) {
            String brand = cursor.getString(1);
            String model = cursor.getString(2);
            String regNumber = cursor.getString(3);
            String customerName = cursor.getString(4);
            customerId = cursor.getInt(5);
            int vMechanicId = cursor.getInt(6);

            if (vMechanicId != loggedInMechanicId) {
                Toast.makeText(this, "Permission denied. You do not own this record.", Toast.LENGTH_SHORT).show();
                cursor.close();
                finish();
                return;
            }

            tvCustomerNameSummary.setText(customerName);
            tvVehicleDetailsSummary.setText(brand + " " + model + " - " + regNumber);
            cursor.close();
        } else {
            Toast.makeText(this, "Vehicle details not found.", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            finish();
        }
    }

    private void updateDateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tvServiceDate.setText(sdf.format(calendar.getTime()));
    }

    private void calculateTotalCost() {
        double labour = 0;
        double otherCharges = 0;
        double discount = 0;
        try {
            if (!TextUtils.isEmpty(labourEditText.getText().toString())) {
                labour = Double.parseDouble(labourEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(otherChargesEditText.getText().toString())) {
                otherCharges = Double.parseDouble(otherChargesEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(discountEditText.getText().toString())) {
                discount = Double.parseDouble(discountEditText.getText().toString());
            }
        } catch (NumberFormatException e) {
            // Leave at 0 if parse fails
        }

        double total = labour + otherCharges - discount;
        totalCostTextView.setText(String.format(Locale.US, "%.2f", total));
    }

    private void saveService() {
        if (!databaseHelper.isMechanicConnected(loggedInMechanicId)) {
            Toast.makeText(this, "You must be connected to an approved garage to add services.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String date = tvServiceDate.getText().toString();
        String type = serviceTypeSpinner.getSelectedItem().toString();
        String status = serviceStatusSpinner.getSelectedItem().toString();
        String paymentStatus = paymentStatusSpinner.getSelectedItem() != null ? paymentStatusSpinner.getSelectedItem().toString() : "Pending";
        String complaint = complaintEditText.getText().toString().trim();
        String workDone = workDoneEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(complaint)) {
            complaintEditText.setError("Complaint is required");
            complaintEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(workDone)) {
            workDoneEditText.setError("Work done details are required");
            workDoneEditText.requestFocus();
            return;
        }

        double labour = 0;
        double otherCharges = 0;
        double discount = 0;
        try {
            if (!TextUtils.isEmpty(labourEditText.getText().toString())) {
                labour = Double.parseDouble(labourEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(otherChargesEditText.getText().toString())) {
                otherCharges = Double.parseDouble(otherChargesEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(discountEditText.getText().toString())) {
                discount = Double.parseDouble(discountEditText.getText().toString());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric format in charges.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (labour < 0 || otherCharges < 0 || discount < 0) {
            Toast.makeText(this, "Charges cannot be negative.", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = labour + otherCharges - discount;

        long newServiceId = databaseHelper.addService(vehicleId, customerId, loggedInMechanicId, date, type, complaint, workDone, notes, labour, otherCharges, discount, 0, total, status, paymentStatus);

        if (newServiceId != -1) {
            Toast.makeText(this, "Service saved successfully!", Toast.LENGTH_SHORT).show();
            // Go to Service Details page to allow adding parts
            android.content.Intent intent = new android.content.Intent(AddServiceActivity.this, ServiceDetailsActivity.class);
            intent.putExtra("vehicle_id", vehicleId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save service. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}