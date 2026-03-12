package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class SearchWarehousesUseCase {

  @Inject
  WarehouseRepository warehouseRepository;

  public PagedResult<Warehouse> execute(SearchCommand command) {
    return warehouseRepository.search(command);
  }

  public record SearchCommand(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      int page,
      int pageSize
  ) {

  }

  public record PagedResult<T>(
      List<T> items,
      int currentPage,
      int pageSize,
      long totalItems,
      int totalPages
  ) {

    public static <T> PagedResult<T> from(List<T> items, int page, int pageSize, long totalItems) {
      int totalPages = (int) Math.ceil((double) totalItems / pageSize);
      return new PagedResult<>(items, page, pageSize, totalItems, totalPages);
    }
  }
}

