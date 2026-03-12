package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.InjectMock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class CreateWarehouseUseCaseTest {

    @Inject
    CreateWarehouseUseCase createWarehouseUseCase;

    @InjectMock
    WarehouseStore warehouseStore;

    @InjectMock
    LocationResolver locationResolver;

    private Warehouse validWarehouse;
    private Location validLocation;

    @BeforeEach
    void setUp() {
        validLocation = new Location("AMSTERDAM-001", 5, 100);
        validWarehouse = new Warehouse();
        validWarehouse.businessUnitCode = "TEST-001";
        validWarehouse.location = "AMSTERDAM-001";
        validWarehouse.capacity = 80;
        validWarehouse.stock = 40;

        // Default mock behavior for happy path
        when(warehouseStore.findByBusinessUnitCode(any())).thenReturn(null);
        when(locationResolver.resolveByIdentifier(any())).thenReturn(validLocation);
    }

    @Test
    void create_withValidData_succeeds() {
        createWarehouseUseCase.create(validWarehouse);
        verify(warehouseStore).create(validWarehouse);
    }

    @Test
    void create_withDuplicateCode_throwsException() {
        when(warehouseStore.findByBusinessUnitCode("TEST-001")).thenReturn(new Warehouse());
        var exception = assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(validWarehouse));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(warehouseStore, never()).create(any());
    }

    @Test
    void create_withInvalidLocation_throwsException() {
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(null);
        var exception = assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(validWarehouse));
        assertTrue(exception.getMessage().contains("is not valid"));
        verify(warehouseStore, never()).create(any());
    }

    @Test
    void create_whenCapacityExceedsLocationMax_throwsException() {
        validWarehouse.capacity = 120; // Location max is 100
        var exception = assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(validWarehouse));
        assertTrue(exception.getMessage().contains("exceeds location max capacity"));
        verify(warehouseStore, never()).create(any());
    }

    @Test
    void create_whenStockExceedsCapacity_throwsException() {
        validWarehouse.stock = 90; // Capacity is 80
        var exception = assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(validWarehouse));
        assertTrue(exception.getMessage().contains("exceeds warehouse capacity"));
        verify(warehouseStore, never()).create(any());
    }

    @Test
    void create_whenCapacityEqualsLocationMax_succeeds() {

        validWarehouse.capacity = 100; // exactly location max

        createWarehouseUseCase.create(validWarehouse);

        verify(warehouseStore).create(validWarehouse);
    }

    @Test
    void create_whenStockEqualsCapacity_succeeds() {

        validWarehouse.stock = 80; // equal to capacity

        createWarehouseUseCase.create(validWarehouse);

        verify(warehouseStore).create(validWarehouse);
    }
}
