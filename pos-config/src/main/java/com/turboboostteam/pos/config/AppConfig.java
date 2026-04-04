package com.turboboostteam.pos.config;

import com.turboboostteam.pos.dao.*;
import com.turboboostteam.pos.dao.impl.*;
import com.turboboostteam.pos.service.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import javax.sql.DataSource;

@Configuration
@Import(DatabaseConfig.class)
public class AppConfig {

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(dataSource, SQLDialect.H2);
    }

    @Bean
    public UserDao userDao(DSLContext dsl) {
        return new UserDaoImpl(dsl);
    }

    @Bean
    public UserService userService(UserDao userDao) {
        return new UserService(userDao);
    }

    @Bean
    public CategoryDao categoryDao(DSLContext dsl) {
        return new CategoryDaoImpl(dsl);
    }

    @Bean
    public ProductDao productDao(DSLContext dsl) {
        return new ProductDaoImpl(dsl);
    }

    @Bean
    public ProductService productService(ProductDao productDao) {
        return new ProductService(productDao);
    }

    @Bean
    public InventoryDao inventoryDao(DSLContext dsl) {
        return new InventoryDaoImpl(dsl);
    }

    @Bean
    public InventoryService inventoryService(InventoryDao inventoryDao) {
        return new InventoryService(inventoryDao);
    }

    @Bean
    public SaleDao saleDao(DSLContext dsl) {
        return new SaleDaoImpl(dsl);
    }

    @Bean
    public TaxService taxService() {
        return new TaxService();
    }

    @Bean
    public SaleService saleService(
            SaleDao saleDao,
            InventoryService inventoryService,
            TaxService taxService,
            ProductService productService,
            LoyaltyService loyaltyService) {
        return new SaleService(saleDao, inventoryService,
                taxService, productService, loyaltyService);
    }

    @Bean
    public DiscountDao discountDao(DSLContext dsl) {
        return new DiscountDaoImpl(dsl);
    }

    @Bean
    public DiscountService discountService(
            DiscountDao discountDao) {
        return new DiscountService(discountDao);
    }

    @Bean
    public CustomerDao customerDao(DSLContext dsl) {
        return new CustomerDaoImpl(dsl);
    }

    @Bean
    public CustomerService customerService(
            CustomerDao customerDao) {
        return new CustomerService(customerDao);
    }

    @Bean
    public LoyaltyService loyaltyService(
            CustomerDao customerDao) {
        return new LoyaltyService(customerDao);
    }
}