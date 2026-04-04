package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.CustomerDao;
import com.turboboostteam.pos.model.customer.Customer;
import com.turboboostteam.pos.model.customer.LoyaltyTier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    public List<Customer> search(String query) {
        return customerDao.search(query);
    }

    public Optional<Customer> findById(Long id) {
        return customerDao.findById(id);
    }

    public Optional<Customer> findByPhone(String phone) {
        return customerDao.findByPhone(phone);
    }

    public Long addCustomer(Customer customer) {
        Long id = customerDao.save(customer);
        AuditLogger.log("CUSTOMER_CREATED",
                SessionManager.getInstance()
                        .getCurrentUser().getUsername(),
                "name=" + customer.getFullName());
        return id;
    }

    public void updateCustomer(Customer customer) {
        customerDao.update(customer);
        AuditLogger.log("CUSTOMER_UPDATED",
                SessionManager.getInstance()
                        .getCurrentUser().getUsername(),
                "id=" + customer.getId());
    }

    // Called after sale commit
    public void recordPurchase(Long customerId,
                               BigDecimal amount) {
        customerDao.findById(customerId)
                .ifPresent(customer -> {
                    // Calculate points earned
                    int pointsEarned = customer
                            .calculatePointsForPurchase(amount);

                    // Update totals
                    customer.addLoyaltyPoints(pointsEarned);
                    customer.addToTotalSpent(amount);

                    // Check tier upgrade
                    LoyaltyTier newTier = LoyaltyTier
                            .fromPoints(customer.getLoyaltyPoints());

                    // Save to DB
                    customerDao.updateLoyalty(
                            customerId,
                            customer.getLoyaltyPoints(),
                            newTier.name(),
                            customer.getTotalSpent()
                    );

                    AuditLogger.log("LOYALTY_POINTS_EARNED",
                            SessionManager.getInstance()
                                    .getCurrentUser().getUsername(),
                            "customerId=" + customerId
                                    + " points=" + pointsEarned
                                    + " tier=" + newTier);
                });
    }
}