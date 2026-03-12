package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StoreTest {

    @Test
    void testDefaultConstructor() {
        Store store = new Store();

        store.name = "STORE-1";
        store.quantityProductsInStock = 10;

        assertEquals("STORE-1", store.name);
        assertEquals(10, store.quantityProductsInStock);
    }

    @Test
    void testParameterizedConstructor() {
        Store store = new Store("STORE-2");

        assertEquals("STORE-2", store.name);
    }
}