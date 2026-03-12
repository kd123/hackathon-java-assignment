package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Integration test for store event handling and transaction integrity.
 *
 * Verifies that the legacy system is only notified when store
 * operations complete successfully.
 */
@QuarkusTest
public class StoreTransactionIntegrationTest {

  @InjectMock
  LegacyStoreManagerGateway legacyGateway;

  @Test
  public void testLegacySystemNotNotifiedOnFailedStoreCreation() throws InterruptedException {
    Mockito.reset(legacyGateway);

    String uniqueName = "IntegrationTest_" + System.currentTimeMillis();

    // First create should succeed
    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 5}")
        .when().post("/store")
        .then()
        .statusCode(201);

    // Allow time for event processing
    Thread.sleep(1000);

    // Legacy system should be notified for the successful creation
    verify(legacyGateway, times(1)).createStoreOnLegacySystem(any(Store.class));

    // Reset for next assertion
    Mockito.reset(legacyGateway);

    // Second create with same name should fail (unique constraint violation)
    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 10}")
        .when().post("/store")
        .then()
        .statusCode(500);

    // Allow time for any async event processing
    Thread.sleep(1000);

    // Legacy system should NOT be notified for a failed transaction
    verify(legacyGateway, never()).createStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testLegacySystemCalledOnSuccessfulStoreUpdate() throws InterruptedException {

    Mockito.reset(legacyGateway);

    String name = "UpdateTest_" + System.currentTimeMillis();

    // Create store
    int id =
            given()
                    .contentType("application/json")
                    .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 5}")
                    .when()
                    .post("/store")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

    Thread.sleep(500);

    verify(legacyGateway, times(1)).createStoreOnLegacySystem(any(Store.class));

    Mockito.reset(legacyGateway);

    // Update store
    given()
            .contentType("application/json")
            .body("{\"name\": \"" + name + "\", \"quantityProductsInStock\": 50}")
            .when()
            .put("/store/" + id)
            .then()
            .statusCode(200);

    Thread.sleep(500);

    verify(legacyGateway, times(1)).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testLegacySystemNotCalledOnFailedUpdate() throws InterruptedException {

    Mockito.reset(legacyGateway);

    // Update non-existing store
    given()
            .contentType("application/json")
            .body("{\"name\": \"InvalidUpdate\", \"quantityProductsInStock\": 10}")
            .when()
            .put("/store/99999")
            .then()
            .statusCode(404);

    Thread.sleep(500);

    verify(legacyGateway, never()).updateStoreOnLegacySystem(any(Store.class));
  }
}
