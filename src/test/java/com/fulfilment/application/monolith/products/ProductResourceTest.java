package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ProductResourceTest {

    @Inject
    ProductRepository productRepository;

    @BeforeEach
    @Transactional
    void setup() {
        productRepository.deleteAll();
        createProduct("Test Product A", "Description A", new BigDecimal("10.50"), 100);
        createProduct("Test Product B", "Description B", new BigDecimal("20.00"), 200);
    }

    private void createProduct(String name, String description, BigDecimal price, int stock) {
        Product p = new Product();
        p.name = name;
        p.description = description;
        p.price = price;
        p.stock = stock;
        productRepository.persist(p);
    }

    @Test
    void testGetAllProducts() {
        given()
                .when().get("/product")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("name", hasItems("Test Product A", "Test Product B"));
    }

    @Test
    void testGetSingleProduct() {
        Product product = productRepository.find("name", "Test Product A").firstResult();
        given()
                .when().get("/product/" + product.id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Product A"));
    }

    @Test
    void testGetNonExistentProduct() {
        given()
                .when().get("/product/9999")
                .then()
                .statusCode(404);
    }

    @Test
    void testCreateProduct() {
        String newProductJson = "{\"name\": \"New Product C\", \"stock\": 50}";
        given()
                .contentType(ContentType.JSON)
                .body(newProductJson)
                .when().post("/product")
                .then()
                .statusCode(201)
                .body("name", equalTo("New Product C"))
                .body("stock", equalTo(50));
    }

    @Test
    void testCreateProductWithIdFails() {
        String newProductJson = "{\"id\": 123, \"name\": \"Fail Product\"}";
        given()
                .contentType(ContentType.JSON)
                .body(newProductJson)
                .when().post("/product")
                .then()
                .statusCode(422);
    }

    @Test
    void testUpdateProduct() {
        Product product = productRepository.find("name", "Test Product A").firstResult();
        String updatedProductJson = "{\"name\": \"Updated Product A\", \"stock\": 150}";

        given()
                .contentType(ContentType.JSON)
                .body(updatedProductJson)
                .when().put("/product/" + product.id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Product A"))
                .body("stock", equalTo(150));
    }

    @Test
    void testUpdateProductWithoutNameFails() {
        Product product = productRepository.find("name", "Test Product A").firstResult();
        String updatedProductJson = "{\"stock\": 150}";
        given()
                .contentType(ContentType.JSON)
                .body(updatedProductJson)
                .when().put("/product/" + product.id)
                .then()
                .statusCode(422);
    }

    @Test
    void testUpdateNonExistentProduct() {
        String updatedProductJson = "{\"name\": \"Updated Product A\", \"stock\": 150}";
        given()
                .contentType(ContentType.JSON)
                .body(updatedProductJson)
                .when().put("/product/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteProduct() {
        Product product = productRepository.find("name", "Test Product A").firstResult();
        given()
                .when().delete("/product/" + product.id)
                .then()
                .statusCode(204);

        // Verify it's gone
        given()
                .when().get("/product/" + product.id)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteNonExistentProduct() {
        given()
                .when().delete("/product/9999")
                .then()
                .statusCode(404);
    }
}
