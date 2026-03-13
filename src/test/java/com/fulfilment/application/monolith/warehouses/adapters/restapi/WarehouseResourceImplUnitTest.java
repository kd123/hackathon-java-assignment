package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase.PagedResult;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase.SearchCommand;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarehouseResourceImplUnitTest {

    private WarehouseResourceImpl resource;
    private WarehouseRepository warehouseRepository;
    private CreateWarehouseOperation createWarehouseOperation;
    private ArchiveWarehouseOperation archiveWarehouseOperation;
    private ReplaceWarehouseOperation replaceWarehouseOperation;
    private SearchWarehousesUseCase searchWarehousesUseCase;

    @BeforeEach
    public void setup() throws Exception {
        resource = new WarehouseResourceImpl();
        warehouseRepository = mock(WarehouseRepository.class);
        createWarehouseOperation = mock(CreateWarehouseOperation.class);
        archiveWarehouseOperation = mock(ArchiveWarehouseOperation.class);
        replaceWarehouseOperation = mock(ReplaceWarehouseOperation.class);
        searchWarehousesUseCase = mock(SearchWarehousesUseCase.class);

        // inject mocks into private fields
        setField(resource, "warehouseRepository", warehouseRepository);
        setField(resource, "createWarehouseOperation", createWarehouseOperation);
        setField(resource, "archiveWarehouseOperation", archiveWarehouseOperation);
        setField(resource, "replaceWarehouseOperation", replaceWarehouseOperation);
        setField(resource, "searchWarehousesUseCase", searchWarehousesUseCase);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void testListAllWarehousesUnits_empty() {
        when(warehouseRepository.getAll()).thenReturn(List.of());
        var result = resource.listAllWarehousesUnits();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testListAllWarehousesUnits_withItems() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "CODE-1";
        w.location = "LOC-1";
        w.capacity = 10;
        w.stock = 5;
        when(warehouseRepository.getAll()).thenReturn(List.of(w));

        var result = resource.listAllWarehousesUnits();
        assertEquals(1, result.size());
        var item = result.get(0);
        assertEquals("CODE-1", item.getBusinessUnitCode());
        assertEquals("LOC-1", item.getLocation());
        assertEquals(10, item.getCapacity());
        assertEquals(5, item.getStock());
    }

    @Test
    void testCreateANewWarehouseUnit_illegalArgument() {
        com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
        api.setBusinessUnitCode("NEW-1");
        api.setLocation("LOC");
        api.setCapacity(20);
        api.setStock(2);

        doThrow(new IllegalArgumentException("invalid")).when(createWarehouseOperation).create(any());

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(api));
        assertEquals(400, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("invalid") || ex.getResponse().getEntity() == null);
    }

    @Test
    void testGetAWarehouseUnitByID_notFound() {
        when(warehouseRepository.findByBusinessUnitCode("MISSING")).thenReturn(null);
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("MISSING"));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @SuppressWarnings("resource")
    void testArchiveAWarehouseUnitByID_notFound() {
        when(warehouseRepository.findByBusinessUnitCode("MISSING")).thenReturn(null);
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("MISSING"));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @SuppressWarnings("resource")
    void testArchiveAWarehouseUnitByID_illegalArgument() {
        Warehouse domain = new Warehouse();
        domain.businessUnitCode = "CODE";
        when(warehouseRepository.findByBusinessUnitCode("CODE")).thenReturn(domain);
        doThrow(new IllegalArgumentException("cannot archive")).when(archiveWarehouseOperation).archive(domain);

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("CODE"));
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void testReplaceTheCurrentActiveWarehouse_illegalArgument() {
        com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
        api.setLocation("LOC");
        api.setCapacity(10);
        api.setStock(1);

        doThrow(new IllegalArgumentException("replace invalid")).when(replaceWarehouseOperation).replace(any());

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.replaceTheCurrentActiveWarehouse("CODE", api));
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void testSearchWarehouses_returnsItems() {
        Warehouse domain = new Warehouse();
        domain.businessUnitCode = "S1";
        domain.location = "LOC";
        domain.capacity = 5;
        domain.stock = 2;

        PagedResult<Warehouse> paged = PagedResult.from(List.of(domain), 0, 10, 1);
        when(searchWarehousesUseCase.execute(any(SearchCommand.class))).thenReturn(paged);

        try (Response resp = resource.searchWarehouses(null, null, null, "createdAt", "asc", 0, 10)) {
            assertEquals(200, resp.getStatus());
            Object entity = resp.getEntity();
            assertNotNull(entity);
            Map<?, ?> map = assertInstanceOf(Map.class, entity);
            assertTrue(map.containsKey("items"));
            Object items = map.get("items");
            List<?> list = assertInstanceOf(List.class, items);
            assertEquals(1, list.size());
        }
    }
}
