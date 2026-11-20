package com.example.autocare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AutoCare.db";
    private static final int DATABASE_VERSION = 10;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createGarageOwnerTable = "CREATE TABLE garage_owners (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT, " +
                "garage_name TEXT, " +
                "email TEXT UNIQUE, " +
                "phone TEXT, " +
                "address TEXT, " +
                "password_hash TEXT)";
        db.execSQL(createGarageOwnerTable);

        String createMechanicTable = "CREATE TABLE mechanics (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT, " +
                "email TEXT UNIQUE, " +
                "phone TEXT, " +
                "city TEXT, " +
                "role TEXT, " +
                "password_hash TEXT)";
        db.execSQL(createMechanicTable);

        String createCustomersTable = "CREATE TABLE customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mechanic_id INTEGER, " +
                "full_name TEXT, " +
                "mobile TEXT, " +
                "email TEXT, " +
                "address TEXT, " +
                "password_hash TEXT)";
        db.execSQL(createCustomersTable);

        String createVehiclesTable = "CREATE TABLE vehicles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER, " +
                "mechanic_id INTEGER, " +
                "type TEXT, " +
                "reg_number TEXT UNIQUE, " +
                "brand TEXT, " +
                "model TEXT, " +
                "year TEXT, " +
                "color TEXT)";
        db.execSQL(createVehiclesTable);

        String createServicesTable = "CREATE TABLE services (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vehicle_id INTEGER, " +
                "customer_id INTEGER, " +
                "mechanic_id INTEGER, " +
                "service_date TEXT, " +
                "service_type TEXT, " +
                "complaint TEXT, " +
                "work_done TEXT, " +
                "mechanic_notes TEXT, " +
                "labour_charges REAL, " +
                "other_charges REAL, " +
                "parts_cost REAL, " +
                "discount REAL, " +
                "total_cost REAL, " +
                "status TEXT, " +
                "payment_status TEXT DEFAULT 'Pending', " +
                "razorpay_payment_id TEXT)";
        db.execSQL(createServicesTable);

        String createReplacedPartsTable = "CREATE TABLE replaced_parts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "service_id INTEGER, " +
                "part_name TEXT, " +
                "part_brand TEXT, " +
                "quantity INTEGER, " +
                "price_per_unit REAL, " +
                "total_cost REAL, " +
                "warranty_duration INTEGER, " +
                "warranty_unit TEXT, " +
                "warranty_start TEXT, " +
                "warranty_expiry TEXT, " +
                "notes TEXT)";
        db.execSQL(createReplacedPartsTable);

        String createRequestsTable = "CREATE TABLE garage_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "garage_id INTEGER, " +
                "mechanic_id INTEGER, " +
                "request_date TEXT, " +
                "status TEXT, " +
                "is_active INTEGER DEFAULT 0)";
        db.execSQL(createRequestsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            String createReplacedPartsTable = "CREATE TABLE IF NOT EXISTS replaced_parts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "service_id INTEGER, " +
                    "part_name TEXT, " +
                    "part_brand TEXT, " +
                    "quantity INTEGER, " +
                    "price_per_unit REAL, " +
                    "total_cost REAL, " +
                    "warranty_duration INTEGER, " +
                    "warranty_unit TEXT, " +
                    "warranty_start TEXT, " +
                    "warranty_expiry TEXT, " +
                    "notes TEXT)";
            db.execSQL(createReplacedPartsTable);
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE services ADD COLUMN other_charges REAL DEFAULT 0.0");
            db.execSQL("ALTER TABLE services ADD COLUMN discount REAL DEFAULT 0.0");
        }
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE customers ADD COLUMN password_hash TEXT");
        }
        if (oldVersion < 9) {
            String createRequestsTable = "CREATE TABLE IF NOT EXISTS garage_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "garage_id INTEGER, " +
                    "mechanic_id INTEGER, " +
                    "request_date TEXT, " +
                    "status TEXT, " +
                    "is_active INTEGER DEFAULT 0)";
            db.execSQL(createRequestsTable);
        }
        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE services ADD COLUMN payment_status TEXT DEFAULT 'Pending'");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE services ADD COLUMN razorpay_payment_id TEXT");
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        try {
            db.execSQL("ALTER TABLE services ADD COLUMN payment_status TEXT DEFAULT 'Pending'");
        } catch (Exception ignored) {}
        try {
            db.execSQL("ALTER TABLE services ADD COLUMN razorpay_payment_id TEXT");
        } catch (Exception ignored) {}
    }

    public boolean registerGarageOwner(String fullName, String garageName, String email, String phone, String address, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("garage_name", garageName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("address", address);
        values.put("password_hash", hashPassword(password));

        long result = db.insert("garage_owners", null, values);
        return result != -1;
    }

    public boolean registerMechanic(String fullName, String email, String phone, String city, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("city", city);
        values.put("role", "Mechanic");
        values.put("password_hash", hashPassword(password));

        long result = db.insert("mechanics", null, values);
        return result != -1;
    }

    public boolean checkEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorGarage = db.rawQuery("SELECT id FROM garage_owners WHERE LOWER(email) = LOWER(?)", new String[]{email.trim()});
        boolean existsInGarage = cursorGarage.getCount() > 0;
        cursorGarage.close();

        Cursor cursorMechanic = db.rawQuery("SELECT id FROM mechanics WHERE LOWER(email) = LOWER(?)", new String[]{email.trim()});
        boolean existsInMechanic = cursorMechanic.getCount() > 0;
        cursorMechanic.close();
        
        Cursor cursorCustomer = db.rawQuery("SELECT id FROM customers WHERE LOWER(email) = LOWER(?) AND password_hash IS NOT NULL AND password_hash != ''", new String[]{email.trim()});
        boolean existsInCustomer = cursorCustomer.getCount() > 0;
        cursorCustomer.close();

        return existsInGarage || existsInMechanic || existsInCustomer;
    }
    
    public boolean updatePasswordByEmail(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        String hashedPassword = hashPassword(newPassword);
        boolean updated = false;
        
        ContentValues values = new ContentValues();
        values.put("password_hash", hashedPassword);
        
        if (db.update("garage_owners", values, "email = ?", new String[]{email}) > 0) {
            updated = true;
        }
        if (db.update("mechanics", values, "email = ?", new String[]{email}) > 0) {
            updated = true;
        }
        if (db.update("customers", values, "email = ?", new String[]{email}) > 0) {
            updated = true;
        }
        return updated;
    }

    public boolean loginGarageOwner(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery("SELECT * FROM garage_owners WHERE email = ? AND password_hash = ?", new String[]{email, hashedPassword});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public String getGarageName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT garage_name FROM garage_owners WHERE email = ?", new String[]{email});
        String garageName = "Garage";
        if (cursor.moveToFirst()) {
            garageName = cursor.getString(0);
        }
        cursor.close();
        return garageName;
    }

    public int getGarageOwnerId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM garage_owners WHERE email = ?", new String[]{email});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public boolean loginMechanic(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery("SELECT * FROM mechanics WHERE email = ? AND password_hash = ?", new String[]{email, hashedPassword});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public String getMechanicName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT full_name FROM mechanics WHERE email = ?", new String[]{email});
        String name = "Mechanic";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public int getMechanicId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM mechanics WHERE email = ?", new String[]{email});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public boolean updateMechanicProfile(int mechanicId, String fullName, String phone, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("phone", phone);
        values.put("city", city);
        return db.update("mechanics", values, "id = ?", new String[]{String.valueOf(mechanicId)}) > 0;
    }

    public Cursor getGarageOwnerById(int garageOwnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM garage_owners WHERE id = ?", new String[]{String.valueOf(garageOwnerId)});
    }

    public boolean updateGarageOwnerProfile(int garageOwnerId, String fullName, String garageName, String phone, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("garage_name", garageName);
        values.put("phone", phone);
        values.put("address", address);
        return db.update("garage_owners", values, "id = ?", new String[]{String.valueOf(garageOwnerId)}) > 0;
    }

    public int getMechanicCustomerCount(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM customers WHERE mechanic_id = ?", new String[]{String.valueOf(mechanicId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getMechanicVehicleCount(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM vehicles WHERE mechanic_id = ?", new String[]{String.valueOf(mechanicId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getMechanicServiceCount(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM services WHERE mechanic_id = ?", new String[]{String.valueOf(mechanicId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean checkCustomerExists(int mechanicId, String email, String mobile) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM customers WHERE mechanic_id = ? AND (email = ? OR mobile = ?)",
                new String[]{String.valueOf(mechanicId), email, mobile});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean addCustomer(int mechanicId, String fullName, String mobile, String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        Cursor cursor = db.rawQuery("SELECT id FROM customers WHERE LOWER(email) = LOWER(?) AND (mechanic_id = -1 OR mechanic_id IS NULL)", new String[]{email.trim()});
        if (cursor.moveToFirst()) {
            int existingId = cursor.getInt(0);
            cursor.close();
            
            ContentValues values = new ContentValues();
            values.put("mechanic_id", mechanicId);
            if (fullName != null && !fullName.trim().isEmpty()) values.put("full_name", fullName);
            if (mobile != null && !mobile.trim().isEmpty()) values.put("mobile", mobile);
            if (address != null && !address.trim().isEmpty()) values.put("address", address);
            
            return db.update("customers", values, "id = ?", new String[]{String.valueOf(existingId)}) > 0;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("mechanic_id", mechanicId);
        values.put("full_name", fullName);
        values.put("mobile", mobile);
        values.put("email", email);
        values.put("address", address);

        long result = db.insert("customers", null, values);
        return result != -1;
    }

    public Cursor getMechanicById(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM mechanics WHERE id = ?", new String[]{String.valueOf(mechanicId)});
    }

    public Cursor getCustomersByMechanicId(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM customers WHERE mechanic_id = ? ORDER BY id DESC", new String[]{String.valueOf(mechanicId)});
    }

    public Cursor getCustomerById(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM customers WHERE id = ?", new String[]{String.valueOf(customerId)});
    }

    public Cursor searchCustomers(int mechanicId, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String likeQuery = "%" + query + "%";
        String sql = "SELECT DISTINCT c.* FROM customers c LEFT JOIN vehicles v ON c.id = v.customer_id WHERE c.mechanic_id = ? AND (c.full_name LIKE ? OR c.mobile LIKE ? OR v.reg_number LIKE ?)";
        return db.rawQuery(sql, new String[]{String.valueOf(mechanicId), likeQuery, likeQuery, likeQuery});
    }

    public Cursor getCustomersByGarageId(int garageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, m.full_name as mechanic_name " +
                "FROM customers c " +
                "INNER JOIN mechanics m ON c.mechanic_id = m.id " +
                "INNER JOIN garage_requests gr ON m.id = gr.mechanic_id " +
                "WHERE gr.garage_id = ? AND gr.status = 'APPROVED' AND gr.is_active = 1 " +
                "ORDER BY c.full_name ASC";
        return db.rawQuery(query, new String[]{String.valueOf(garageId)});
    }

    public Cursor searchCustomersForGarage(int garageId, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String likeQuery = "%" + query.trim() + "%";
        String sql = "SELECT DISTINCT c.*, m.full_name as mechanic_name " +
                "FROM customers c " +
                "INNER JOIN mechanics m ON c.mechanic_id = m.id " +
                "INNER JOIN garage_requests gr ON m.id = gr.mechanic_id " +
                "LEFT JOIN vehicles v ON c.id = v.customer_id " +
                "WHERE gr.garage_id = ? AND gr.status = 'APPROVED' AND gr.is_active = 1 " +
                "AND (c.full_name LIKE ? OR c.mobile LIKE ? OR c.email LIKE ? OR v.reg_number LIKE ? OR m.full_name LIKE ?)" +
                "ORDER BY c.full_name ASC";
        return db.rawQuery(sql, new String[]{String.valueOf(garageId), likeQuery, likeQuery, likeQuery, likeQuery, likeQuery});
    }

    public Cursor getVehiclesByCustomerId(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM vehicles WHERE customer_id = ?", new String[]{String.valueOf(customerId)});
    }

    public boolean checkVehicleExists(String regNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM vehicles WHERE reg_number = ?", new String[]{regNumber});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean addVehicle(int customerId, int mechanicId, String type, String regNumber, String brand, String model, String year, String color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customer_id", customerId);
        values.put("mechanic_id", mechanicId);
        values.put("type", type);
        values.put("reg_number", regNumber);
        values.put("brand", brand);
        values.put("model", model);
        values.put("year", year);
        values.put("color", color);

        long result = db.insert("vehicles", null, values);
        return result != -1;
    }

    public Cursor getVehicleAndCustomerDetails(int vehicleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT v.id, v.brand, v.model, v.reg_number, c.full_name, v.customer_id, v.mechanic_id " +
                "FROM vehicles v INNER JOIN customers c ON v.customer_id = c.id WHERE v.id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(vehicleId)});
    }

    public Cursor getServiceSummary(int serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.id, v.brand, v.model, v.reg_number, c.full_name, s.service_date, s.mechanic_id " +
                "FROM services s " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "INNER JOIN customers c ON s.customer_id = c.id " +
                "WHERE s.id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(serviceId)});
    }

    public long addService(int vehicleId, int customerId, int mechanicId, String date, String type, 
                              String complaint, String workDone, String notes, double labour, double otherCharges, double discount, double parts, 
                              double total, String status) {
        return addService(vehicleId, customerId, mechanicId, date, type, complaint, workDone, notes, labour, otherCharges, discount, parts, total, status, "Pending");
    }

    public long addService(int vehicleId, int customerId, int mechanicId, String date, String type, 
                              String complaint, String workDone, String notes, double labour, double otherCharges, double discount, double parts, 
                              double total, String status, String paymentStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("vehicle_id", vehicleId);
        values.put("customer_id", customerId);
        values.put("mechanic_id", mechanicId);
        values.put("service_date", date);
        values.put("service_type", type);
        values.put("complaint", complaint);
        values.put("work_done", workDone);
        values.put("mechanic_notes", notes);
        values.put("labour_charges", labour);
        values.put("other_charges", otherCharges);
        values.put("discount", discount);
        values.put("parts_cost", parts);
        values.put("total_cost", total);
        values.put("status", status);
        values.put("payment_status", (paymentStatus != null && !paymentStatus.isEmpty()) ? paymentStatus : "Pending");

        return db.insert("services", null, values);
    }

    public boolean updateService(int serviceId, String date, String type, 
                              String complaint, String workDone, String notes, double labour, double otherCharges, double discount, double parts, 
                              double total, String status) {
        return updateService(serviceId, date, type, complaint, workDone, notes, labour, otherCharges, discount, parts, total, status, null);
    }

    public boolean updateService(int serviceId, String date, String type, 
                              String complaint, String workDone, String notes, double labour, double otherCharges, double discount, double parts, 
                              double total, String status, String paymentStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("service_date", date);
        values.put("service_type", type);
        values.put("complaint", complaint);
        values.put("work_done", workDone);
        values.put("mechanic_notes", notes);
        values.put("labour_charges", labour);
        values.put("other_charges", otherCharges);
        values.put("discount", discount);
        values.put("parts_cost", parts);
        values.put("total_cost", total);
        values.put("status", status);
        if (paymentStatus != null) {
            values.put("payment_status", paymentStatus);
        }

        return db.update("services", values, "id = ?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    public boolean updateServicePaymentStatus(int serviceId, String paymentStatus, String razorpayPaymentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("payment_status", paymentStatus);
        if (razorpayPaymentId != null && !razorpayPaymentId.isEmpty()) {
            values.put("razorpay_payment_id", razorpayPaymentId);
        }
        return db.update("services", values, "id = ?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    public boolean updateServiceTotals(int serviceId, double partsTotal, double totalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("parts_cost", partsTotal);
        values.put("total_cost", totalCost);
        return db.update("services", values, "id = ?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    public boolean updateServiceStatus(int serviceId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        return db.update("services", values, "id = ?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    public boolean deleteService(int serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("replaced_parts", "service_id = ?", new String[]{String.valueOf(serviceId)});
            int result = db.delete("services", "id = ?", new String[]{String.valueOf(serviceId)});
            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateCustomer(int customerId, String fullName, String mobile, String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("mobile", mobile);
        values.put("email", email);
        values.put("address", address);
        return db.update("customers", values, "id = ?", new String[]{String.valueOf(customerId)}) > 0;
    }

    public boolean deleteCustomer(int customerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT id FROM services WHERE customer_id = ?", new String[]{String.valueOf(customerId)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int serviceId = cursor.getInt(0);
                    db.delete("replaced_parts", "service_id = ?", new String[]{String.valueOf(serviceId)});
                }
                cursor.close();
            }
            db.delete("services", "customer_id = ?", new String[]{String.valueOf(customerId)});
            db.delete("vehicles", "customer_id = ?", new String[]{String.valueOf(customerId)});
            int result = db.delete("customers", "id = ?", new String[]{String.valueOf(customerId)});
            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateVehicle(int vehicleId, String type, String regNumber, String brand, String model, String year, String color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("reg_number", regNumber);
        values.put("brand", brand);
        values.put("model", model);
        values.put("year", year);
        values.put("color", color);
        return db.update("vehicles", values, "id = ?", new String[]{String.valueOf(vehicleId)}) > 0;
    }

    public boolean deleteVehicle(int vehicleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM services WHERE vehicle_id = ?", new String[]{String.valueOf(vehicleId)});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return db.delete("vehicles", "id = ?", new String[]{String.valueOf(vehicleId)}) > 0;
    }

    public boolean updateReplacedPart(int partId, String partName, String partBrand, int quantity, 
                                   double pricePerUnit, double totalCost, int warrantyDuration, 
                                   String warrantyUnit, String warrantyStart, String warrantyExpiry, 
                                   String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("part_name", partName);
        values.put("part_brand", partBrand);
        values.put("quantity", quantity);
        values.put("price_per_unit", pricePerUnit);
        values.put("total_cost", totalCost);
        values.put("warranty_duration", warrantyDuration);
        values.put("warranty_unit", warrantyUnit);
        values.put("warranty_start", warrantyStart);
        values.put("warranty_expiry", warrantyExpiry);
        values.put("notes", notes);
        return db.update("replaced_parts", values, "id = ?", new String[]{String.valueOf(partId)}) > 0;
    }

    public boolean deleteReplacedPart(int partId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("replaced_parts", "id = ?", new String[]{String.valueOf(partId)}) > 0;
    }

    public Cursor getServiceById(int serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM services WHERE id = ?", new String[]{String.valueOf(serviceId)});
    }
    
    public Cursor getServiceDetailsWithVehicle(int serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, v.brand, v.model, v.reg_number, v.type " +
                       "FROM services s " +
                       "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                       "WHERE s.id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(serviceId)});
    }

    public boolean addReplacedPart(int serviceId, String partName, String partBrand, int quantity, 
                                   double pricePerUnit, double totalCost, int warrantyDuration, 
                                   String warrantyUnit, String warrantyStart, String warrantyExpiry, 
                                   String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("service_id", serviceId);
        values.put("part_name", partName);
        values.put("part_brand", partBrand);
        values.put("quantity", quantity);
        values.put("price_per_unit", pricePerUnit);
        values.put("total_cost", totalCost);
        values.put("warranty_duration", warrantyDuration);
        values.put("warranty_unit", warrantyUnit);
        values.put("warranty_start", warrantyStart);
        values.put("warranty_expiry", warrantyExpiry);
        values.put("notes", notes);

        long result = db.insert("replaced_parts", null, values);
        return result != -1;
    }

    public Cursor getServicesByVehicleId(int vehicleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM services WHERE vehicle_id = ? ORDER BY service_date DESC, id DESC", new String[]{String.valueOf(vehicleId)});
    }

    public Cursor getServicesByMechanicId(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, v.brand, v.model, v.reg_number, c.full_name " +
                "FROM services s " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "INNER JOIN customers c ON s.customer_id = c.id " +
                "WHERE s.mechanic_id = ? " +
                "ORDER BY s.service_date DESC, s.id DESC";
        return db.rawQuery(query, new String[]{String.valueOf(mechanicId)});
    }

    public Cursor getServicesByGarageId(int garageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, v.brand, v.model, v.reg_number, c.full_name, m.full_name as mechanic_name " +
                "FROM services s " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "INNER JOIN customers c ON s.customer_id = c.id " +
                "INNER JOIN mechanics m ON s.mechanic_id = m.id " +
                "INNER JOIN garage_requests gr ON m.id = gr.mechanic_id " +
                "WHERE gr.garage_id = ? AND gr.status = 'APPROVED' AND gr.is_active = 1 " +
                "ORDER BY s.service_date DESC, s.id DESC";
        return db.rawQuery(query, new String[]{String.valueOf(garageId)});
    }

    public Cursor searchServices(int mechanicId, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "%" + query.trim() + "%";
        String sql = "SELECT s.*, v.brand, v.model, v.reg_number, c.full_name " +
                "FROM services s " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "INNER JOIN customers c ON s.customer_id = c.id " +
                "WHERE s.mechanic_id = ? AND (c.full_name LIKE ? OR v.brand LIKE ? OR v.model LIKE ? OR v.reg_number LIKE ? OR s.service_type LIKE ? OR s.status LIKE ?)" +
                "ORDER BY s.service_date DESC, s.id DESC";
        return db.rawQuery(sql, new String[]{String.valueOf(mechanicId), searchQuery, searchQuery, searchQuery, searchQuery, searchQuery, searchQuery});
    }

    public Cursor getReplacedPartsByServiceId(int serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM replaced_parts WHERE service_id = ?", new String[]{String.valueOf(serviceId)});
    }

    public Cursor getCustomerByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM customers WHERE LOWER(email) = LOWER(?)", new String[]{email.trim()});
    }

    public Cursor getCustomerByMobile(String mobile) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM customers WHERE mobile = ?", new String[]{mobile.trim()});
    }

    public boolean registerNewCustomerAccountDirect(String fullName, String mobile, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mechanic_id", -1);
        values.put("full_name", fullName);
        values.put("mobile", mobile);
        values.put("email", email);
        values.put("password_hash", hashPassword(password));

        long result = db.insert("customers", null, values);
        return result != -1;
    }

    public boolean registerCustomerAccount(int customerId, String fullName, String mobile, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (fullName != null && !fullName.trim().isEmpty()) {
            values.put("full_name", fullName);
        }
        if (mobile != null && !mobile.trim().isEmpty()) {
            values.put("mobile", mobile);
        }
        values.put("password_hash", hashPassword(password));
        return db.update("customers", values, "id = ?", new String[]{String.valueOf(customerId)}) > 0;
    }

    public boolean loginCustomer(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery("SELECT * FROM customers WHERE LOWER(email) = LOWER(?) AND password_hash = ?", new String[]{email.trim(), hashedPassword});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getCustomerDetailsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM customers WHERE LOWER(email) = LOWER(?)", new String[]{email.trim()});
    }

    public Cursor getCustomerVehicles(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM vehicles WHERE customer_id = ?", new String[]{String.valueOf(customerId)});
    }

    public Cursor getCustomerServices(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, v.brand, v.model, v.reg_number, v.type " +
                "FROM services s " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "WHERE s.customer_id = ? " +
                "ORDER BY s.service_date DESC, s.id DESC";
        return db.rawQuery(query, new String[]{String.valueOf(customerId)});
    }

    public int getCustomerPendingServiceCount(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM services WHERE customer_id = ? AND (LOWER(status) = 'pending' OR LOWER(status) = 'in progress')", new String[]{String.valueOf(customerId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getCustomerVehicleCount(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM vehicles WHERE customer_id = ?", new String[]{String.valueOf(customerId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getCustomerServiceCount(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM services WHERE customer_id = ?", new String[]{String.valueOf(customerId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor getCustomerWarranties(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT rp.*, v.brand, v.model, v.reg_number, s.service_date " +
                "FROM replaced_parts rp " +
                "INNER JOIN services s ON rp.service_id = s.id " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "WHERE s.customer_id = ? " +
                "ORDER BY rp.id DESC";
        return db.rawQuery(query, new String[]{String.valueOf(customerId)});
    }

    public Cursor getServiceDetailsForCustomer(int serviceId, int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, v.brand, v.model, v.reg_number, v.type, v.year, v.color " +
                "FROM services s " +
                "INNER JOIN vehicles v ON s.vehicle_id = v.id " +
                "WHERE s.id = ? AND s.customer_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(serviceId), String.valueOf(customerId)});
    }

    public Cursor getAllGarages(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (query == null || query.trim().isEmpty()) {
            return db.rawQuery("SELECT * FROM garage_owners ORDER BY garage_name ASC", null);
        } else {
            String like = "%" + query.trim() + "%";
            return db.rawQuery("SELECT * FROM garage_owners WHERE garage_name LIKE ? OR address LIKE ? ORDER BY garage_name ASC", new String[]{like, like});
        }
    }

    public String getGarageRequestStatus(int mechanicId, int garageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT status, is_active FROM garage_requests WHERE mechanic_id = ? AND garage_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(mechanicId), String.valueOf(garageId)});
        String result = "NOT_CONNECTED";
        if (cursor.moveToFirst()) {
            String status = cursor.getString(0);
            int isActive = cursor.getInt(1);
            if ("APPROVED".equalsIgnoreCase(status) && isActive == 1) {
                result = "CONNECTED";
            } else if ("PENDING".equalsIgnoreCase(status)) {
                result = "PENDING";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                result = "REJECTED";
            } else if ("CANCELLED".equalsIgnoreCase(status) || "DISCONNECTED".equalsIgnoreCase(status)) {
                result = "NOT_CONNECTED";
            }
        }
        cursor.close();
        return result;
    }

    public boolean sendGarageRequest(int mechanicId, int garageId, String requestDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, status, is_active FROM garage_requests WHERE mechanic_id = ? AND garage_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(mechanicId), String.valueOf(garageId)});
        if (cursor.moveToFirst()) {
            String status = cursor.getString(1);
            int isActive = cursor.getInt(2);
            if ("PENDING".equalsIgnoreCase(status) || ("APPROVED".equalsIgnoreCase(status) && isActive == 1)) {
                cursor.close();
                return false;
            }
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("garage_id", garageId);
        values.put("mechanic_id", mechanicId);
        values.put("request_date", requestDate);
        values.put("status", "PENDING");
        values.put("is_active", 0);

        long result = db.insert("garage_requests", null, values);
        return result != -1;
    }

    public Cursor getMechanicRequests(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.id, r.garage_id, r.mechanic_id, r.request_date, r.status, r.is_active, " +
                "g.garage_name, g.address, g.phone, g.email, g.full_name as owner_name " +
                "FROM garage_requests r " +
                "INNER JOIN garage_owners g ON r.garage_id = g.id " +
                "WHERE r.mechanic_id = ? " +
                "ORDER BY r.id DESC";
        return db.rawQuery(query, new String[]{String.valueOf(mechanicId)});
    }

    public boolean cancelGarageRequest(int requestId, int mechanicId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "CANCELLED");
        values.put("is_active", 0);
        return db.update("garage_requests", values, "id = ? AND mechanic_id = ? AND status = 'PENDING'", new String[]{String.valueOf(requestId), String.valueOf(mechanicId)}) > 0;
    }

    public Cursor getRequestsForGarageOwner(int garageOwnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.id, r.garage_id, r.mechanic_id, r.request_date, r.status, r.is_active, " +
                "m.full_name, m.email, m.phone, m.city " +
                "FROM garage_requests r " +
                "INNER JOIN mechanics m ON r.mechanic_id = m.id " +
                "WHERE r.garage_id = ? AND r.status = 'PENDING' " +
                "ORDER BY r.id DESC";
        return db.rawQuery(query, new String[]{String.valueOf(garageOwnerId)});
    }

    public boolean updateRequestStatus(int requestId, int garageOwnerId, String status, int isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("is_active", isActive);
        return db.update("garage_requests", values, "id = ? AND garage_id = ?", new String[]{String.valueOf(requestId), String.valueOf(garageOwnerId)}) > 0;
    }

    public Cursor getConnectedMechanicsForGarage(int garageOwnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.id as request_id, r.request_date, m.id as mechanic_id, m.full_name, m.email, m.phone, m.city " +
                "FROM garage_requests r " +
                "INNER JOIN mechanics m ON r.mechanic_id = m.id " +
                "WHERE r.garage_id = ? AND r.status = 'APPROVED' AND r.is_active = 1 " +
                "ORDER BY m.full_name ASC";
        return db.rawQuery(query, new String[]{String.valueOf(garageOwnerId)});
    }

    public boolean disconnectMechanic(int requestId, int garageOwnerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "DISCONNECTED");
        values.put("is_active", 0);
        return db.update("garage_requests", values, "id = ? AND garage_id = ?", new String[]{String.valueOf(requestId), String.valueOf(garageOwnerId)}) > 0;
    }

    public String getMechanicConnectionInfo(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.status, r.is_active, g.garage_name " +
                "FROM garage_requests r " +
                "INNER JOIN garage_owners g ON r.garage_id = g.id " +
                "WHERE r.mechanic_id = ? " +
                "ORDER BY r.id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(mechanicId)});
        String info = "Not Connected";
        if (cursor.moveToFirst()) {
            String status = cursor.getString(0);
            int isActive = cursor.getInt(1);
            String garageName = cursor.getString(2);

            if ("APPROVED".equalsIgnoreCase(status) && isActive == 1) {
                info = "Connected: " + garageName;
            } else if ("PENDING".equalsIgnoreCase(status)) {
                info = "Pending: " + garageName;
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                info = "Rejected by " + garageName;
            }
        }
        cursor.close();
        return info;
    }

    public int getGarageConnectedMechanicCount(int garageOwnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM garage_requests WHERE garage_id = ? AND status = 'APPROVED' AND is_active = 1", new String[]{String.valueOf(garageOwnerId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getGarageCustomerCount(int garageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT c.id) FROM customers c " +
                "INNER JOIN mechanics m ON c.mechanic_id = m.id " +
                "INNER JOIN garage_requests gr ON m.id = gr.mechanic_id " +
                "WHERE gr.garage_id = ? AND gr.status = 'APPROVED' AND gr.is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(garageId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getGaragePendingRequestCount(int garageOwnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM garage_requests WHERE garage_id = ? AND status = 'PENDING'", new String[]{String.valueOf(garageOwnerId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean isMechanicConnected(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM garage_requests WHERE mechanic_id = ? AND status = 'APPROVED' AND is_active = 1", new String[]{String.valueOf(mechanicId)});
        boolean connected = false;
        if (cursor.moveToFirst()) {
            connected = cursor.getInt(0) > 0;
        }
        cursor.close();
        return connected;
    }

    public boolean updateCustomerProfile(int customerId, String fullName, String mobile, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("mobile", mobile);
        values.put("address", address);

        int rows = db.update("customers", values, "id = ?", new String[]{String.valueOf(customerId)});
        return rows > 0;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalGarageOwnersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM garage_owners", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalMechanicsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM mechanics", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalCustomersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM customers", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalVehiclesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM vehicles", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalServicesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM services", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor getAllMechanics() {
        return getAllMechanics("");
    }

    public Cursor getAllMechanics(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (query == null || query.trim().isEmpty()) {
            return db.rawQuery("SELECT * FROM mechanics ORDER BY full_name ASC", null);
        } else {
            String like = "%" + query.trim() + "%";
            return db.rawQuery("SELECT * FROM mechanics WHERE full_name LIKE ? OR email LIKE ? OR phone LIKE ? OR city LIKE ? ORDER BY full_name ASC", new String[]{like, like, like, like});
        }
    }

    public Cursor getAllCustomers() {
        return getAllCustomers("");
    }

    public Cursor getAllCustomers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (query == null || query.trim().isEmpty()) {
            return db.rawQuery("SELECT c.*, m.full_name as mechanic_name FROM customers c LEFT JOIN mechanics m ON c.mechanic_id = m.id ORDER BY c.id DESC", null);
        } else {
            String like = "%" + query.trim() + "%";
            return db.rawQuery("SELECT c.*, m.full_name as mechanic_name FROM customers c LEFT JOIN mechanics m ON c.mechanic_id = m.id WHERE c.full_name LIKE ? OR c.mobile LIKE ? OR c.email LIKE ? ORDER BY c.id DESC", new String[]{like, like, like});
        }
    }

    public Cursor getAllServices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT s.*, c.full_name as customer_name, m.full_name as mechanic_name FROM services s INNER JOIN customers c ON s.customer_id = c.id INNER JOIN mechanics m ON s.mechanic_id = m.id ORDER BY s.service_date DESC, s.id DESC", null);
    }

    public int getMechanicLinkedCustomersCount(int mechanicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM customers WHERE mechanic_id = ?", new String[]{String.valueOf(mechanicId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getGarageOwnerConnectedMechanicsCount(int garageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM garage_requests WHERE garage_id = ? AND (status = 'ACCEPTED' OR is_active = 1)", new String[]{String.valueOf(garageId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public boolean deleteGarageOwner(int garageId) {
        if (getGarageOwnerConnectedMechanicsCount(garageId) > 0) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("garage_requests", "garage_id = ?", new String[]{String.valueOf(garageId)});
            int result = db.delete("garage_owners", "id = ?", new String[]{String.valueOf(garageId)});
            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteMechanic(int mechanicId) {
        if (getMechanicLinkedCustomersCount(mechanicId) > 0) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("garage_requests", "mechanic_id = ?", new String[]{String.valueOf(mechanicId)});
            int result = db.delete("mechanics", "id = ?", new String[]{String.valueOf(mechanicId)});
            db.setTransactionSuccessful();
            return result > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateCustomerAdmin(int customerId, String fullName, String mobile, String email, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("mobile", mobile);
        values.put("email", email);
        values.put("address", address);
        return db.update("customers", values, "id = ?", new String[]{String.valueOf(customerId)}) > 0;
    }
}