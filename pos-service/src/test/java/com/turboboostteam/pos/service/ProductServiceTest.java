package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.ProductDao;
import com.turboboostteam.pos.model.product.*;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    ProductDao productDao;

    ProductService productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productDao);

        SessionManager.getInstance().login(
                new com.turboboostteam.pos.model.user.User(
                        1L, "admin", "hash",
                        com.turboboostteam.pos.model.user.Role.ADMIN,
                        true
                )
        );
    }

    @Test
    @DisplayName("getAllProducts delegates to DAO")
    void getAllProducts_delegatesToDao() {
        Product p = new SimpleProduct(1L, "Coffee", "123",
                Money.of(BigDecimal.TEN, "USD"),
                null, true);
        when(productDao.findAll()).thenReturn(List.of(p));

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Coffee");
        verify(productDao).findAll();
    }

    @Test
    @DisplayName("findByBarcode returns correct product")
    void findByBarcode_returnsProduct() {
        Product p = new SimpleProduct(1L, "Coffee", "4901234567890",
                Money.of(new BigDecimal("2.50"), "USD"),
                null, true);
        when(productDao.findByBarcode("4901234567890"))
                .thenReturn(Optional.of(p));

        Optional<Product> result =
                productService.findByBarcode("4901234567890");

        assertThat(result).isPresent();
        assertThat(result.get().getPrice())
                .isEqualTo(Money.of(new BigDecimal("2.50"), "USD"));
    }

    @Test
    @DisplayName("addProduct calls save on DAO")
    void addProduct_callsSave() {
        Product p = new SimpleProduct(null, "New Item", "999",
                Money.of(BigDecimal.ONE, "USD"), null, true);

        productService.addProduct(p);

        verify(productDao).save(p);
    }

    @Test
    @DisplayName("deactivateProduct calls deactivate on DAO")
    void deactivateProduct_callsDeactivate() {
        productService.deactivateProduct(1L);
        verify(productDao).deactivate(1L);
    }

    @Test
    @DisplayName("Polymorphism — SimpleProduct getFinalPrice returns base price")
    void simpleProduct_getFinalPrice_equalsBasePrice() {
        Product p = new SimpleProduct(1L, "Coffee", "123",
                Money.of(new BigDecimal("2.50"), "USD"),
                null, true);

        assertThat(p.getFinalPrice())
                .isEqualTo(p.getPrice());
    }

    @Test
    @DisplayName("Polymorphism — BundleProduct type is BUNDLE")
    void bundleProduct_getProductType_returnsBundle() {
        Product p = new BundleProduct(1L, "Lunch", "456",
                Money.of(new BigDecimal("7.99"), "USD"),
                null, true, List.of(), 0.0);

        assertThat(p.getProductType()).isEqualTo("BUNDLE");
    }
}