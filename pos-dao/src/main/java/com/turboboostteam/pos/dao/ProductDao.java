package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.product.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByCategory(Long categoryId);
    void save(Product product);
    void update(Product product);
    void deactivate(Long id);   // soft delete
}