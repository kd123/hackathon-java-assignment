package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseUnitTest {

  WarehouseStore store;
  LocationResolver resolver;
  ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    store = mock(WarehouseStore.class);
    resolver = mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(store, resolver);
  }

  @Test
  void replaceHappyPath() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "B1";
    existing.archivedAt = null;
    when(store.findByBusinessUnitCode("B1")).thenReturn(existing);

    when(resolver.resolveByIdentifier("L1")).thenReturn(new Location("L1", 1, 100));

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "B1";
    newW.location = "L1";
    newW.capacity = 20;
    newW.stock = 5;

    useCase.replace(newW);

    verify(store).update(existing);
    assertEquals("L1", existing.location);
    assertEquals(20, existing.capacity);
    assertEquals(5, existing.stock);
  }

  @Test
  void replaceFailsWhenNotExists() {
    when(store.findByBusinessUnitCode("X")).thenReturn(null);
    Warehouse newW = new Warehouse(); newW.businessUnitCode = "X";
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(newW));
    assertTrue(ex.getMessage().contains("does not exist"));
  }

  @Test
  void replaceFailsWhenArchived() {
    Warehouse existing = new Warehouse(); existing.businessUnitCode = "B2"; 
    existing.archivedAt = java.time.LocalDateTime.now();
    when(store.findByBusinessUnitCode("B2")).thenReturn(existing);
    Warehouse newW = new Warehouse(); newW.businessUnitCode = "B2";
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(newW));
    assertTrue(ex.getMessage().contains("archived"));
  }

  @Test
  void replaceFailsWhenLocationInvalid() {

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "B3";
    existing.archivedAt = null;

    when(store.findByBusinessUnitCode("B3")).thenReturn(existing);
    when(resolver.resolveByIdentifier("BAD")).thenReturn(null);

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "B3";
    newW.location = "BAD";

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> useCase.replace(newW));

    assertTrue(ex.getMessage().contains("not valid"));
  }

  @Test
  void replaceFailsWhenCapacityExceedsLocationMax() {

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "B4";
    existing.archivedAt = null;

    when(store.findByBusinessUnitCode("B4")).thenReturn(existing);
    when(resolver.resolveByIdentifier("LOC"))
            .thenReturn(new Location("LOC", 1, 50));

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "B4";
    newW.location = "LOC";
    newW.capacity = 100;
    newW.stock = 10;

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> useCase.replace(newW));

    assertTrue(ex.getMessage().contains("exceeds location max capacity"));
  }

  @Test
  void replaceFailsWhenStockExceedsCapacity() {

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "B5";
    existing.archivedAt = null;

    when(store.findByBusinessUnitCode("B5")).thenReturn(existing);
    when(resolver.resolveByIdentifier("LOC"))
            .thenReturn(new Location("LOC", 1, 100));

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "B5";
    newW.location = "LOC";
    newW.capacity = 20;
    newW.stock = 50;

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> useCase.replace(newW));

    assertTrue(ex.getMessage().contains("exceeds warehouse capacity"));
  }
}
