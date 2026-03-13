package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreResourceUnitTest {

    private LegacyStoreManagerGateway legacyGateway;
    @SuppressWarnings("rawtypes")
    private Event storeCreatedEvent;
    @SuppressWarnings("rawtypes")
    private Event storeUpdatedEvent;

    @BeforeEach
    public void setup() throws Exception {
        legacyGateway = mock(LegacyStoreManagerGateway.class);
        storeCreatedEvent = mock(Event.class);
        storeUpdatedEvent = mock(Event.class);
    }

    @Test
    public void testCreate_withId_throws422() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        Store s = new Store();
        s.id = 10L;
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.create(s));
        assertEquals(422, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Id was invalidly set"));
    }

    @Test
    public void testGetSingle_notFound_throws404() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.getSingle(9999L));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    public void testUpdate_withoutName_throws422() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        Store s = new Store();
        s.name = null;
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(1L, s));
        assertEquals(422, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Store Name was not set"));
    }

    @Test
    public void testUpdate_notFound_throws404() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        Store s = new Store();
        s.name = "X";
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(5L, s));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    public void testPatch_notFound_throws404() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        Store s = new Store();
        s.name = "Name";
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.patch(12345L, s));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    public void testDelete_notFound_throws404() throws Exception {
        StoreResource resource = new StoreResource() {
            @Override
            protected Store findStoreById(Long id) { return null; }
        };
        injectDependencies(resource);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.delete(4242L));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    public void testErrorMapper_generalException_returns500Json() throws Exception {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        ObjectMapper om = new ObjectMapper();
        Field f = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
        f.setAccessible(true);
        f.set(mapper, om);

        try (Response resp = mapper.toResponse(new RuntimeException("boom"))) {
            assertEquals(500, resp.getStatus());
            Object entity = resp.getEntity();
            assertNotNull(entity);
            ObjectNode node = assertInstanceOf(ObjectNode.class, entity);
            assertEquals("java.lang.RuntimeException", node.get("exceptionType").asText());
            assertEquals(500, node.get("code").asInt());
            assertEquals("boom", node.get("error").asText());
        }
    }

    @Test
    public void testErrorMapper_webApplicationException_returnsStatusFromException() throws Exception {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        ObjectMapper om = new ObjectMapper();
        Field f = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
        f.setAccessible(true);
        f.set(mapper, om);

        WebApplicationException wae = new WebApplicationException("bad", Response.status(422).build());
        try (Response resp = mapper.toResponse(wae)) {
            assertEquals(422, resp.getStatus());
            ObjectNode node = assertInstanceOf(ObjectNode.class, resp.getEntity());
            assertEquals("bad", node.get("error").asText());
            assertEquals(422, node.get("code").asInt());
        }
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
