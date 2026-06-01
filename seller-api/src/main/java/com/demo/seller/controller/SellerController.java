package com.demo.seller.controller;

import com.demo.seller.dto.SellerProductDto;
import com.demo.seller.service.SellerService;
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
public class SellerController {

  private final SellerService service;

  public SellerController(SellerService service) {
    this.service = service;
  }

  @GetMapping("/products")
  public ResponseEntity<List<SellerProductDto>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @PostMapping("/products")
  public ResponseEntity<SellerProductDto> create(@Valid @RequestBody SellerProductDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<SellerProductDto> update(
      @PathVariable Long id,
      @Valid @RequestBody SellerProductDto dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }

  /**
   * Trigger: the seller calls this endpoint whenever they want to sync
   * their catalog with the marketplace. n8n will take it from here.
   */
  @PostMapping("/sync/trigger")
  public ResponseEntity<Void> triggerSync() {
    service.triggerSync();
    return ResponseEntity.accepted().build();
  }
}
