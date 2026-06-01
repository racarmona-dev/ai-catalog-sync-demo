package com.demo.supplier.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SupplierProductDto {

  private Long id;

  @NotBlank(message = "Name is required")
  private String name;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
  private BigDecimal price;

  public Long getId() { return id; }
  public String getName() { return name; }
  public BigDecimal getPrice() { return price; }

  public void setId(Long id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setPrice(BigDecimal price) { this.price = price; }
}
