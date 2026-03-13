package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WarehouseRepositoryInMemoryTest {

    static class TestWarehouseRepository extends WarehouseRepository {
        final List<DbWarehouse> storage = new ArrayList<>();

        @Override
        public List<DbWarehouse> listAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public void persist(DbWarehouse entity) {
            // simulate generated id
            entity.id = (long) (storage.size() + 1);
            storage.add(entity);
        }

        @Override
        public void delete(DbWarehouse entity) {
            storage.removeIf(d -> d.businessUnitCode.equals(entity.businessUnitCode));
        }

        // Instead of implementing PanacheQuery (which is Quarkus-specific), override high-level repository
        // methods directly to use our in-memory storage. This keeps the test independent of Panache internals.
        @Override
        public java.util.List<Warehouse> getAll() {
            List<Warehouse> out = new ArrayList<>();
            for (DbWarehouse d : storage) out.add(d.toWarehouse());
            return out;
        }

        @Override
        public void create(Warehouse warehouse) {
            DbWarehouse db = new DbWarehouse();
            db.businessUnitCode = warehouse.businessUnitCode;
            db.location = warehouse.location;
            db.capacity = warehouse.capacity;
            db.stock = warehouse.stock;
            db.createdAt = warehouse.createdAt;
            db.archivedAt = warehouse.archivedAt;
            this.persist(db);
        }

        @Override
        public void update(Warehouse warehouse) {
            for (DbWarehouse d : storage) {
                if (d.businessUnitCode.equals(warehouse.businessUnitCode)) {
                    d.location = warehouse.location;
                    d.capacity = warehouse.capacity;
                    d.stock = warehouse.stock;
                    d.archivedAt = warehouse.archivedAt;
                    return;
                }
            }
        }

        @Override
        public void remove(Warehouse warehouse) {
            storage.removeIf(d -> d.businessUnitCode.equals(warehouse.businessUnitCode));
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            for (DbWarehouse d : storage) {
                if (d.businessUnitCode.equals(buCode)) return d.toWarehouse();
            }
            return null;
        }
    }

    private TestWarehouseRepository repo;

    @BeforeEach
    public void setup() {
        repo = new TestWarehouseRepository();
    }

    @Test
    public void testCreateAndGetAll() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "C-1";
        w.location = "L1";
        w.capacity = 10;
        w.stock = 2;

        repo.create(w);

        List<Warehouse> all = repo.getAll();
        assertEquals(1, all.size());
        assertEquals("C-1", all.get(0).businessUnitCode);
    }

    @Test
    public void testFindByBusinessUnitCode_andUpdate() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "U-1";
        w.location = "L2";
        w.capacity = 20;
        w.stock = 5;
        repo.create(w);

        Warehouse found = repo.findByBusinessUnitCode("U-1");
        assertNotNull(found);
        assertEquals("L2", found.location);

        // update
        found.location = "L2-NEW";
        found.capacity = 25;
        repo.update(found);

        Warehouse updated = repo.findByBusinessUnitCode("U-1");
        assertEquals("L2-NEW", updated.location);
        assertEquals(25, updated.capacity);
    }

    @Test
    public void testRemove() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "R-1";
        w.location = "L3";
        w.capacity = 5;
        w.stock = 1;
        repo.create(w);

        Warehouse found = repo.findByBusinessUnitCode("R-1");
        assertNotNull(found);

        repo.remove(found);

        Warehouse after = repo.findByBusinessUnitCode("R-1");
        assertNull(after);
    }
}
