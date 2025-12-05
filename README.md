# 🍽️ Bistro Management System (BMS) - 

> **Group:** 6  
> **Status:** Phase 2 (Prototype Complete)

## 📖 Overview
The **Bistro Management System** is a client-server application designed to manage restaurant orders efficiently. This repository contains the source code for **Phase 1 and Phase 2** of the project, demonstrating a robust architecture, database connectivity, and a graphical user interface (GUI).

The current prototype allows a client to connect to a central server, fetch real-time order data from a MySQL database, and perform updates with strict validation logic.

## 🚀 Key Features

### 🖥️ Client-Side
- **JavaFX GUI:** A user-friendly interface to view and manage orders.
- **Real-Time Connection:** Connects to the server via TCP/IP (Input for IP/Port).
- **Data Visualization:** Displays orders in a dynamic table.
- **Order Management:**
  - Select an order to edit details.
  - Update `Order Date` and `Number of Guests`.
- **Validation Logic:** Prevents invalid inputs (e.g., date logic, guest limits) before sending data to the server.
- **Multithreading:** Network operations run on background threads to keep the UI responsive.

### ⚙️ Server-Side
- **Connection Monitor:** A live GUI table showing all connected clients (IP, Hostname, and Client ID).
- **Status Tracking:** Detects and displays real-time connection status (Connected/Disconnected/Crashed).
- **Database Handler:** Singleton pattern implementation for efficient MySQL connection management.
- **Graceful Shutdown:** Handles unexpected client disconnections without crashing.

## 🛠️ Tech Stack & Architecture

* **Language:** Java 
* **GUI Framework:** JavaFX
* **Network Framework:** OCSF (Object Client Server Framework)
* **Database:** MySQL
* **Serialization:** **Kryo** (EsotericSoftware)

### ⚡ Special Feature: Kryo Serialization
Unlike standard Java serialization, we implemented **Kryo** for network communication.
* **Why?** Kryo is significantly faster and produces smaller byte sizes, reducing network load.
* **How?** All objects (Messages, Orders) are serialized into byte arrays before transmission and deserialized on the receiving end using a custom `KryoUtil` helper class.

### 🏗️ Architecture
* **Framework:** OCSF (Object Client Server Framework) for TCP/IP communication.
* **Design Pattern:** MVC (Model-View-Controller) separation on the client side.
* **Concurrency:** extensive use of `Threads` and `Platform.runLater` to ensure thread-safety between network logic and JavaFX UI.

  ▶️ How to Run The prototype 
Follow these steps to launch the system in your local environment.

🟢 Step 1: Start the Server
Run the Server JAR file in the computer with the DB.

Verification: The Bistro Server Manager window will appear, waiting for connections.

🔵 Step 2: Start the Client

Action: In the window that opens, enter the Server IP (use localhost for local testing) and Port 5555.

Click the Connect button.

Once the status turns Green, click Load Orders to fetch data.

🔮 Future Roadmap (Phase 3)
This prototype sets the foundation for the full Bistro Management System. The upcoming Phase 3 will introduce comprehensive restaurant management features like :

📋 Full Menu Management: Add, remove, and edit all the operations in the UI's.

👥 User Roles & Permissions: Distinct interfaces for Managers, Customers and Others.

📊 Reports & Analytics: Generate monthly revenue reports.

📍 Visual Table Map:  managing table reservations.

🔔 Notifications: Real-time alerts for order readiness and cancellations.
