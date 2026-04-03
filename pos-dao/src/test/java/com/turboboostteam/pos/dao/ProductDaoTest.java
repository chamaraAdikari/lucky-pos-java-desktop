package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.dao.impl.ProductDaoImpl;
import com.turboboostteam.pos.model.product.*;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class ProductDaoTest extends BaseDbTest {

    static ProductDao productDao;

    @BeforeAll
    static void setup() {
        initDb();
        productDao = new ProductDaoImpl(dsl);
    }

    @Test
    @DisplayName("Should load all seeded products")
    void findAll_returnsSeedProducts() {
        List<Product> products = productDao.findAll();
        assertThat(products).isNotEmpty();
        assertThat(products.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should find product by barcode")
    void findByBarcode_returnsCorrectProduct() {
        Optional<Product> product =
                productDao.findByBarcode("4901234567890");
        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("Coffee");
        assertThat(product.get().getProductType()).isEqualTo("SIMPLE");
    }

    @Test
    @DisplayName("Should return empty for unknown barcode")
    void findByBarcode_unknownReturnsEmpty() {
        Optional<Product> product =
                productDao.findByBarcode("0000000000000");
        assertThat(product).isEmpty();
    }

    @Test
    @DisplayName("Should save and retrieve new product")
    void save_newProduct_canBeRetrieved() {
        // Arrange
        Category category = new Category(1L, "Food & Beverage",
                "Test");
        Product product = new SimpleProduct(
                null,
                "Test Juice",
                "1234567890123",
                Money.of(new BigDecimal("3.50"), "USD"),
                category,
                true
        );

        // Act
        productDao.save(product);

        // Assert
        Optional<Product> found =
                productDao.findByBarcode("1234567890123");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Juice");
        assertThat(found.get().getPrice())
                .isEqualTo(Money.of(new BigDecimal("3.50"), "USD"));
    }

    @Test
    @DisplayName("Should deactivate product (soft delete)")
    void deactivate_productNotInFindAll() {
        // Get Coffee (id=1)
        Optional<Product> coffee =
                productDao.findByBarcode("4901234567890");
        assertThat(coffee).isPresent();
        Long id = coffee.get().getId();

        // Deactivate
        productDao.deactivate(id);

        // Should not appear in findAll (active only)
        List<Product> products = productDao.findAll();
        assertThat(products)
                .noneMatch(p -> p.getId().equals(id));
    }

    @Test
    @DisplayName("BundleProduct type should be BUNDLE")
    void bundleProduct_hasCorrectType() {
        Optional<Product> bundle =
                productDao.findByBarcode("4901234567894");
        assertThat(bundle).isPresent();
        assertThat(bundle.get().getProductType()).isEqualTo("BUNDLE");
        assertThat(bundle.get()).isInstanceOf(BundleProduct.class);
    }

    @Test
    @DisplayName("Should find products by category")
    void findByCategory_returnsCategoryProducts() {
        // Category 1 = Food & Beverage
        List<Product> foodProducts = productDao.findByCategory(1L);
        assertThat(foodProducts).isNotEmpty();
        assertThat(foodProducts)
                .allMatch(p -> p.getCategory() != null
                        && p.getCategory().getId().equals(1L));
    }
}