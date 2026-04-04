package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.customer.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    List<Customer> findAll();
    Optional<Customer> findById(Long id);
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
    List<Customer> search(String query);
    Long save(Customer customer);
    void update(Customer customer);
    void updateLoyalty(Long id, int points,
                       String tier,
                       java.math.BigDecimal totalSpent);
}