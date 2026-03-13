package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DbWarehouseTest {

    @Test
    public void testToWarehouseMapsFields() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "DB-001";
        db.location = "AMSTERDAM-001";
        db.capacity = 42;
        db.stock = 7;
        LocalDateTime now = LocalDateTime.now();
        db.createdAt = now;
        db.archivedAt = now.minusDays(1);

        Warehouse w = db.toWarehouse();
        assertNotNull(w);
        assertEquals("DB-001", w.businessUnitCode);
        assertEquals("AMSTERDAM-001", w.location);
        assertEquals(42, w.capacity);
        assertEquals(7, w.stock);
        assertEquals(now, w.createdAt);
        assertEquals(now.minusDays(1), w.archivedAt);
    }
}

