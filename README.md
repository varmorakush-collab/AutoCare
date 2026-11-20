# AutoCare - Vehicle Service & Garage Management System

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Java-orange.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-brightgreen.svg)
![Payment Gateway](https://img.shields.io/badge/Payment-Razorpay-blue.svg)
![Database](https://img.shields.io/badge/Database-SQLite-lightgrey.svg)

AutoCare is a comprehensive Android application designed to streamline vehicle maintenance, service history tracking, garage connectivity, warranty management, and online payment processing. The app provides a seamlessly integrated ecosystem connecting Customers (Vehicle Owners), Garage Owners, Mechanics, and System Administrators.

---

## Key Features

### 1. Customer (Vehicle Owner)
- Role-based Authentication: Secure Login, Registration, and Password Reset.
- Vehicle Management: Add and manage multiple vehicles with details like vehicle name, registration number, model, and fuel type.
- Service History: Access complete service logs, replaced parts details, cost breakdown, and maintenance dates.
- Warranty Tracking: Track warranty periods for installed replacement parts with active/expired status indicators.
- Find Garages: Search and locate nearby registered garages, request garage connections, and submit service requests.
- Digital Payments: Pay for vehicle services directly via integrated Razorpay Payment Gateway.

### 2. Garage Owner
- Garage Management: Register and set up garage profiles, services offered, and business details.
- Customer & Vehicle Tracking: Manage customer lists, link vehicles to customers, and record new service entries.
- Parts & Warranty Management: Record replaced spare parts with warranty duration and serial numbers.
- Mechanic Connections: Connect with mechanics, assign service tasks, and approve connection requests.
- Business Dashboard: View total services, pending requests, connected customers, and revenue insights.

### 3. Mechanic
- Mechanic Profile: Dedicated dashboard and profile management for mechanics.
- Assigned Workload: View active service requests, vehicle repair logs, and update job status.
- Garage Association: Send and receive garage connection requests to work with multiple garages.

### 4. System Administrator
- Centralized Control Panel: Monitor overall app metrics including total registered users, garages, mechanics, and service records.
- User & Service Moderation: Manage customers, mechanics, garage listings, and active service entries across the platform.

---

## Tech Stack & Dependencies

- Programming Language: Java
- UI Framework: Android XML with Material Design 3 UI Components
- Architecture: Multi-Role Modular Activity-Based Architecture
- Database: Local SQLite Database (DatabaseHelper)
- Payment Gateway: Razorpay SDK (com.razorpay:checkout:1.6.38)
- Build System: Gradle (Kotlin DSL build.gradle.kts)
- Minimum Android SDK: API Level 24 (Android 7.0 Nougat)
- Target Android SDK: API Level 37 (Android 15 / Latest)

---

## App Flow

```text
[ Splash Screen ] -> [ Onboarding Screen ] -> [ Role Selection ]
                                                    |
       +--------------------+-----------------------+-----------------------+
       |                    |                       |                       |
       v                    v                       v                       v
  [ Customer ]       [ Garage Owner ]          [ Mechanic ]            [ Admin ]
  - Vehicles List    - Service Control         - Task Logs            - System Overview
  - Warranty Tracker - Customer Records        - Garage Requests      - User Moderation
  - Razorpay Pay     - Parts Warranty          - Activity Details     - Garage Management
```

---

## Prerequisites

Before running the project, ensure you have the following installed:

- Android Studio (Ladybug / Jellyfish / 2024.2.1 or newer)
- JDK 11 or higher
- Android SDK API Level 34+ installed
- Git installed on your system
- A physical Android device or Emulator running Android 7.0 (API 24) or higher

---

## Getting Started & Installation

### Step 1: Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/AutoCare.git
cd AutoCare
```

### Step 2: Open in Android Studio
1. Launch Android Studio.
2. Select Open and choose the AutoCare project folder.
3. Allow Gradle to sync dependencies automatically.

### Step 3: Configure Razorpay Key
Open `RazorpayConfig.java` and replace the placeholder API key with your active Razorpay Key:

```java
public class RazorpayConfig {
    public static final String KEY_ID = "rzp_test_YOUR_KEY_HERE";
}
```

### Step 4: Build & Run
1. Connect your Android device or start an emulator.
2. Click the Run button or press Shift + F10 in Android Studio.

---

## Database Schema Summary

The app utilizes a SQLite database handled by `DatabaseHelper.java` containing the following core tables:
- `users` (User credentials, roles, email, phone)
- `vehicles` (Customer vehicle details)
- `garages` (Garage profiles & locations)
- `services` (Service records, status, total cost)
- `replaced_parts` (Part names, prices, warranty details)
- `garage_requests` (Connection requests between garages, customers, and mechanics)

---

## Contributing

Contributions are welcome! Feel free to submit a pull request or open an issue.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AwesomeFeature`)
3. Commit your Changes (`git commit -m 'Add some AwesomeFeature'`)
4. Push to the Branch (`git push origin feature/AwesomeFeature`)
5. Open a Pull Request

---

## License

Distributed under the MIT License.
