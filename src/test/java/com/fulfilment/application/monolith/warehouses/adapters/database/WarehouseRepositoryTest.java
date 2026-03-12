package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setup() {
        warehouseRepository.deleteAll();
    }

    @Test
    @Transactional
    void testRemoveWarehouse() {
        // 1. Create a warehouse to be removed
        Warehouse warehouseToRemove = new Warehouse();
        warehouseToRemove.businessUnitCode = "REMOVE-ME";
        warehouseToRemove.location = "AMSTERDAM-001";
        warehouseToRemove.capacity = 50;
        warehouseToRemove.stock = 10;
        warehouseRepository.create(warehouseToRemove);

        // Verify it was created
        Warehouse found = warehouseRepository.findByBusinessUnitCode("REMOVE-ME");
        assertNotNull(found, "Warehouse should exist before removal");

        // 2. Remove the warehouse
        warehouseRepository.remove(found);

        // 3. Verify it has been removed
        Warehouse afterRemove = warehouseRepository.findByBusinessUnitCode("REMOVE-ME");
        assertNull(afterRemove, "Warehouse should not exist after removal");
    }

    @Test
    void testRemoveNonExistentWarehouse() {
        // This test ensures that calling remove with a non-existent warehouse doesn't throw an exception
        Warehouse nonExistent = new Warehouse();
        nonExistent.businessUnitCode = "I-DONT-EXIST";
        warehouseRepository.remove(nonExistent);
    }
}
