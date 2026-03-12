package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryIT {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setup() {
        warehouseRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testPersistAndFind() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "REPO-TEST-1";
        w.location = "AMSTERDAM-001";
        w.capacity = 100;
        w.stock = 10;
        w.createdAt = LocalDateTime.now();

        warehouseRepository.persist(w);
        warehouseRepository.flush();

        Warehouse found = warehouseRepository.findByBusinessUnitCode("REPO-TEST-1");
        assertNotNull(found);
        assertEquals("AMSTERDAM-001", found.location);
    }

    @Test
    public void testFindByNonExistentCode() {
        Warehouse found = warehouseRepository.findByBusinessUnitCode("NON-EXISTENT");
        assertNull(found);
    }
    
    @Test
    public void testGetAll() {
        // Should be empty initially due to setup()
        List<Warehouse> all = warehouseRepository.listAll();
        assertTrue(all.isEmpty());
    }
}
