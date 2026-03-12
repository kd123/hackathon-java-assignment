package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseRepositoryQuarkusTest {

  @Inject
  WarehouseRepository repository;

  @BeforeEach
  @Transactional
  public void clean() {
    repository.deleteAll();
  }

  @Test
  @Transactional
  public void testCreateFindUpdateRemove() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "QR-001";
    w.location = "TEST-LOC";
    w.capacity = 10;
    w.stock = 2;

    repository.create(w);

    Warehouse fetched = repository.findByBusinessUnitCode("QR-001");
    assertNotNull(fetched);
    assertEquals("TEST-LOC", fetched.location);

    fetched.stock = 5;
    repository.update(fetched);

    Warehouse updated = repository.findByBusinessUnitCode("QR-001");
    assertEquals(5, updated.stock);

    repository.remove(updated);
    Warehouse removed = repository.findByBusinessUnitCode("QR-001");
    assertNull(removed);
  }

  @Test
  @Transactional
  public void testFindNonExistingWarehouse() {
    Warehouse result = repository.findByBusinessUnitCode("DOES-NOT-EXIST");
    assertNull(result);
  }

  @Test
  @Transactional
  public void testCreateMultipleWarehouses() {

    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "QR-101";
    w1.location = "LOC1";
    w1.capacity = 20;
    w1.stock = 5;

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "QR-102";
    w2.location = "LOC2";
    w2.capacity = 30;
    w2.stock = 10;

    repository.create(w1);
    repository.create(w2);

    assertNotNull(repository.findByBusinessUnitCode("QR-101"));
    assertNotNull(repository.findByBusinessUnitCode("QR-102"));
  }

  @Test
  @Transactional
  public void testUpdateCapacity() {

    Warehouse w = new Warehouse();
    w.businessUnitCode = "QR-200";
    w.location = "TEST";
    w.capacity = 10;
    w.stock = 1;

    repository.create(w);

    Warehouse fetched = repository.findByBusinessUnitCode("QR-200");
    fetched.capacity = 99;

    repository.update(fetched);

    Warehouse updated = repository.findByBusinessUnitCode("QR-200");
    assertEquals(99, updated.capacity);
  }
}

