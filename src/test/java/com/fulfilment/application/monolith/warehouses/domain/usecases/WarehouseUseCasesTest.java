package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class WarehouseUseCasesTest {

    @Inject
    ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @Inject
    ReplaceWarehouseUseCase replaceWarehouseUseCase;

    @InjectMock
    WarehouseStore warehouseStore;

    @InjectMock
    LocationResolver locationResolver;

    private Warehouse activeWarehouse;
    private Warehouse archivedWarehouse;
    private Location validLocation;

    @BeforeEach
    void setUp() {
        activeWarehouse = new Warehouse();
        activeWarehouse.businessUnitCode = "WH-001";
        activeWarehouse.archivedAt = null;

        archivedWarehouse = new Warehouse();
        archivedWarehouse.businessUnitCode = "WH-002";
        archivedWarehouse.archivedAt = LocalDateTime.now();

        validLocation = new Location("AMSTERDAM-001", 5, 100);
    }

    // --- ArchiveWarehouseUseCase Tests ---

    @Test
    void testArchiveActiveWarehouse() {
        when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(activeWarehouse);
        archiveWarehouseUseCase.archive(activeWarehouse);
        assertNotNull(activeWarehouse.archivedAt);
        verify(warehouseStore).update(activeWarehouse);
    }

    @Test
    void testArchiveAlreadyArchivedWarehouse() {
        when(warehouseStore.findByBusinessUnitCode(archivedWarehouse.businessUnitCode)).thenReturn(archivedWarehouse);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            archiveWarehouseUseCase.archive(archivedWarehouse);
        });
        assertTrue(exception.getMessage().contains("is already archived"));
    }

    // --- ReplaceWarehouseUseCase Tests ---

    @Test
    void testReplaceActiveWarehouse() {
        Warehouse replacementData = new Warehouse();
        replacementData.businessUnitCode = "WH-001";
        replacementData.location = "AMSTERDAM-001";
        replacementData.capacity = 80;
        replacementData.stock = 40;

        when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(validLocation);
        when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(activeWarehouse);

        replaceWarehouseUseCase.replace(replacementData);

        verify(warehouseStore).update(activeWarehouse);
        assertEquals("AMSTERDAM-001", activeWarehouse.location);
        assertEquals(80, activeWarehouse.capacity);
        assertEquals(40, activeWarehouse.stock);
    }

    @Test
    void testReplaceArchivedWarehouse() {
        when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(archivedWarehouse);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace(archivedWarehouse);
        });
        assertTrue(exception.getMessage().contains("is archived and cannot be replaced"));
    }

    @Test
    void testReplaceWithInvalidLocation() {
        Warehouse replacementData = new Warehouse();
        replacementData.businessUnitCode = "WH-001";
        replacementData.location = "INVALID-LOC";

        when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(activeWarehouse);
        when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            replaceWarehouseUseCase.replace(replacementData);
        });
        assertTrue(exception.getMessage().contains("is not valid"));
    }
}