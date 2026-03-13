package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryIntegrationTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setup() {
        warehouseRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateAndFind() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "INT-001";
        w.location = "AMSTERDAM-002";
        w.capacity = 100;
        w.stock = 40;
        w.createdAt = LocalDateTime.now();

        warehouseRepository.create(w);

        Warehouse found = warehouseRepository.findByBusinessUnitCode("INT-001");
        assertNotNull(found);
        assertEquals("INT-001", found.businessUnitCode);
    }

    @Test
    @Transactional
    public void testGetAllAndUpdate() {
        Warehouse a = new Warehouse();
        a.businessUnitCode = "A-001";
        a.location = "LOC-A";
        a.capacity = 10;
        a.stock = 1;
        a.createdAt = LocalDateTime.now();
        warehouseRepository.create(a);

        Warehouse b = new Warehouse();
        b.businessUnitCode = "B-001";
        b.location = "LOC-B";
        b.capacity = 20;
        b.stock = 2;
        b.createdAt = LocalDateTime.now();
        warehouseRepository.create(b);

        List<Warehouse> all = warehouseRepository.getAll();
        assertEquals(2, all.size());

        // update A
        a.location = "LOC-A-UPDATED";
        a.capacity = 11;
        warehouseRepository.update(a);

        Warehouse updated = warehouseRepository.findByBusinessUnitCode("A-001");
        assertEquals("LOC-A-UPDATED", updated.location);
        assertEquals(11, updated.capacity);
    }

    @Test
    @Transactional
    public void testRemove() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "TO-REMOVE";
        w.location = "X";
        w.capacity = 5;
        w.stock = 0;
        w.createdAt = LocalDateTime.now();
        warehouseRepository.create(w);

        Warehouse found = warehouseRepository.findByBusinessUnitCode("TO-REMOVE");
        assertNotNull(found);

        warehouseRepository.remove(found);

        Warehouse after = warehouseRepository.findByBusinessUnitCode("TO-REMOVE");
        assertNull(after);
    }
}

