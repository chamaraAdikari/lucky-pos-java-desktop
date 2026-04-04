package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.CustomerDao;
import com.turboboostteam.pos.model.customer.Customer;
import com.turboboostteam.pos.model.customer.LoyaltyTier;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.turboboostteam.pos.jooq.Tables.CUSTOMERS;

public class CustomerDaoImpl implements CustomerDao {

    private final DSLContext dsl;

    public CustomerDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Customer> findAll() {
        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ACTIVE.isTrue())
                .orderBy(CUSTOMERS.FIRST_NAME.asc())
                .fetch()
                .map(this::toCustomer);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        Record r = dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(r).map(this::toCustomer);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        Record r = dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.PHONE.eq(phone))
                .fetchOne();
        return Optional.ofNullable(r).map(this::toCustomer);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        Record r = dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.EMAIL.eq(email))
                .fetchOne();
        return Optional.ofNullable(r).map(this::toCustomer);
    }

    @Override
    public List<Customer> search(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ACTIVE.isTrue()
                        .and(CUSTOMERS.FIRST_NAME
                                .lower().like(pattern)
                                .or(CUSTOMERS.LAST_NAME
                                        .lower().like(pattern))
                                .or(CUSTOMERS.PHONE
                                        .like(pattern))
                                .or(CUSTOMERS.EMAIL
                                        .lower().like(pattern))))
                .fetch()
                .map(this::toCustomer);
    }

    @Override
    public Long save(Customer customer) {
        return dsl.insertInto(CUSTOMERS)
                .set(CUSTOMERS.FIRST_NAME,
                        customer.getFirstName())
                .set(CUSTOMERS.LAST_NAME,
                        customer.getLastName())
                .set(CUSTOMERS.EMAIL, customer.getEmail())
                .set(CUSTOMERS.PHONE, customer.getPhone())
                .set(CUSTOMERS.LOYALTY_POINTS,
                        customer.getLoyaltyPoints())
                .set(CUSTOMERS.LOYALTY_TIER,
                        customer.getLoyaltyTier().name())
                .set(CUSTOMERS.TOTAL_SPENT,
                        customer.getTotalSpent())
                .set(CUSTOMERS.ACTIVE, customer.isActive())
                .returningResult(CUSTOMERS.ID)
                .fetchOne()
                .getValue(CUSTOMERS.ID);
    }

    @Override
    public void update(Customer customer) {
        dsl.update(CUSTOMERS)
                .set(CUSTOMERS.FIRST_NAME,
                        customer.getFirstName())
                .set(CUSTOMERS.LAST_NAME,
                        customer.getLastName())
                .set(CUSTOMERS.EMAIL, customer.getEmail())
                .set(CUSTOMERS.PHONE, customer.getPhone())
                .where(CUSTOMERS.ID.eq(customer.getId()))
                .execute();
    }

    @Override
    public void updateLoyalty(Long id, int points,
                              String tier,
                              BigDecimal totalSpent) {
        dsl.update(CUSTOMERS)
                .set(CUSTOMERS.LOYALTY_POINTS, points)
                .set(CUSTOMERS.LOYALTY_TIER, tier)
                .set(CUSTOMERS.TOTAL_SPENT, totalSpent)
                .where(CUSTOMERS.ID.eq(id))
                .execute();
    }

    private Customer toCustomer(Record r) {
        return new Customer.Builder()
                .id(r.get(CUSTOMERS.ID))
                .firstName(r.get(CUSTOMERS.FIRST_NAME))
                .lastName(r.get(CUSTOMERS.LAST_NAME))
                .email(r.get(CUSTOMERS.EMAIL))
                .phone(r.get(CUSTOMERS.PHONE))
                .loyaltyPoints(r.get(CUSTOMERS.LOYALTY_POINTS))
                .loyaltyTier(LoyaltyTier.valueOf(
                        r.get(CUSTOMERS.LOYALTY_TIER)))
                .totalSpent(r.get(CUSTOMERS.TOTAL_SPENT))
                .active(r.get(CUSTOMERS.ACTIVE))
                .createdAt(r.get(CUSTOMERS.CREATED_AT))
                .build();
    }
}