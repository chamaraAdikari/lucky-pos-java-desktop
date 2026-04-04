package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.SaleDao;
import com.turboboostteam.pos.model.enums.PaymentType;
import com.turboboostteam.pos.model.sale.*;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

public class SaleService {

    private final SaleDao saleDao;
    private final InventoryService inventoryService;
    private final TaxService taxService;
    private final ProductService productService;
    private final LoyaltyService loyaltyService;

    private SaleTransaction currentSale;

    // Current active sale — one at a time per cashier
    public SaleService(SaleDao saleDao,
                       InventoryService inventoryService,
                       TaxService taxService,
                       ProductService productService,
                       LoyaltyService loyaltyService) {
        this.saleDao          = saleDao;
        this.inventoryService = inventoryService;
        this.taxService       = taxService;
        this.productService   = productService;
        this.loyaltyService   = loyaltyService;
    }

    public void addItem(SaleItem item) {
        validateActiveSale();

        // Apply tax before adding to cart
        String category = productService
                .findById(item.getProductId())
                .map(p -> p.getCategory() != null
                        ? p.getCategory().getName()
                        : "General")
                .orElse("General");

        MonetaryAmount tax = taxService.calculate(item, category);
        item.setTaxAmount(tax);

        currentSale.addItem(item);
    }

    // ---- Sale Lifecycle ----

    public SaleTransaction openSale(Long cashierId) {
        currentSale = new SaleTransaction(cashierId);
        AuditLogger.log("SALE_OPENED",
                SessionManager.getInstance()
                        .getCurrentUser().getUsername(),
                "saleNumber=" + currentSale.getSaleNumber());
        return currentSale;
    }


    public void removeItem(Long productId) {
        validateActiveSale();
        currentSale.removeItem(productId);
    }

    public void updateQuantity(Long productId, int quantity) {
        validateActiveSale();
        currentSale.updateQuantity(productId, quantity);
    }

    public SaleTransaction commitSale(
            PaymentType paymentType,
            MonetaryAmount amountTendered) {

        validateActiveSale();

        if (currentSale.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot commit empty sale");
        }

        // 1. Commit the sale
        currentSale.commit();

        // 2. Save to DB
        Long saleId = saleDao.saveSale(currentSale);
        currentSale.setId(saleId);
        saleDao.saveSaleItems(saleId, currentSale);

        // 3. Save payment
        Payment payment = new Payment(
                saleId, paymentType,
                currentSale.getTotalAmount(),
                "REF-" + System.currentTimeMillis()
        );
        saleDao.savePayment(payment);

        // 4. Decrement stock for each item
        String username = SessionManager.getInstance()
                .getCurrentUser().getUsername();
        currentSale.getItems().forEach(item ->
                inventoryService.decrementStock(
                        item.getProductId(),
                        item.getQuantity(),
                        username)
        );

        // 5. Award loyalty points if customer attached ← ADD
        if (currentSale.getCustomerId() != null) {
            BigDecimal totalAmount = currentSale
                    .getTotalAmount()
                    .getNumber()
                    .numberValue(BigDecimal.class);
            loyaltyService.awardPoints(
                    currentSale.getCustomerId(),
                    totalAmount);
        }

        AuditLogger.saleCompleted(username,
                currentSale.getSaleNumber());

        SaleTransaction completed = currentSale;
        currentSale = null; // reset
        return completed;
    }

    public void cancelSale() {
        validateActiveSale();
        currentSale.cancel();
        AuditLogger.log("SALE_CANCELLED",
                SessionManager.getInstance()
                        .getCurrentUser().getUsername(),
                "saleNumber=" + currentSale.getSaleNumber());
        currentSale = null;
    }

    // ---- Getters ----

    public SaleTransaction getCurrentSale() {
        return currentSale;
    }

    public boolean hasSaleOpen() {
        return currentSale != null;
    }

    private void validateActiveSale() {
        if (currentSale == null) {
            throw new IllegalStateException(
                    "No active sale. Call openSale() first.");
        }
    }
}