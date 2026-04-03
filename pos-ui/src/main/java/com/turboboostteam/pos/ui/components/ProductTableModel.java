package com.turboboostteam.pos.ui.components;

import com.turboboostteam.pos.model.product.Product;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ProductTableModel extends AbstractTableModel {

    private final String[] columns = {
            "ID", "Name", "Barcode", "Price", "Type", "Category", "Active"
    };

    private List<Product> products;

    public ProductTableModel(List<Product> products) {
        this.products = products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        fireTableDataChanged();
    }

    public Product getProductAt(int row) {
        return products.get(row);
    }

    @Override
    public int getRowCount()    { return products.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Product p = products.get(row);
        return switch (col) {
            case 0 -> p.getId();
            case 1 -> p.getName();
            case 2 -> p.getBarcode();
            case 3 -> p.getPrice().toString();
            case 4 -> p.getProductType();
            case 5 -> p.getCategory() != null
                    ? p.getCategory().getName() : "—";
            case 6 -> p.isActive() ? "✅" : "❌";
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false; // editing done via form
    }
}