package com.stockmaster.controller;

import com.stockmaster.dao.BatchDAO;
import com.stockmaster.hardware.BarcodeDetector;
import com.stockmaster.hardware.ReceiptPrinter;
import com.stockmaster.model.Batch;
import com.stockmaster.service.ProductService;
import com.stockmaster.service.CategoryService;
import com.stockmaster.service.SupplierService;
import com.stockmaster.model.Category;
import com.stockmaster.model.Product;
import com.stockmaster.model.Supplier;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductController {

    // Table
    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colPreferencia;
    @FXML private TableColumn<Product, String> colSupplier;
    @FXML private TableColumn<Product, Double> colPriceBuy;
    @FXML private TableColumn<Product, Double> colPriceSell;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Integer> colStockMin;

    // Form
    @FXML private TextField txtSearch;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Category> comboCategory;
    @FXML private ComboBox<Supplier> comboSupplier;
    @FXML private TextField txtPriceBuy;
    @FXML private TextField txtPriceSell;
    @FXML private TextField txtStockCurrent;
    @FXML private TextField txtStockMin;
    @FXML private Label lblMessage;
    @FXML private TextField txtBarcode;

    // Batch Panel
    @FXML private VBox batchPanel;
    @FXML private HBox batchForm;
    @FXML private Label lblBatchTitle;
    @FXML private TableView<Batch> tableBatches;
    @FXML private TableColumn<Batch, String> colBatchLot;
    @FXML private TableColumn<Batch, String> colBatchExpiry;
    @FXML private TableColumn<Batch, Integer> colBatchQty;
    @FXML private TableColumn<Batch, Double> colBatchCost;
    @FXML private TableColumn<Batch, String> colBatchReceived;
    @FXML private TableColumn<Batch, String> colBatchStatus;
    @FXML private TextField txtLotNumber;
    @FXML private DatePicker dpExpiry;
    @FXML private TextField txtBatchQty;
    @FXML private TextField txtBatchCost;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final SupplierService supplierService = new SupplierService();
    private final BatchDAO batchDAO = new BatchDAO();
    
    private Product selectedProduct;

    @FXML
    public void initialize() {
        setupColumns();
        setupBatchColumns();
        loadComboData();
        loadTableData();

        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
                loadBatches(newSelection);
            } else {
                hideBatchPanel();
            }
        });

        // Attach barcode scanner detection to search field
        BarcodeDetector detector = new BarcodeDetector(this::onBarcodeScanned);
        detector.attachTo(txtSearch);
    }

    private void onBarcodeScanned(String barcode) {
        Product found = new com.stockmaster.dao.ProductDAO().findByBarcode(barcode);
        if (found != null) {
            tableProducts.getSelectionModel().clearSelection();
            for (Product p : tableProducts.getItems()) {
                if (p.getId() == found.getId()) {
                    tableProducts.getSelectionModel().select(p);
                    tableProducts.scrollTo(p);
                    showSuccess("Barcode found: " + found.getName());
                    return;
                }
            }
        }
        showError("Barcode not found: " + barcode);
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPreferencia.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colPriceBuy.setCellValueFactory(new PropertyValueFactory<>("priceBuy"));
        colPriceSell.setCellValueFactory(new PropertyValueFactory<>("priceSell"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockCurrent"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMin"));
    }

    private void loadComboData() {
        List<Category> categories = categoryService.findAll();
        comboCategory.setItems(FXCollections.observableArrayList(categories));

        List<Supplier> suppliers = supplierService.findAll();
        comboSupplier.setItems(FXCollections.observableArrayList(suppliers));
    }

    private void loadTableData() {
        List<Product> products = productService.findAll();
        tableProducts.setItems(FXCollections.observableArrayList(products));
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            loadTableData();
        } else {
            List<Product> results = productService.search(query);
            tableProducts.setItems(FXCollections.observableArrayList(results));
        }
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    public void handleClear() {
        selectedProduct = null;
        txtName.clear();
        txtDescription.clear();
        comboCategory.getSelectionModel().clearSelection();
        comboSupplier.getSelectionModel().clearSelection();
        txtPriceBuy.clear();
        txtPriceSell.clear();
        txtStockCurrent.setText("0");
        txtStockMin.setText("5");
        if (txtBarcode != null) txtBarcode.clear();
        clearMessage();
        tableProducts.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleDelete() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a product to delete.");
            return;
        }

        try {
            productService.delete(selected.getId());
            loadTableData();
            handleClear();
            showSuccess("Product deleted.");
        } catch (Exception e) {
            showError("Error deleting: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (!validateInput()) return;

        try {
            Product p = new Product();
            if (selectedProduct != null) {
                p.setId(selectedProduct.getId());
            }
            p.setName(txtName.getText());
            p.setDescription(txtDescription.getText());
            p.setCategoryId(comboCategory.getValue().getId());
            p.setSupplierId(comboSupplier.getValue().getId());
            p.setPriceBuy(Double.parseDouble(txtPriceBuy.getText()));
            p.setPriceSell(Double.parseDouble(txtPriceSell.getText()));
            p.setStockCurrent(Integer.parseInt(txtStockCurrent.getText()));
            p.setStockMin(Integer.parseInt(txtStockMin.getText()));
            if (txtBarcode != null && !txtBarcode.getText().isEmpty()) {
                p.setBarcode(txtBarcode.getText().trim());
            }

            if (selectedProduct == null) {
                productService.insert(p);
                showSuccess("Product added successfully.");
            } else {
                productService.update(p);
                showSuccess("Product updated successfully.");
            }
            
            loadTableData();
            handleClear();

        } catch (Exception e) {
            showError("Error saving: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== CSV EXPORT ====================
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.setInitialFileName("products_export.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(tableProducts.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")))) {
                // BOM for Excel UTF-8 compatibility
                writer.print('\uFEFF');
                // Header
                writer.println("Name,Description,Category,Supplier,BuyPrice,SellPrice,Stock,MinStock");

                for (Product p : tableProducts.getItems()) {
                    writer.printf("%s,%s,%s,%s,%.2f,%.2f,%d,%d%n",
                            escapeCsv(p.getName()),
                            escapeCsv(p.getDescription()),
                            escapeCsv(p.getCategoryName()),
                            escapeCsv(p.getSupplierName()),
                            p.getPriceBuy(),
                            p.getPriceSell(),
                            p.getStockCurrent(),
                            p.getStockMin());
                }

                showSuccess("CSV exported: " + file.getName());
            } catch (Exception e) {
                showError("Error exporting CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ==================== CSV IMPORT ====================
    @FXML
    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(tableProducts.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    showError("CSV file is empty.");
                    return;
                }

                // Remove BOM if present
                if (headerLine.startsWith("\uFEFF")) {
                    headerLine = headerLine.substring(1);
                }

                // Build lookup maps for category and supplier names
                List<Category> categories = categoryService.findAll();
                List<Supplier> suppliers = supplierService.findAll();

                List<Product> toImport = new ArrayList<>();
                String line;
                int lineNum = 1;
                int errors = 0;

                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 6) {
                        errors++;
                        continue;
                    }

                    try {
                        Product p = new Product();
                        p.setName(parts[0].trim());
                        p.setDescription(parts.length > 1 ? parts[1].trim() : "");

                        // Match category by name (or use first available)
                        String catName = parts.length > 2 ? parts[2].trim() : "";
                        int catId = categories.stream()
                                .filter(c -> c.getName().equalsIgnoreCase(catName))
                                .mapToInt(Category::getId)
                                .findFirst()
                                .orElse(categories.isEmpty() ? 0 : categories.get(0).getId());
                        p.setCategoryId(catId);

                        // Match supplier by name
                        String supName = parts.length > 3 ? parts[3].trim() : "";
                        int supId = suppliers.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(supName))
                                .mapToInt(Supplier::getId)
                                .findFirst()
                                .orElse(suppliers.isEmpty() ? 0 : suppliers.get(0).getId());
                        p.setSupplierId(supId);

                        p.setPriceBuy(parts.length > 4 ? Double.parseDouble(parts[4].trim()) : 0);
                        p.setPriceSell(parts.length > 5 ? Double.parseDouble(parts[5].trim()) : 0);
                        p.setStockCurrent(parts.length > 6 ? Integer.parseInt(parts[6].trim()) : 0);
                        p.setStockMin(parts.length > 7 ? Integer.parseInt(parts[7].trim()) : 5);

                        toImport.add(p);
                    } catch (NumberFormatException e) {
                        errors++;
                    }
                }

                // Batch insert
                int imported = 0;
                for (Product p : toImport) {
                    try {
                        productService.insert(p);
                        imported++;
                    } catch (Exception e) {
                        errors++;
                    }
                }

                loadTableData();
                loadComboData();
                
                String msg = String.format("Imported %d products.", imported);
                if (errors > 0) {
                    msg += String.format(" (%d rows skipped)", errors);
                }
                showSuccess(msg);

            } catch (Exception e) {
                showError("Error importing CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    // ==================== PDF EXPORT (Professional Multi-Page) ====================
    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.setInitialFileName("StockMaster_Report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tableProducts.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                List<Product> products = new ArrayList<>(tableProducts.getItems());
                
                float margin = 50;
                float yStart = 700;
                float rowHeight = 18;
                float pageWidth = PDRectangle.A4.getWidth();
                
                // Column widths
                float[] colWidths = {35, 120, 85, 85, 65, 65, 50, 55};
                String[] headers = {"ID", "Name", "Category", "Supplier", "Buy $", "Sell $", "Stock", "Min"};
                
                int totalPages = (int) Math.ceil((double) products.size() / 32);
                if (totalPages == 0) totalPages = 1;
                int productIndex = 0;
                
                for (int page = 1; page <= totalPages; page++) {
                    PDPage pdPage = new PDPage(PDRectangle.A4);
                    document.addPage(pdPage);
                    
                    try (PDPageContentStream cs = new PDPageContentStream(document, pdPage)) {
                        // === HEADER ===
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                        cs.beginText();
                        cs.newLineAtOffset(margin, 770);
                        cs.showText("StockMaster — Inventory Report");
                        cs.endText();
                        
                        cs.setFont(PDType1Font.HELVETICA, 9);
                        cs.beginText();
                        cs.newLineAtOffset(margin, 752);
                        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        cs.showText("Generated: " + dateStr + "  |  Total Products: " + products.size());
                        cs.endText();
                        
                        // Header line
                        cs.setStrokingColor(0.3f, 0.3f, 0.5f);
                        cs.setLineWidth(1);
                        cs.moveTo(margin, 745);
                        cs.lineTo(pageWidth - margin, 745);
                        cs.stroke();
                        
                        // === TABLE HEADER ===
                        float tableTop = 725;
                        
                        // Header background
                        cs.setNonStrokingColor(0.15f, 0.15f, 0.25f);
                        cs.addRect(margin, tableTop - rowHeight, pageWidth - 2 * margin, rowHeight);
                        cs.fill();
                        
                        // Header text
                        cs.setNonStrokingColor(1f, 1f, 1f);
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 8);
                        float xPos = margin + 4;
                        for (int i = 0; i < headers.length; i++) {
                            cs.beginText();
                            cs.newLineAtOffset(xPos, tableTop - 13);
                            cs.showText(headers[i]);
                            cs.endText();
                            xPos += colWidths[i];
                        }
                        
                        // === TABLE ROWS ===
                        cs.setFont(PDType1Font.HELVETICA, 8);
                        float yRow = tableTop - rowHeight;
                        int rowsOnPage = 0;
                        
                        while (productIndex < products.size() && rowsOnPage < 32) {
                            Product p = products.get(productIndex);
                            yRow -= rowHeight;
                            
                            // Alternating row background
                            if (rowsOnPage % 2 == 0) {
                                cs.setNonStrokingColor(0.95f, 0.95f, 0.97f);
                                cs.addRect(margin, yRow, pageWidth - 2 * margin, rowHeight);
                                cs.fill();
                            }
                            
                            // Low stock highlight
                            if (p.getStockCurrent() <= p.getStockMin()) {
                                cs.setNonStrokingColor(1f, 0.9f, 0.9f);
                                cs.addRect(margin, yRow, pageWidth - 2 * margin, rowHeight);
                                cs.fill();
                            }
                            
                            cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                            xPos = margin + 4;
                            
                            String[] rowData = {
                                String.valueOf(p.getId()),
                                truncate(p.getName(), 20),
                                truncate(p.getCategoryName() != null ? p.getCategoryName() : "-", 14),
                                truncate(p.getSupplierName() != null ? p.getSupplierName() : "-", 14),
                                String.format("$%.2f", p.getPriceBuy()),
                                String.format("$%.2f", p.getPriceSell()),
                                String.valueOf(p.getStockCurrent()),
                                String.valueOf(p.getStockMin())
                            };
                            
                            for (int i = 0; i < rowData.length; i++) {
                                cs.beginText();
                                cs.newLineAtOffset(xPos, yRow + 5);
                                cs.showText(rowData[i]);
                                cs.endText();
                                xPos += colWidths[i];
                            }
                            
                            productIndex++;
                            rowsOnPage++;
                        }
                        
                        // === FOOTER ===
                        cs.setNonStrokingColor(0.5f, 0.5f, 0.6f);
                        cs.setFont(PDType1Font.HELVETICA, 8);
                        cs.beginText();
                        cs.newLineAtOffset(margin, 30);
                        cs.showText("StockMaster v2.0  •  Page " + page + " of " + totalPages);
                        cs.endText();
                        
                        // Footer line
                        cs.setStrokingColor(0.8f, 0.8f, 0.85f);
                        cs.setLineWidth(0.5f);
                        cs.moveTo(margin, 42);
                        cs.lineTo(pageWidth - margin, 42);
                        cs.stroke();
                    }
                }
                
                document.save(file);
                showSuccess("PDF exported: " + file.getName() + " (" + totalPages + " pages)");
                
            } catch (IOException e) {
                showError("Error exporting PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "…" : text;
    }

    // ==================== Form Helpers ====================
    private void populateForm(Product p) {
        selectedProduct = p;
        txtName.setText(p.getName());
        txtDescription.setText(p.getDescription());
        txtPriceBuy.setText(String.valueOf(p.getPriceBuy()));
        txtPriceSell.setText(String.valueOf(p.getPriceSell()));
        txtStockCurrent.setText(String.valueOf(p.getStockCurrent()));
        txtStockMin.setText(String.valueOf(p.getStockMin()));
        if (txtBarcode != null) txtBarcode.setText(p.getBarcode() != null ? p.getBarcode() : "");

        for (Category c : comboCategory.getItems()) {
            if (c.getId() == p.getCategoryId()) {
                comboCategory.setValue(c);
                break;
            }
        }
        for (Supplier s : comboSupplier.getItems()) {
            if (s.getId() == p.getSupplierId()) {
                comboSupplier.setValue(s);
                break;
            }
        }
    }

    private boolean validateInput() {
        if (txtName.getText().isEmpty() || 
            comboCategory.getValue() == null || 
            comboSupplier.getValue() == null) {
            showError("Name, Category and Supplier are required.");
            return false;
        }
        try {
            double buy = Double.parseDouble(txtPriceBuy.getText());
            double sell = Double.parseDouble(txtPriceSell.getText());
            int stock = Integer.parseInt(txtStockCurrent.getText());
            int min = Integer.parseInt(txtStockMin.getText());

            if (buy < 0 || sell < 0) {
                showError("Prices cannot be negative.");
                return false;
            }
            if (stock < 0 || min < 0) {
                showError("Stock cannot be negative.");
                return false;
            }

        } catch (NumberFormatException e) {
            showError("Invalid number format for Price or Stock.");
            return false;
        }
        return true;
    }

    // ==================== Toast-style Feedback ====================
    private void showSuccess(String msg) {
        lblMessage.setText("✓ " + msg);
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
        lblMessage.getStyleClass().add("toast-success");
    }

    private void showError(String msg) {
        lblMessage.setText("✕ " + msg);
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
        lblMessage.getStyleClass().add("toast-error");
    }

    private void clearMessage() {
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
    }

    // Public for keyboard shortcut access
    public void focusSearch() {
        if (txtSearch != null) txtSearch.requestFocus();
    }

    // ==================== POS Hardware ====================
    @FXML
    private void handlePrintTicket() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a product to print ticket.");
            return;
        }
        String ticket = ReceiptPrinter.formatProductTicket(selected);
        if (ReceiptPrinter.printToDefault(ticket)) {
            showSuccess("Ticket sent to printer.");
        } else {
            // Show in console as fallback
            System.out.println(ticket);
            showError("No printer found. Ticket printed to console.");
        }
    }

    @FXML
    private void handleOpenDrawer() {
        if (ReceiptPrinter.openCashDrawer()) {
            showSuccess("Cash drawer opened.");
        } else {
            showError("Failed to open cash drawer. No ESC/POS printer found.");
        }
    }

    // ==================== Batch Management ====================
    private void setupBatchColumns() {
        colBatchLot.setCellValueFactory(new PropertyValueFactory<>("lotNumber"));
        colBatchExpiry.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getExpiryDate();
            return new SimpleStringProperty(d != null ? d.toString() : "—");
        });
        colBatchQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colBatchCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colBatchReceived.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getReceivedDate();
            return new SimpleStringProperty(d != null ? d.toString() : "—");
        });
        colBatchStatus.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getExpiryStatus())
        );

        // Color status cells
        colBatchStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "EXPIRED" -> setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
                        case "Expiring Soon" -> setStyle("-fx-text-fill: #ffb347; -fx-font-weight: bold;");
                        case "OK" -> setStyle("-fx-text-fill: #00d764;");
                        default -> setStyle("-fx-text-fill: #888;");
                    }
                }
            }
        });
    }

    private void loadBatches(Product product) {
        List<Batch> batches = batchDAO.findByProductId(product.getId());
        tableBatches.setItems(FXCollections.observableArrayList(batches));
        lblBatchTitle.setText("📦 Batches for: " + product.getName());
        batchPanel.setManaged(true);
        batchPanel.setVisible(true);
    }

    private void hideBatchPanel() {
        batchPanel.setManaged(false);
        batchPanel.setVisible(false);
        batchForm.setManaged(false);
        batchForm.setVisible(false);
    }

    @FXML
    private void handleAddBatch() {
        if (selectedProduct == null) {
            showError("Select a product first.");
            return;
        }
        txtLotNumber.clear();
        dpExpiry.setValue(null);
        txtBatchQty.clear();
        txtBatchCost.clear();
        batchForm.setManaged(true);
        batchForm.setVisible(true);
    }

    @FXML
    private void handleSaveBatch() {
        if (selectedProduct == null) return;

        String lot = txtLotNumber.getText();
        if (lot == null || lot.trim().isEmpty()) {
            showError("Lot number is required.");
            return;
        }

        try {
            Batch batch = new Batch();
            batch.setProductId(selectedProduct.getId());
            batch.setLotNumber(lot.trim());
            batch.setExpiryDate(dpExpiry.getValue());
            batch.setQuantity(Integer.parseInt(txtBatchQty.getText().trim()));
            batch.setCostPrice(Double.parseDouble(txtBatchCost.getText().trim()));
            batch.setReceivedDate(LocalDate.now());

            batchDAO.insert(batch);
            loadBatches(selectedProduct);
            handleCancelBatch();
            showSuccess("Batch added: " + lot);
        } catch (NumberFormatException e) {
            showError("Invalid quantity or cost.");
        } catch (Exception e) {
            showError("Error adding batch: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteBatch() {
        Batch selected = tableBatches.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a batch to delete.");
            return;
        }
        try {
            batchDAO.delete(selected.getId());
            loadBatches(selectedProduct);
            showSuccess("Batch deleted.");
        } catch (Exception e) {
            showError("Error deleting batch: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBatch() {
        batchForm.setManaged(false);
        batchForm.setVisible(false);
    }
}
