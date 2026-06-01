package com.demo.supplier.controller;

import com.demo.supplier.dto.SupplierProductDto;
import com.demo.supplier.service.SupplierService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SupplierController {

  private final SupplierService service;

  public SupplierController(SupplierService service) {
    this.service = service;
  }

  @GetMapping("/products")
  public ResponseEntity<List<SupplierProductDto>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @PostMapping("/products")
  public ResponseEntity<SupplierProductDto> create(@Valid @RequestBody SupplierProductDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<SupplierProductDto> update(
      @PathVariable Long id,
      @Valid @RequestBody SupplierProductDto dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }

  /**
   * Trigger: the supplier calls this endpoint whenever they want to sync
   * their catalog with the marketplace. n8n will take it from here.
   */
  @PostMapping("/sync/trigger")
  public ResponseEntity<Void> triggerSync() {
    service.triggerSync();
    return ResponseEntity.accepted().build();
  }
}
