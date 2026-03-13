package com.fulfilment.application.monolith.stores;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreResourceSuccessTest {

    private LegacyStoreManagerGateway legacyGateway;
    @SuppressWarnings("rawtypes")
    private Event storeCreatedEvent;
    @SuppressWarnings("rawtypes")
    private Event storeUpdatedEvent;

    @BeforeEach
    public void setup() {
        legacyGateway = mock(LegacyStoreManagerGateway.class);
        storeCreatedEvent = mock(Event.class);
        storeUpdatedEvent = mock(Event.class);
    }

    @Test
    public void testCreate_success_returns201_and_firesEvent() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        Store s = new Store() {
            @Override
            public void persist() {
                // simulate persistence by setting an id
                this.id = 100L;
            }
        };
        s.name = "NEW-STORE";
        s.quantityProductsInStock = 5;

        Response resp = resource.create(s);
        assertEquals(201, resp.getStatus());
        Object entity = resp.getEntity();
        assertTrue(entity instanceof Store);
        Store returned = (Store) entity;
        assertEquals("NEW-STORE", returned.name);
        assertEquals(100L, returned.id.longValue());

        verify(storeCreatedEvent).fire(any(StoreCreatedEvent.class));
    }

    @Test
    public void testUpdate_success_updatesEntity_and_firesEvent() throws Exception {
        // existing entity
        Store existing = new Store() {
            @Override
            public void persist() { }
        };
        existing.id = 200L;
        existing.name = "OLD-NAME";
        existing.quantityProductsInStock = 1;

        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return existing; }
        };
        injectDependencies(resource);

        Store updated = new Store();
        updated.name = "NEW-NAME";
        updated.quantityProductsInStock = 10;

        Store result = resource.update(200L, updated);
        assertEquals("NEW-NAME", result.name);
        assertEquals(10, result.quantityProductsInStock);

        verify(storeUpdatedEvent).fire(any(StoreUpdatedEvent.class));
    }

    @Test
    public void testPatch_success_patchesFields_and_firesEvent() throws Exception {
        Store existing = new Store() {
            @Override
            public void persist() { }
        };
        existing.id = 300L;
        existing.name = "OLD";
        existing.quantityProductsInStock = 2;

        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return existing; }
        };
        injectDependencies(resource);

        Store update = new Store();
        update.name = "PATCHED";
        // quantityProductsInStock left as 0 to simulate no change

        Store result = resource.patch(300L, update);
        assertEquals("PATCHED", result.name);
        assertEquals(2, result.quantityProductsInStock);

        verify(storeUpdatedEvent).fire(any(StoreUpdatedEvent.class));
    }

    @Test
    public void testDelete_success_deletesEntity() throws Exception {
        final boolean[] deleted = {false};
        Store existing = new Store() {
            @Override
            public void delete() { deleted[0] = true; }
        };
        existing.id = 400L;
        existing.name = "TO-DEL";

        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return existing; }
        };
        injectDependencies(resource);

        Response resp = resource.delete(400L);
        assertEquals(204, resp.getStatus());
        assertTrue(deleted[0]);
    }

    private void injectDependencies(StoreResource resource) throws Exception {
        Field f1 = StoreResource.class.getDeclaredField("legacyStoreManagerGateway");
        f1.setAccessible(true);
        f1.set(resource, legacyGateway);

        Field f2 = StoreResource.class.getDeclaredField("storeCreatedEvent");
        f2.setAccessible(true);
        f2.set(resource, storeCreatedEvent);

        Field f3 = StoreResource.class.getDeclaredField("storeUpdatedEvent");
        f3.setAccessible(true);
        f3.set(resource, storeUpdatedEvent);
    }
}

