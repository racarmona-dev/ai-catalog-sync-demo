package com.demo.marketplace.service;

import com.demo.marketplace.dto.ProductDto;
import com.demo.marketplace.entity.Product;
import com.demo.marketplace.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  private final ProductRepository repository;

  public ProductService(ProductRepository repository) {
    this.repository = repository;
  }

  public List<ProductDto> findAll() {
    return repository.findAll().stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public ProductDto create(ProductDto dto) {
    Product product = new Product();
    product.setName(dto.getName());
    product.setPrice(dto.getPrice());
    product.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
    return toDto(repository.save(product));
  }

  public ProductDto update(Long id, ProductDto dto) {
    Product product = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    product.setName(dto.getName());
    product.setPrice(dto.getPrice());
    if (dto.getEnabled() != null) {
      product.setEnabled(dto.getEnabled());
    }
    return toDto(repository.save(product));
  }

  private ProductDto toDto(Product p) {
    ProductDto dto = new ProductDto();
    dto.setId(p.getId());
    dto.setName(p.getName());
    dto.setPrice(p.getPrice());
    dto.setEnabled(p.getEnabled());
    return dto;
  }
}
