package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.SaleDao;
import com.turboboostteam.pos.model.sale.SaleTransaction;
import com.turboboostteam.pos.service.command.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class VoidRefundService {

    private final SaleDao saleDao;
    private final InventoryService inventoryService;

    // Command history for undo
    private final Stack<SaleCommand> commandHistory
            = new Stack<>();

    public VoidRefundService(SaleDao saleDao,
                             InventoryService inventoryService) {
        this.saleDao          = saleDao;
        this.inventoryService = inventoryService;
    }

    public List<SaleTransaction> getRecentSales(int limit) {
        return saleDao.findRecentSales(limit);
    }

    public Optional<SaleTransaction> findSaleById(Long id) {
        return saleDao.findById(id);
    }

    public List<com.turboboostteam.pos.model.sale.SaleItem>
    getSaleItems(Long saleId) {
        return saleDao.findItemsBySaleId(saleId);
    }

    // Void entire sale
    public void voidSale(Long saleId, String saleNumber) {
        VoidSaleCommand cmd = new VoidSaleCommand(
                saleDao, inventoryService,
                saleId, saleNumber);
        cmd.execute();
        commandHistory.push(cmd);
    }

    // Refund selected items
    public void refundSale(Long saleId,
                           String saleNumber,
                           Map<Long, Integer> items) {
        RefundCommand cmd = new RefundCommand(
                saleDao, inventoryService,
                saleId, saleNumber, items);
        cmd.execute();
        commandHistory.push(cmd);
    }

    // Undo last command
    public boolean undoLast() {
        if (commandHistory.isEmpty()) return false;
        SaleCommand cmd = commandHistory.pop();
        cmd.undo();
        AuditLogger.log("COMMAND_UNDONE",
                SessionManager.getInstance()
                        .getCurrentUser().getUsername(),
                cmd.getDescription());
        return true;
    }

    public boolean hasHistory() {
        return !commandHistory.isEmpty();
    }
}