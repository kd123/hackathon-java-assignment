package com.fulfilment.application.monolith.stores;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StoreCreatePersistenceExceptionTest {

    @Test
    public void testCreate_whenPersistThrows_returns409() {
        // Create resource and inject no-op dependencies
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };

        // create a Store that throws PersistenceException on persist()
        Store bad = new Store() {
            @Override
            public void persist() {
                throw new PersistenceException("duplicate");
            }
        };
        bad.name = "DUP-STORE";
        bad.quantityProductsInStock = 10;

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.create(bad));
        assertEquals(409, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("already exists"));
    }
}

