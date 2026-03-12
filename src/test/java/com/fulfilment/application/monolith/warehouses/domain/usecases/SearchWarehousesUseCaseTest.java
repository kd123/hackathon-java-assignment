package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class SearchWarehousesUseCaseTest {

    @Inject
    SearchWarehousesUseCase useCase;

    @InjectMock
    WarehouseRepository warehouseRepository;

    @Test
    void execute_withValidCommand_delegatesToRepository() {
        // Arrange
        SearchWarehousesUseCase.SearchCommand cmd = new SearchWarehousesUseCase.SearchCommand(
                "AMSTERDAM-001", 10, 100, "createdAt", "asc", 0, 10);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "TEST-001";
        SearchWarehousesUseCase.PagedResult<Warehouse> expectedResult = SearchWarehousesUseCase.PagedResult.from(
                List.of(warehouse), 0, 10, 1);

        when(warehouseRepository.search(cmd)).thenReturn(expectedResult);

        // Act
        SearchWarehousesUseCase.PagedResult<Warehouse> actualResult = useCase.execute(cmd);

        // Assert
        assertNotNull(actualResult);
        assertEquals(1, actualResult.totalItems());
        assertEquals(1, actualResult.items().size());
        assertEquals("TEST-001", actualResult.items().get(0).businessUnitCode);
        verify(warehouseRepository).search(cmd);
    }

    @Test
    void execute_whenRepositoryReturnsEmpty_returnsEmptyPagedResult() {
        // Arrange
        SearchWarehousesUseCase.SearchCommand cmd = new SearchWarehousesUseCase.SearchCommand(
                null, null, null, "capacity", "desc", 1, 20);
        SearchWarehousesUseCase.PagedResult<Warehouse> emptyResult = SearchWarehousesUseCase.PagedResult.from(
                Collections.emptyList(), 1, 20, 0);
        when(warehouseRepository.search(cmd)).thenReturn(emptyResult);
        // Act
        SearchWarehousesUseCase.PagedResult<Warehouse> actualResult = useCase.execute(cmd);
        // Assert
        assertNotNull(actualResult);
        assertEquals(0, actualResult.totalItems());
        assertTrue(actualResult.items().isEmpty());
        assertEquals(20, actualResult.pageSize());
        verify(warehouseRepository).search(cmd);
    }

    @Test
    void execute_withNullFilters_stillDelegatesToRepository() {

        SearchWarehousesUseCase.SearchCommand cmd =
                new SearchWarehousesUseCase.SearchCommand(
                        null, null, null, null, null, 0, 5);

        SearchWarehousesUseCase.PagedResult<Warehouse> result =
                SearchWarehousesUseCase.PagedResult.from(
                        List.of(), 0, 5, 0);

        when(warehouseRepository.search(cmd)).thenReturn(result);

        SearchWarehousesUseCase.PagedResult<Warehouse> response =
                useCase.execute(cmd);

        assertNotNull(response);
        assertEquals(0, response.totalItems());
        assertEquals(5, response.pageSize());

        verify(warehouseRepository).search(cmd);
    }
}
