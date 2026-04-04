package com.turboboostteam.pos.service.command;

import com.turboboostteam.pos.dao.SaleDao;
import com.turboboostteam.pos.model.sale.SaleItem;
import com.turboboostteam.pos.model.enums.SaleStatus;
import com.turboboostteam.pos.service.AuditLogger;
import com.turboboostteam.pos.service.InventoryService;
import com.turboboostteam.pos.service.SessionManager;

import java.util.List;
import java.util.Map;

// Command Pattern — refund a completed sale
public class RefundCommand implements SaleCommand {

    private final SaleDao saleDao;
    private final InventoryService inventoryService;
    private final Long saleId;
    private final String saleNumber;
    private final Map<Long, Integer> refundItems;
    // productId → quantity to refund

    public RefundCommand(SaleDao saleDao,
                         InventoryService inventoryService,
                         Long saleId,
                         String saleNumber,
                         Map<Long, Integer> refundItems) {
        this.saleDao          = saleDao;
        this.inventoryService = inventoryService;
        this.saleId           = saleId;
        this.saleNumber       = saleNumber;
        this.refundItems      = refundItems;
    }

    @Override
    public void execute() {
        String username = SessionManager.getInstance()
                .getCurrentUser().getUsername();

        // 1. Mark as refunded
        saleDao.refundSale(saleId,
                SaleStatus.REFUNDED.name());

        // 2. Restore stock for refunded items
        refundItems.forEach((productId, qty) ->
                inventoryService.getStockForProduct(
                        productId).ifPresent(stock -> {
                    int newQty = stock.getQuantity() + qty;
                    inventoryService.adjustStock(
                            productId, newQty,
                            "Stock restored — refund of "
                                    + saleNumber,
                            username);
                }));

        // 3. Audit log
        AuditLogger.log("SALE_REFUNDED", username,
                "saleId=" + saleId
                        + " items=" + refundItems);
    }

    @Override
    public void undo() {
        // Re-mark as committed
        saleDao.updateSaleStatus(saleId,
                SaleStatus.COMMITTED.name(), null);

        String username = SessionManager.getInstance()
                .getCurrentUser().getUsername();

        // Re-decrement refunded stock
        refundItems.forEach((productId, qty) ->
                inventoryService.decrementStock(
                        productId, qty, username));

        AuditLogger.log("REFUND_UNDONE", username,
                "saleId=" + saleId);
    }

    @Override
    public String getDescription() {
        return "Refund Sale #" + saleNumber;
    }
}