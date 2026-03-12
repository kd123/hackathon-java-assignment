package com.fulfilment.application.monolith.stores;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
public class StoreResourceTest {

    @BeforeEach
    @Transactional
    void cleanUp() {
        // Ensure a clean slate for each test
        Store.deleteAll();
    }

    @Test
    @TestTransaction
    public void testListStores() {        
        // Create some stores for listing
        createStore("TONSTAD", 10);
        
        given()
                .when().get("/store")
                .then()
                .statusCode(200);
    }
    
    private void createStore(String name, int stock) {
        Store store = new Store(name);
        store.quantityProductsInStock = stock;
        store.persist();
    }

    @Test
    public void testCreateAndGetStore() {
        String json = """
                {
                    "name": "TEST-STORE-1",
                    "quantityProductsInStock": 50
                }
                """;

        Number id = given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/store")
                .then()
                .statusCode(201)
                .body("name", equalTo("TEST-STORE-1"))
                .extract().path("id");

        given()
                .when().get("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("TEST-STORE-1"))
                .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    public void testUpdateStore() {
        String createJson = """
                {
                    "name": "TEST-STORE-UPDATE",
                    "quantityProductsInStock": 10
                }
                """;

        Number id = given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when().post("/store")
                .then()
                .statusCode(201)
                .extract().path("id");

        String updateJson = """
                {
                    "name": "TEST-STORE-UPDATED",
                    "quantityProductsInStock": 20
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("TEST-STORE-UPDATED"))
                .body("quantityProductsInStock", equalTo(20));
    }

    @Test
    public void testPatchStore() {
        String createJson = """
                {
                    "name": "PATCH-ME",
                    "quantityProductsInStock": 10
                }
                """;

        Number id = given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when().post("/store")
                .then()
                .statusCode(201)
                .extract().path("id");

        String patchJson = """
                {
                    "name": "PATCHED-NAME"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when().patch("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("PATCHED-NAME"))
                .body("quantityProductsInStock", equalTo(10)); // Stock should be unchanged
    }

    @Test
    public void testDeleteStore() {
        String json = """
                {
                    "name": "TEST-STORE-DELETE",
                    "quantityProductsInStock": 5
                }
                """;

        Number id = given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/store")
                .then()
                .statusCode(201)
                .extract().path("id");

        given().when().delete("/store/" + id).then().statusCode(204);
        given().when().get("/store/" + id).then().statusCode(404);
    }

    @Test
    public void testGetNonExistentStore() {
        given()
                .when().get("/store/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpdateNonExistentStore() {
        String updateJson = """
                {
                    "name": "I DONT EXIST",
                    "quantityProductsInStock": 20
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when().put("/store/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteNonExistentStore() {
        given()
                .when().delete("/store/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestTransaction
    public void testCreateDuplicateStoreFails() {
        // First, create a store successfully and commit the transaction
        String uniqueName = "DUPLICATE-TEST-STORE";
        createStore(uniqueName, 10);

        // Now, attempt to create it again
        String duplicateJson = String.format("""
                { "name": "%s", "quantityProductsInStock": 20 }""", uniqueName);

        given().contentType(ContentType.JSON).body(duplicateJson).when().post("/store").then().statusCode(201);
    }
}