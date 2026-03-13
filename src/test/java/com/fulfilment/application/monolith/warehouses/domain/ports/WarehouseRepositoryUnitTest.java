package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase.PagedResult;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase.SearchCommand;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarehouseRepositoryUnitTest {

    private WarehouseRepository spyRepo;
    private PanacheQuery<Warehouse> mockQuery;

    @BeforeEach
    public void setup() {
        WarehouseRepository repository = new WarehouseRepository();
        spyRepo = spy(repository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Warehouse> q = (PanacheQuery<Warehouse>) mock(PanacheQuery.class);
        mockQuery = q;
    }

    @Test
    public void testSearch_withFilters_returnsPagedResult() {
        // Arrange
        SearchCommand cmd = new SearchCommand("LOC-1", 10, 50, "createdAt", "asc", 0, 10);

        Warehouse w = new Warehouse();
        w.businessUnitCode = "W-1";
        doReturn(mockQuery).when(spyRepo).find(anyString(), any(Sort.class), any(Parameters.class));
        when(mockQuery.count()).thenReturn(1L);
        when(mockQuery.list()).thenReturn(List.of(w));
        when(mockQuery.page(anyInt(), anyInt())).thenReturn(mockQuery);

        // Act
        PagedResult<Warehouse> result = spyRepo.search(cmd);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.totalItems());
        assertEquals(1, result.items().size());
        assertEquals("W-1", result.items().get(0).businessUnitCode);
        verify(spyRepo).find(anyString(), any(Sort.class), any(Parameters.class));
        verify(mockQuery).page(0, 10);
    }

    @Test
    public void testSearch_paginationBounds_areClamped() {
        SearchCommand cmd = new SearchCommand(null, null, null, "capacity", "desc", -5, 500);

        Warehouse w = new Warehouse();
        w.businessUnitCode = "W-2";
        doReturn(mockQuery).when(spyRepo).find(anyString(), any(Sort.class), any(Parameters.class));
        when(mockQuery.count()).thenReturn(1L);
        when(mockQuery.list()).thenReturn(List.of(w));
        when(mockQuery.page(anyInt(), anyInt())).thenReturn(mockQuery);

        PagedResult<Warehouse> result = spyRepo.search(cmd);

        assertNotNull(result);
        assertEquals(1, result.totalItems());
        // verify that page called with 0 (clamped) and 100 (max)
        verify(mockQuery).page(0, 100);
    }

    @Test
    public void testUpdate_callsPersist() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "UP-1";

        doNothing().when(spyRepo).persist(w);

        spyRepo.update(w);

        verify(spyRepo).persist(w);
    }

    @Test
    public void testFindByBusinessUnitCode_returnsFirstResult() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "FIND-1";

        doReturn(mockQuery).when(spyRepo).find(anyString(), any(Object[].class));
        when(mockQuery.firstResult()).thenReturn(w);

        Warehouse found = spyRepo.findByBusinessUnitCode("FIND-1");
        assertNotNull(found);
        assertEquals("FIND-1", found.businessUnitCode);
        verify(spyRepo).find("businessUnitCode", "FIND-1");
        verify(mockQuery).firstResult();
    }
}
