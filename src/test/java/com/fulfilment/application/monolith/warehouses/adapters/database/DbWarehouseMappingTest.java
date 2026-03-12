package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

public class DbWarehouseMappingTest {

  @Test
  void mappingToDomain() {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = "M1";
    db.location = "L1";
    db.capacity = 10;
    db.stock = 3;

    Warehouse w = db.toWarehouse();

    assertEquals("M1", w.businessUnitCode);
    assertEquals("L1", w.location);
    assertEquals(10, w.capacity);
    assertEquals(3, w.stock);
  }

  @Test
  void mappingHandlesZeroStock() {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = "M2";
    db.location = "L2";
    db.capacity = 5;
    db.stock = 0;

    Warehouse w = db.toWarehouse();

    assertEquals(0, w.stock);
  }

  @Test
  void mappingHandlesLargeCapacity() {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = "M3";
    db.location = "L3";
    db.capacity = 1000;
    db.stock = 500;

    Warehouse w = db.toWarehouse();

    assertEquals(1000, w.capacity);
    assertEquals(500, w.stock);
  }
}