package com.turboboostteam.pos.config;

import com.turboboostteam.pos.dao.CategoryDao;
import com.turboboostteam.pos.dao.ProductDao;
import com.turboboostteam.pos.dao.UserDao;
import com.turboboostteam.pos.dao.impl.CategoryDaoImpl;
import com.turboboostteam.pos.dao.impl.ProductDaoImpl;
import com.turboboostteam.pos.dao.impl.UserDaoImpl;
import com.turboboostteam.pos.service.ProductService;
import com.turboboostteam.pos.service.UserService;
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


}