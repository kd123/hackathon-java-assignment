package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase.PagedResult;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase.SearchCommand;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements PanacheRepository<Warehouse> {

  public PagedResult<Warehouse> search(SearchCommand command) {
    List<String> conditions = new ArrayList<>();
    Parameters params = new Parameters();

    // Always exclude archived warehouses
    conditions.add("archivedAt IS NULL");

    if (command.location() != null && !command.location().isBlank()) {
      conditions.add("location = :location");
      params.and("location", command.location());
    }
    if (command.minCapacity() != null) {
      conditions.add("capacity >= :minCapacity");
      params.and("minCapacity", command.minCapacity());
    }
    if (command.maxCapacity() != null) {
      conditions.add("capacity <= :maxCapacity");
      params.and("maxCapacity", command.maxCapacity());
    }

    String queryString = String.join(" AND ", conditions);

    // Sorting
    String sortByField = "createdAt"; // default sort
    if ("capacity".equalsIgnoreCase(command.sortBy())) {
      sortByField = "capacity";
    }
    Sort.Direction direction = "desc".equalsIgnoreCase(command.sortOrder()) ? Sort.Direction.Descending : Sort.Direction.Ascending;
    Sort sort = Sort.by(sortByField, direction);

    PanacheQuery<Warehouse> query = find(queryString, sort, params);

    // Pagination
    int page = Math.max(0, command.page());
    int pageSize = Math.min(100, Math.max(1, command.pageSize()));
    query.page(page, pageSize);

    long totalCount = query.count();
    List<Warehouse> results = query.list();

    return PagedResult.from(results, page, pageSize, totalCount);
  }

  public void update(Warehouse warehouse) {
    // With Panache, calling persist on an entity with an existing ID will merge its state.
    // This is the correct way to ensure changes to a managed entity are flushed and that
    // optimistic locking checks are triggered upon transaction commit.
    persist(warehouse);
  }

  public Warehouse findByBusinessUnitCode(String buCode) {
    return find("businessUnitCode", buCode).firstResult();
  }
}
