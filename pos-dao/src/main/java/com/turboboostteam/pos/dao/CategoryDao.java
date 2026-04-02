package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.product.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    List<Category> findAll();
    Optional<Category> findById(Long id);
    void save(Category category);
}