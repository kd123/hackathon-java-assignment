package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class CreateWarehouseUseCaseUnitTest {


  WarehouseStore store;
  LocationResolver resolver;
  CreateWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    store = mock(WarehouseStore.class);
    resolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(store, resolver);
  }

  @Test
  void createHappyPath() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "CU-001";
    w.location = "AMSTERDAM-001";
    w.capacity = 10;
    w.stock = 5;

    when(store.findByBusinessUnitCode("CU-001")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(new Location("AMSTERDAM-001", 1, 100));

    useCase.create(w);

    ArgumentCaptor<Warehouse> cap = ArgumentCaptor.forClass(Warehouse.class);
    verify(store).create(cap.capture());
    Warehouse created = cap.getValue();
    assertEquals("CU-001", created.businessUnitCode);
    assertNotNull(created.createdAt);
    assertTrue(created.createdAt instanceof LocalDateTime);
  }

  @Test
  void createFailsOnDuplicateCode() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "DUP";
    when(store.findByBusinessUnitCode("DUP")).thenReturn(new Warehouse());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
    assertTrue(ex.getMessage().contains("already exists"));
  }

  @Test
  void createFailsOnInvalidLocation() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "C2";
    w.location = "UNKNOWN";
    when(store.findByBusinessUnitCode("C2")).thenReturn(null);
    when(resolver.resolveByIdentifier("UNKNOWN")).thenReturn(null);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
    assertTrue(ex.getMessage().contains("not valid"));
  }

  @Test
  void createFailsWhenCapacityExceedsLocationMax() {

    Warehouse w = new Warehouse();
    w.businessUnitCode = "CAP-FAIL";
    w.location = "LOC1";
    w.capacity = 200;
    w.stock = 10;

    when(store.findByBusinessUnitCode("CAP-FAIL")).thenReturn(null);
    when(resolver.resolveByIdentifier("LOC1"))
            .thenReturn(new Location("LOC1", 1, 100));

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> useCase.create(w));

    assertTrue(ex.getMessage().contains("exceeds location max capacity"));
  }

  @Test
  void createFailsWhenStockExceedsCapacity() {

    Warehouse w = new Warehouse();
    w.businessUnitCode = "STOCK-FAIL";
    w.location = "LOC2";
    w.capacity = 20;
    w.stock = 50;

    when(store.findByBusinessUnitCode("STOCK-FAIL")).thenReturn(null);
    when(resolver.resolveByIdentifier("LOC2"))
            .thenReturn(new Location("LOC2", 1, 100));

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> useCase.create(w));

    assertTrue(ex.getMessage().contains("exceeds warehouse capacity"));
  }
  @Test
  void storeIsNotCalledWhenValidationFails() {

    Warehouse w = new Warehouse();
    w.businessUnitCode = "FAIL";
    w.location = "UNKNOWN";
    w.capacity = 10;
    w.stock = 5;

    when(store.findByBusinessUnitCode("FAIL")).thenReturn(null);
    when(resolver.resolveByIdentifier("UNKNOWN")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> useCase.create(w));

    verify(store, never()).create(any());
  }

  @Test
  void createWhenCapacityEqualsLocationMaxSucceeds() {

    Warehouse w = new Warehouse();
    w.businessUnitCode = "CAP-OK";
    w.location = "LOC3";
    w.capacity = 100;
    w.stock = 50;

    when(store.findByBusinessUnitCode("CAP-OK")).thenReturn(null);
    when(resolver.resolveByIdentifier("LOC3"))
            .thenReturn(new Location("LOC3", 1, 100));

    useCase.create(w);

    verify(store).create(any());
  }

  @Test
  void createWhenStockEqualsCapacitySucceeds() {

    Warehouse w = new Warehouse();
    w.businessUnitCode = "STOCK-OK";
    w.location = "LOC4";
    w.capacity = 20;
    w.stock = 20;

    when(store.findByBusinessUnitCode("STOCK-OK")).thenReturn(null);
    when(resolver.resolveByIdentifier("LOC4"))
            .thenReturn(new Location("LOC4", 1, 100));

    useCase.create(w);

    verify(store).create(any());
  }
}

