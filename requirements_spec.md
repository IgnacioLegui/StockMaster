# Software Requirements Specification (SRS)
**Project Name:** StockMaster  
**Version:** 1.0  
**Author:** Ignacio Leguizamon  
**Date:** 2026-02-10  

---

## 1. Introduction

### 1.1 Purpose
The purpose of this document is to define the functional and non-functional requirements for **StockMaster**, a desktop-based inventory management system designed for small to medium-sized businesses.

### 1.2 Scope
StockMaster provides a streamlined interface for managing product inventory, suppliers, and product categories. It operates as a local desktop application using a persistent SQLite database, ensuring data privacy and offline accessibility. The system includes reporting capabilities via PDF export and visual data analytics through a dashboard.

---

## 2. Functional Requirements

### 2.1 Product Management
- **FR-01**: The system shall allow users to **Create** new products with fields: Name, Category, Price, Stock Quantity, and Supplier.
- **FR-02**: The system shall allow users to **Read** (view) a list of all products in a tabular format.
- **FR-03**: The system shall allow users to **Update** existing product details.
- **FR-04**: The system shall allow users to **Delete** products from the inventory.
- **FR-05**: The system shall implement **Search** functionality to filter products by name or category.

### 2.2 Category & Supplier Management
- **FR-06**: The system shall manage Product Categories (Create, Read, Update, Delete).
- **FR-07**: The system shall manage Suppliers (Create, Read, Update, Delete) with contact details (Email, Phone).

### 2.3 Dashboard & Analytics
- **FR-08**: The dashboard shall display **Key Performance Indicators (KPIs)**:
  - Total Products in stock.
  - Total Inventory Value (Price * Stock).
  - Low Stock Alerts (Products with stock < 5).
- **FR-09**: The dashboard shall display a **Bar Chart** visualizing product distribution across categories.

### 2.4 Reporting
- **FR-10**: The system shall generate a **PDF Inventory Report** listing all current products and stock levels.
- **FR-11**: The PDF report shall include a timestamp and a summary of total items.

---

## 3. Non-Functional Requirements

### 3.1 Usability (UI/UX)
- **NFR-01**: The interface shall be designed with a modern **Dark Theme** to reduce eye strain.
- **NFR-02**: The application shall use intuitive navigation (Sidebar) for easy access to different modules.

### 3.2 Performance
- **NFR-03**: Product search results shall appear in under 200ms for datasets of up to 10,000 items.
- **NFR-04**: Use of SQLite shall ensure lightweight operations with minimal system resource usage.

### 3.3 Reliability & Persistence
- **NFR-05**: Data shall be persisted locally in a **SQLite database**.
- **NFR-06**: The database file (`stockmaster.db`) shall survive application restarts and system reboots.
- **NFR-07**: The application configuration shall auto-detect the AppData folder for secure database storage.

### 3.4 Compatibility
- **NFR-08**: The application shall be compatible with **Windows 10/11** environments.
- **NFR-09**: The installer shall create a desktop shortcut and Start Menu entry.

---

## 4. Technical Stack
- **Language**: Java 17+ (JDK 24 Compatible)
- **GUI Framework**: JavaFX
- **Database**: SQLite (via JDBC)
- **Build Tool**: Maven
- **PDF Engine**: Apache PDFBox
- **Installer**: jpackage (WiX Toolset)

---

## 5. Future Enhancements (Roadmap)
- [ ] User Authentication (Login/Register).
- [ ] Cloud Sync / Multi-user support.
- [ ] Barcode Scanner integration.
- [ ] Sales styling & Invoice generation.
