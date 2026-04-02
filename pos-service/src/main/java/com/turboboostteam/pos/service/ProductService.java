package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.ProductDao;
import com.turboboostteam.pos.model.product.Product;

import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    public Optional<Product> findByBarcode(String barcode) {
        return productDao.findByBarcode(barcode);
    }

    public Optional<Product> findById(Long id) {
        return productDao.findById(id);
    }

    public void addProduct(Product product) {
        productDao.save(product);
        AuditLogger.log("PRODUCT_CREATED",
                SessionManager.getInstance().getCurrentUser().getUsername(),
                "product=" + product.getName());
    }

    public void updateProduct(Product product) {
        productDao.update(product);
        AuditLogger.log("PRODUCT_UPDATED",
                SessionManager.getInstance().getCurrentUser().getUsername(),
                "product=" + product.getName());
    }

    public void deactivateProduct(Long id) {
        productDao.deactivate(id);
        AuditLogger.log("PRODUCT_DELETED",
                SessionManager.getInstance().getCurrentUser().getUsername(),
                "productId=" + id);
    }
}