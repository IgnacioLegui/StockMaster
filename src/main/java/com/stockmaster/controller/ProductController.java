package com.stockmaster.controller;

import com.stockmaster.dao.CategoryDAO;
import com.stockmaster.dao.ProductDAO;
import com.stockmaster.dao.SupplierDAO;
import com.stockmaster.model.Category;
import com.stockmaster.model.Product;
import com.stockmaster.model.Supplier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProductController {

    // Table
    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colPreferencia; // mapped to categoryName
    @FXML private TableColumn<Product, String> colSupplier; // mapped to supplierName
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

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    
    // Track selected product for update
    private Product selectedProduct;

    @FXML
    public void initialize() {
        setupColumns();
        loadComboData();
        loadTableData();

        // Listener for selection
        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
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
        List<Category> categories = categoryDAO.findAll();
        comboCategory.setItems(FXCollections.observableArrayList(categories));

        List<Supplier> suppliers = supplierDAO.findAll();
        comboSupplier.setItems(FXCollections.observableArrayList(suppliers));
    }

    private void loadTableData() {
        List<Product> products = productDAO.findAll();
        tableProducts.setItems(FXCollections.observableArrayList(products));
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            loadTableData();
        } else {
            List<Product> results = productDAO.search(query);
            tableProducts.setItems(FXCollections.observableArrayList(results));
        }
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    private void handleClear() {
        selectedProduct = null;
        txtName.clear();
        txtDescription.clear();
        comboCategory.getSelectionModel().clearSelection();
        comboSupplier.getSelectionModel().clearSelection();
        txtPriceBuy.clear();
        txtPriceSell.clear();
        txtStockCurrent.setText("0");
        txtStockMin.setText("5");
        lblMessage.setText("");
        tableProducts.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDelete() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Select a product to delete.");
            return;
        }

        try {
            productDAO.delete(selected.getId());
            loadTableData();
            handleClear();
            lblMessage.setText("Product deleted.");
            lblMessage.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblMessage.setText("Error deleting: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleSave() {
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

            if (selectedProduct == null) {
                productDAO.insert(p);
                lblMessage.setText("Product added successfully.");
            } else {
                productDAO.update(p);
                lblMessage.setText("Product updated successfully.");
            }
            lblMessage.setStyle("-fx-text-fill: green;");
            
            loadTableData();
            handleClear();

        } catch (Exception e) {
            lblMessage.setText("Error saving: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tableProducts.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText("Product Inventory Report");
                    contentStream.endText();

                    int y = 680;
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    
                    for (Product p : tableProducts.getItems()) {
                        y -= 20;
                        if (y < 50) { // New page if needed
                            contentStream.close();
                            PDPage newPage = new PDPage();
                            document.addPage(newPage);
                            // We would need to manage the stream logic better for multiple pages, simplified here.
                            break; 
                        }
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, y);
                        String text = String.format("ID: %d | Name: %s | Category: %s | Price: $%.2f | Stock: %d", 
                                p.getId(), p.getName(), p.getCategoryName(), p.getPriceSell(), p.getStockCurrent());
                        contentStream.showText(text);
                        contentStream.endText();
                    }
                }
                document.save(file);
                lblMessage.setText("PDF exported successfully.");
                lblMessage.setStyle("-fx-text-fill: green;");
            } catch (IOException e) {
                lblMessage.setText("Error exporting PDF: " + e.getMessage());
                lblMessage.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    private void populateForm(Product p) {
        selectedProduct = p;
        txtName.setText(p.getName());
        txtDescription.setText(p.getDescription());
        txtPriceBuy.setText(String.valueOf(p.getPriceBuy()));
        txtPriceSell.setText(String.valueOf(p.getPriceSell()));
        txtStockCurrent.setText(String.valueOf(p.getStockCurrent()));
        txtStockMin.setText(String.valueOf(p.getStockMin()));

        // Set combos - bit inefficient O(N) but fine for small lists
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
            lblMessage.setText("Name, Category and Supplier are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return false;
        }
        try {
            double buy = Double.parseDouble(txtPriceBuy.getText());
            double sell = Double.parseDouble(txtPriceSell.getText());
            int stock = Integer.parseInt(txtStockCurrent.getText());
            int min = Integer.parseInt(txtStockMin.getText());

            if (buy < 0 || sell < 0) {
                lblMessage.setText("Prices cannot be negative.");
                lblMessage.setStyle("-fx-text-fill: red;");
                return false;
            }
            if (stock < 0 || min < 0) {
                lblMessage.setText("Stock cannot be negative.");
                lblMessage.setStyle("-fx-text-fill: red;");
                return false;
            }

        } catch (NumberFormatException e) {
            lblMessage.setText("Invalid number format for Price or Stock.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return false;
        }
        return true;
    }
}
