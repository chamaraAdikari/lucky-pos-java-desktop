package com.turboboostteam.pos.service.command;

import com.turboboostteam.pos.dao.SaleDao;
import com.turboboostteam.pos.model.sale.SaleItem;
import com.turboboostteam.pos.model.sale.SaleTransaction;
import com.turboboostteam.pos.model.enums.SaleStatus;
import com.turboboostteam.pos.service.AuditLogger;
import com.turboboostteam.pos.service.InventoryService;
import com.turboboostteam.pos.service.SessionManager;

import java.util.List;

// Command Pattern — void a completed sale
public class VoidSaleCommand implements SaleCommand {

    private final SaleDao saleDao;
    private final InventoryService inventoryService;
    private final Long saleId;
    private final String saleNumber;
    private List<SaleItem> items;

    public VoidSaleCommand(SaleDao saleDao,
                           InventoryService inventoryService,
                           Long saleId,
                           String saleNumber) {
        this.saleDao          = saleDao;
        this.inventoryService = inventoryService;
        this.saleId           = saleId;
        this.saleNumber       = saleNumber;
    }

    @Override
    public void execute() {
        String username = SessionManager.getInstance()
                .getCurrentUser().getUsername();

        // 1. Load sale items before voiding
        items = saleDao.findItemsBySaleId(saleId);

        // 2. Void the sale in DB
        saleDao.voidSale(saleId);

        // 3. Restore stock for each item
        items.forEach(item ->
                inventoryService.adjustStock(
                        item.getProductId(),
                        getRestoredQty(item),
                        "Stock restored — void of "
                                + saleNumber,
                        username
                ));

        // 4. Audit log
        AuditLogger.log("SALE_VOIDED", username,
                "saleId=" + saleId
                        + " saleNumber=" + saleNumber);
    }

    @Override
    public void undo() {
        // Re-commit the sale
        saleDao.updateSaleStatus(saleId,
                SaleStatus.COMMITTED.name(),
                null);

        // Re-decrement stock
        String username = SessionManager.getInstance()
                .getCurrentUser().getUsername();
        if (items != null) {
            items.forEach(item ->
                    inventoryService.decrementStock(
                            item.getProductId(),
                            item.getQuantity(),
                            username));
        }

        AuditLogger.log("VOID_UNDONE", username,
                "saleId=" + saleId);
    }

    private int getRestoredQty(SaleItem item) {
        return inventoryService
                .getStockForProduct(item.getProductId())
                .map(s -> s.getQuantity()
                        + item.getQuantity())
                .orElse(item.getQuantity());
    }

    @Override
    public String getDescription() {
        return "Void Sale #" + saleNumber;
    }
}