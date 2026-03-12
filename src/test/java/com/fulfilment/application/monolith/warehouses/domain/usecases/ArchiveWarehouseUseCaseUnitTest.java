package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseUnitTest {

  WarehouseStore warehouseStore;
  ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void archiveSuccessfully() {

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "WH1";
    existing.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("WH1")).thenReturn(existing);

    useCase.archive(existing);

    assertNotNull(existing.archivedAt);
    verify(warehouseStore).update(existing);
  }

  @Test
  void archiveFailsWhenWarehouseDoesNotExist() {

    when(warehouseStore.findByBusinessUnitCode("WH404")).thenReturn(null);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH404";

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class,
                    () -> useCase.archive(warehouse));

    assertTrue(ex.getMessage().contains("does not exist"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void archiveFailsWhenAlreadyArchived() {

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "WH2";
    existing.archivedAt = java.time.LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("WH2")).thenReturn(existing);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH2";

    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class,
                    () -> useCase.archive(warehouse));

    assertTrue(ex.getMessage().contains("already archived"));
    verify(warehouseStore, never()).update(any());
  }
}