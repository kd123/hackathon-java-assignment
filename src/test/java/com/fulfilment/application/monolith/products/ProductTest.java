package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    @Test
    void testDefaultConstructor() {

        Product product = new Product();

        product.name = "PRODUCT-1";
        product.description = "Test product";
        product.price = new BigDecimal("19.99");
        product.stock = 50;

        assertEquals("PRODUCT-1", product.name);
        assertEquals("Test product", product.description);
        assertEquals(new BigDecimal("19.99"), product.price);
        assertEquals(50, product.stock);
    }

    @Test
    void testParameterizedConstructor() {

        Product product = new Product("PRODUCT-2");

        assertEquals("PRODUCT-2", product.name);
    }
}