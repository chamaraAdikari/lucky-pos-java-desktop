package com.turboboostteam.pos.ui.components;

import com.turboboostteam.pos.model.sale.SaleItem;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CartTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Product", "Qty", "Unit Price", "Tax", "Line Total"
    };

    private List<SaleItem> items;

    public CartTableModel(List<SaleItem> items) {
        this.items = items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public SaleItem getItemAt(int row) {
        return items.get(row);
    }

    @Override public int getRowCount()    { return items.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        SaleItem item = items.get(row);
        return switch (col) {
            case 0 -> item.getProductName();
            case 1 -> item.getQuantity();
            case 2 -> item.getUnitPrice().toString();
            case 3 -> item.getTaxAmount().toString();
            case 4 -> item.getLineTotal().toString();
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}