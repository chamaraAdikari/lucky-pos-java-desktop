package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.discount.Discount;
import java.util.List;
import java.util.Optional;

public interface DiscountDao {
    List<Discount> findAll();
    Optional<Discount> findByCode(String code);
    void save(Discount discount);
    void deactivate(Long id);
}