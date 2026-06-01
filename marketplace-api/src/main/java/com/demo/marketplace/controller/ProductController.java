package com.demo.marketplace.controller;

import com.demo.marketplace.dto.ProductDto;
import com.demo.marketplace.service.ProductService;
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
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @PostMapping
  public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDto> update(
      @PathVariable Long id,
      @Valid @RequestBody ProductDto dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }
}
