# 🍽️ Bistro Management System (BMS)

> **Group:** 6  
> **Status:** Phase 3 (Final Version / Production Ready)

## 📖 Overview
The **Bistro Management System (BMS)** is a comprehensive client-server application designed to digitize and automate restaurant operations.

While Phase 2 focused on the communication prototype, **Phase 3** introduces a fully functional system with role-based access, intelligent table allocation, automated notifications (Email/SMS simulation), and management reporting tools.

## 🚀 New Features in Phase 3

### 👤 Role-Based Access Control
The system now supports distinct interfaces for different user types:
* **Casual Diners:** Walk-in registration, digital menu viewing, and manual check-in.
* **Subscribers:** Easy login, order history, **Digital QR Card** generation, and "Smart Check-in".
* **Representatives/Managers:** Table management, schedule configuration, and report generation.

### 📅 Advanced Reservation System
* **Smart Capacity Check:** The server utilizes a "Best Fit" algorithm to assign tables based on party size and time slots, ensuring optimal restaurant utilization.
* **Waiting List Logic:** If the restaurant is full, users are added to a prioritized waiting list and automatically notified when a table becomes available.
* **Smart Check-In:** Subscribers can select their specific active order from a dropdown list upon arrival.

### 🔔 Automated Notification Service
* **Email Integration:** Uses SMTP (Gmail) to send real emails for:
    * Reservation Confirmations
    * Registration Welcome Messages
    * 2-Hour Arrival Reminders
    * Lost Code Recovery
* **Background Scheduler:** A server-side thread runs every minute to:
    * Send reminders 2 hours before bookings.
    * Auto-cancel "No-Show" orders (15 min late).
    * Clean up expired waiting list entries.

### 📊 Management & Analytics
* **Monthly Reports:** Managers can generate performance reports (Orders/Revenue) for specific months.
* **Schedule Management:** Define opening hours, special holidays, and closure dates dynamically.

---

## 🛠️ Tech Stack & Architecture

* **Language:** Java (JDK 17+)
* **GUI Framework:** JavaFX
* **Network Framework:** OCSF (Object Client Server Framework)
* **Database:** MySQL
* **Serialization:** **Kryo** (High-performance object graph serialization)
* **Email:** JavaMail API (javax.mail)
* **QR Generation:** QRServer API

### 🏗️ Architectural Highlights
* **Multithreading:** Extensive use of `ScheduledExecutorService` for background maintenance and `Platform.runLater` for UI responsiveness.
* **MVC Pattern:** Strict separation of logic (Controllers), data (Entities), and view (FXML/JavaFX).
* **Robust Error Handling:** The server handles client crashes gracefully, and the email service includes fallback mechanisms if credentials are missing.

---

### ▶️ How to Run

Follow these steps to launch the system in your local environment.

#### 1. Database Setup
Ensure your MySQL server is running and the `bistro_db` schema is imported.

#### 2. Configuration (Email Service)
To enable email features, create a `.env` file in the Server project root with the following keys:
```properties
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

#### 3. Start the Server
Run the Server JAR or `BistroServer.java`.

* **Verification:** The Server Console will appear, showing the database connection status and loaded configurations.

#### 4. Start the Client
Run the Client JAR or `ClientUI.java`.

1. Enter Server IP (use `localhost`) and Port `5555`.
2. **Select Location Mode:**
    * **In Restaurant:** Full access (Check-in enabled).
    * **Remote:** Restricted access (Reservations only).
3. Choose your Role (Casual, Subscriber, or Manager) to begin.

---

## 🔮 Project Conclusion
This project demonstrates a scalable, full-stack Java solution. It successfully handles concurrent clients,
manages persistent data via MySQL, and provides a seamless user experience through a responsive GUI and automated background services.
