package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sophisticated Test: Testcontainers Integration Test
 * 
 * Uses real PostgreSQL database via Testcontainers instead of mocks.
 * Tests complex database scenarios, constraints, and queries against
 * actual database behavior.
 * 
 * Quarkus provides built-in support for spinning up test databases.
 */
@QuarkusTest
public class WarehouseTestcontainersIT {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  EntityManager em;

  @Inject
  CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  @Transactional
  public void setup() {
    // Clean database
    warehouseRepository.deleteAll();
  }

  /**
   * Test database unique constraint on business unit code.
   */
  @Test
  @Transactional
  public void testDatabaseUniqueConstraintOnBusinessUnitCode() {
    // Create first warehouse
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "DB-UNIQUE-001";
    warehouse1.location = "AMSTERDAM-001";
    warehouse1.capacity = 50;
    warehouse1.stock = 10;

    createWarehouseUseCase.create(warehouse1);

    // Try to create second with same code directly via DB
    Warehouse warehouse2 = new Warehouse();
    warehouse2.businessUnitCode = "DB-UNIQUE-001";  // Duplicate!
    warehouse2.location = "ZWOLLE-001";
    warehouse2.capacity = 30;
    warehouse2.stock = 5;
    warehouse2.createdAt = LocalDateTime.now();

    // Database should reject this
    assertThrows(Exception.class, () -> {
      // We persist directly to bypass use case validations and test the DB constraint
      em.persist(warehouse2);
      em.flush();
    });
  }

  /**
   * Test query performance and correctness with multiple warehouses.
   */
  @Test
  @Transactional
  public void testQueryingMultipleWarehousesAtSameLocation() {
    // Create multiple warehouses at same location
    for (int i = 0; i < 5; i++) {
      Warehouse warehouse = new Warehouse();
      warehouse.businessUnitCode = "QUERY-TEST-" + i;
      warehouse.location = "AMSTERDAM-001";
      warehouse.capacity = 20 + (i * 10);
      warehouse.stock = 5 + i;
      
      createWarehouseUseCase.create(warehouse);
    }
    
    // Query all warehouses
    List<Warehouse> all = warehouseRepository.listAll();
    
    // Should have at least 5
    assertTrue(all.size() >= 5);
    
    // Verify they're from Amsterdam
    long amsterdamCount = all.stream()
        .filter(w -> "AMSTERDAM-001".equals(w.location))
        .count();
    
    assertEquals(5, amsterdamCount);
  }

  /**
   * Test database handles NULL values correctly.
   */
  @Test
  @Transactional
  public void testNullFieldsHandling() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NULL-TEST-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;  // NULL archived date

    em.persist(warehouse);
    em.flush();

    // Retrieve and verify
    Warehouse found = em.find(Warehouse.class, warehouse.id);
    assertNotNull(found);
    assertNull(found.archivedAt);
  }

  /**
   * Test database transaction rollback behavior.
   */
  @Test
  public void testTransactionRollbackDoesNotPersist() {
    try {
      performFailingTransaction();
    } catch (Exception e) {
      // Expected
    }
    
    // Verify nothing was persisted
    Warehouse found = warehouseRepository.findByBusinessUnitCode("ROLLBACK-TEST-001");
    assertNull(found, "Rolled back warehouse should not exist in database");
  }

  @Transactional
  void performFailingTransaction() {

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "ROLLBACK-TEST-001";
    warehouse.location = "TILBURG-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    em.persist(warehouse);
    em.flush();

    throw new RuntimeException("Simulated failure");
  }

  /**
   * Test complex query: find warehouses by location and capacity range.
   */
  @Test
  @Transactional
  public void testComplexQueryByLocationAndCapacity() {
    // Create warehouses with different capacities
    createWarehouse("COMPLEX-1", "AMSTERDAM-001", 30);
    createWarehouse("COMPLEX-2", "AMSTERDAM-001", 50);
    createWarehouse("COMPLEX-3", "AMSTERDAM-001", 70);
    createWarehouse("COMPLEX-4", "ZWOLLE-001", 40);
    
    // Query using JPQL
    List<Warehouse> results = em.createQuery(
        "SELECT w FROM Warehouse w WHERE w.location = :location AND w.capacity BETWEEN :min AND :max",
        Warehouse.class)
        .setParameter("location", "AMSTERDAM-001")
        .setParameter("min", 40)
        .setParameter("max", 70)
        .getResultList();
    
    // Should find COMPLEX-2 and COMPLEX-3
    assertEquals(2, results.size());
  }

  private void createWarehouse(String code, String location, int capacity) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = 10;
    createWarehouseUseCase.create(warehouse);
  }
  @Test
  @Transactional
  public void testDeleteWarehouse() {

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "DELETE-TEST-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 40;
    warehouse.stock = 10;

    createWarehouseUseCase.create(warehouse);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("DELETE-TEST-001");
    assertNotNull(found);

    warehouseRepository.delete(found);

    Warehouse afterDelete = warehouseRepository.findByBusinessUnitCode("DELETE-TEST-001");
    assertNull(afterDelete);
  }
}
