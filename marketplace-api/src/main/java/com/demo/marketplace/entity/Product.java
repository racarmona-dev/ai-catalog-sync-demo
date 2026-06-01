package com.demo.marketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Boolean enabled = true;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public BigDecimal getPrice() { return price; }
  public Boolean getEnabled() { return enabled; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }

  public void setId(Long id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setPrice(BigDecimal price) { this.price = price; }
  public void setEnabled(Boolean enabled) { this.enabled = enabled; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
