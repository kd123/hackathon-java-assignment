package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductResourceUnitTest {

    private ProductResource resource;
    private ProductRepository productRepository;

    @BeforeEach
    public void setup() throws Exception {
        resource = new ProductResource();
        productRepository = mock(ProductRepository.class);
        // inject mock into resource
        Field f = ProductResource.class.getDeclaredField("productRepository");
        f.setAccessible(true);
        f.set(resource, productRepository);
    }

    @Test
    public void testCreate_withId_throws422() {
        Product p = new Product();
        p.id = 123L;
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.create(p));
        assertEquals(422, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Id was invalidly set"));
    }

    @Test
    public void testGetSingle_notFound_throws404() {
        when(productRepository.findById(999L)).thenReturn(null);
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.getSingle(999L));
        assertEquals(404, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    public void testUpdate_withoutName_throws422() {
        Product p = new Product();
        p.name = null;
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(1L, p));
        assertEquals(422, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Product Name was not set"));
    }

    @Test
    public void testUpdate_notFound_throws404() {
        Product p = new Product();
        p.name = "X";
        when(productRepository.findById(5L)).thenReturn(null);
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(5L, p));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    public void testDelete_notFound_throws404() {
        when(productRepository.findById(42L)).thenReturn(null);
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.delete(42L));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    public void testErrorMapper_generalException_returns500Json() throws Exception {
        ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
        ObjectMapper om = new ObjectMapper();
        // inject objectMapper
        Field f = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
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
        ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
        ObjectMapper om = new ObjectMapper();
        Field f = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
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
}
