package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class WarehouseResourceImplTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setup() {
        warehouseRepository.deleteAll();
        createTestWarehouse("WH-001", "AMSTERDAM-001", 50, 10);
        createTestWarehouse("WH-002", "ZWOLLE-001", 30, 20);
    }

    private void createTestWarehouse(String code, String location, int capacity, int stock) {
        var wh = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        wh.businessUnitCode = code;
        wh.location = location;
        wh.capacity = capacity;
        wh.stock = stock;
        wh.createdAt = java.time.LocalDateTime.now();
        warehouseRepository.persist(wh);
    }

    @Test
    void testListAllWarehouses() {
        given()
                .when().get("/warehouse")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("businessUnitCode", hasItems("WH-001", "WH-002"));
    }

    @Test
    void testGetAWarehouseUnitByID() {
        given()
                .when().get("/warehouse/WH-001")
                .then()
                .statusCode(200)
                .body("businessUnitCode", equalTo("WH-001"));
    }

    @Test
    void testGetNonExistentWarehouse() {
        given()
                .when().get("/warehouse/NOT-EXIST")
                .then()
                .statusCode(404);
    }

    @Test
    void testCreateANewWarehouseUnit() {
        String newWarehouseJson = "{\"businessUnitCode\": \"WH-003\", \"location\": \"TILBURG-001\", \"capacity\": 40, \"stock\": 5}";
        given()
                .contentType(ContentType.JSON)
                .body(newWarehouseJson)
                .when().post("/warehouse")
                .then()
                .statusCode(200)
                .body("businessUnitCode", equalTo("WH-003"));
    }

    @Test
    void testCreateWarehouseWithInvalidData() {
        String invalidWarehouseJson = "{\"businessUnitCode\": \"WH-004\", \"location\": \"INVALID-LOC\", \"capacity\": 10, \"stock\": 5}";
        given()
                .contentType(ContentType.JSON)
                .body(invalidWarehouseJson)
                .when().post("/warehouse")
                .then()
                .statusCode(400);
    }

    @Test
    @Transactional
    void testArchiveAWarehouseUnitByID() {
        given()
                .when().post("/warehouse/WH-001/archive")
                .then()
                .statusCode(204);

        Warehouse archived = warehouseRepository.findByBusinessUnitCode("WH-001");
        assertNotNull(archived.archivedAt);
    }

    @Test
    void testArchiveNonExistentWarehouse() {
        given()
                .when().post("/warehouse/NOT-EXIST/archive")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    void testReplaceTheCurrentActiveWarehouse() {
        String replacementJson = "{\"location\": \"EINDHOVEN-001\", \"capacity\": 60, \"stock\": 30}";
        given()
                .contentType(ContentType.JSON)
                .body(replacementJson)
                .when().put("/warehouse/WH-002")
                .then()
                .statusCode(200)
                .body("location", equalTo("EINDHOVEN-001"))
                .body("capacity", equalTo(60));
    }

    @Test
    void testSearchWarehouses() {
        given()
                .queryParam("location", "AMSTERDAM-001")
                .when().get("/warehouse/search")
                .then()
                .statusCode(200)
                .body("items", hasSize(1))
                .body("items[0].businessUnitCode", equalTo("WH-001"));
    }

    @Test
    void testSearchWithInvalidSortField() {
        given()
                .queryParam("sortBy", "invalidField")
                .when().get("/warehouse/search")
                .then()
                .statusCode(200);
    }
}