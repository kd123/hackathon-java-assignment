package com.fulfilment.application.monolith.warehouses.domain.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
public class Warehouse extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String businessUnitCode;
    public String location;
    public int capacity;
    public int stock;
    public LocalDateTime createdAt;
    public LocalDateTime archivedAt;
    @Version
    public int version;
}