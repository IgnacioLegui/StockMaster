# StockMaster 📦

**StockMaster** is a professional desktop inventory management system built with **JavaFX** and **SQLite**. It provides a complete solution for stock control, point of sale, purchase orders, reporting, and user management — all in a sleek dark-themed bilingual interface.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![License](https://img.shields.io/github/license/IgnacioLegui/StockMaster)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-4.0-blue)

---

## 🚀 Key Features

### 📊 Dashboard
- **Real-time KPIs**: Total products, inventory value, low stock alerts, and expiring batches.
- **Visual Analytics**: Interactive bar charts for category distribution.

### 🛒 Point of Sale (POS)
- **Shopping Cart**: Add products by search or barcode scanning.
- **Tax Calculation**: Automatic IVA calculation per product.
- **Payment Methods**: Cash, credit card, debit card, and transfer.
- **Receipt Printing**: ESC/POS thermal printer support.
- **Sales History**: View recent transactions and reprint receipts.

### 📦 Product Management
- **Complete CRUD**: Add, edit, delete, and search products.
- **Barcode Support**: Scan or type barcodes for quick lookup.
- **Batch/Lot Tracking**: Track lot numbers, expiry dates, and batch quantities.
- **CSV Import/Export**: Bulk import products from CSV files.
- **PDF Export**: Generate professional inventory reports.
- **Stock Alerts**: Automatic visual indicators for low stock items.

### 📋 Purchase Orders
- **Order Management**: Create, receive, and cancel purchase orders.
- **Automatic Stock Update**: Receiving an order automatically updates product stock.
- **Item Tracking**: Per-item quantity, unit cost, and line totals.

### 📈 Reports
- **Sales Summary**: Revenue, transaction count, and average sale value.
- **Top Products**: Best-selling products by quantity and revenue.
- **Stock Movements**: Full audit trail of stock in/out movements.
- **Profit Margins**: Buy vs. sell price analysis.
- **Expiring Soon**: Products with batches expiring within 30 days.
- **Low Stock**: Products below minimum stock threshold.
- **PDF & Excel Export**: Export any report with one click.

### 🗂 Organization
- **Categories**: Organize your inventory logically.
- **Suppliers**: Manage vendor contact information (name, contact, phone, email).

### 👤 User Management
- **Authentication**: Secure login with hashed passwords (SHA-256).
- **Roles**: ADMIN and CASHIER roles with different permissions.
- **Session Management**: Track active user and role-based access.

### 🌐 Bilingual Interface (i18n)
- **Full Spanish/English support** across all views, buttons, labels, and columns.
- **Instant switching** — change language from the sidebar, views reload automatically.

---

## 🛠️ Technology Stack

| Component       | Technology                     |
|----------------|--------------------------------|
| Language        | Java 17+ (JDK 24 Compatible)  |
| UI Framework    | JavaFX + FXML                  |
| Database        | SQLite (local persistence)     |
| Build Tool      | Maven                          |
| Installer       | jpackage (WiX Toolset)         |
| i18n            | Java ResourceBundle            |
| PDF Generation  | iTextPDF                       |
| Excel Export    | Apache POI                     |
| Hardware        | ESC/POS printers (javax.print) |

---

## 📥 Installation

### Option 1: Download Installer
Download the latest Windows installer (`.exe`) from the [Releases](https://github.com/IgnacioLegui/StockMaster/releases) page or the [Landing Page](https://stockmaster-site.vercel.app).

> ⚠️ **Windows SmartScreen**: Since this is a new open-source app, Windows may show a SmartScreen warning. Click **More info** → **Run anyway** to install.

### Option 2: Build from Source
1. **Clone the repository**:
   ```bash
   git clone https://github.com/IgnacioLegui/StockMaster.git
   cd StockMaster
   ```
2. **Build with Maven**:
   ```bash
   mvn clean package
   ```
3. **Run the application**:
   ```bash
   java -jar target/StockMaster-1.0-SNAPSHOT.jar
   ```
4. **Build installer** (optional, requires JDK 14+ and WiX Toolset):
   ```powershell
   .\build_installer.ps1
   ```

### Default Login
| Username | Password | Role   |
|----------|----------|--------|
| admin    | admin    | ADMIN  |

---

## 📁 Project Structure

```
src/main/java/com/stockmaster/
├── auth/          # Session management
├── controller/    # JavaFX controllers (Dashboard, Products, Sales, PO, Reports, Users)
├── dao/           # Data access objects (SQLite queries)
├── db/            # Database connection manager
├── hardware/      # Barcode detection, receipt printing
├── i18n/          # Language manager (ES/EN)
├── model/         # Data models (Product, Sale, PurchaseOrder, Batch, User, etc.)
└── service/       # Business logic (backup, export, CSV import)

src/main/resources/
├── css/           # Dark theme stylesheet
├── i18n/          # Translation files (messages_en.properties, messages_es.properties)
├── images/        # App icons
├── sql/           # Database schema
└── view/          # FXML layout files
```

---

## 📚 Documentation

- [📄 **Software Requirements Specification (SRS)**](./StockMaster_SRS_Ignacio.pdf)
- [📊 **Database Schema (EER Diagram)**](./db-graph.pdf)

---

## 📬 Contact Developer

**Ignacio Leguizamon**  
Software Developer

[![Website](https://img.shields.io/badge/Website-ignacioleguizamon.site-blueviolet?style=flat&logo=safari)](https://ignacioleguizamon.site)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=flat&logo=linkedin)](https://www.linkedin.com/in/ignaciolegui/)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=flat&logo=github)](https://github.com/IgnacioLegui)

---

> **Note**: This project is open-source under the MIT License. Contributions are welcome!
