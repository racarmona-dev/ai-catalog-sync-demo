package com.demo.supplier.service;

import com.demo.supplier.dto.SupplierProductDto;
import com.demo.supplier.entity.SupplierProduct;
import com.demo.supplier.repository.SupplierProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SupplierService {

  private final SupplierProductRepository repository;
  private final RestTemplate restTemplate;

  @Value("${n8n.webhook-url}")
  private String n8nWebhookUrl;

  public SupplierService(SupplierProductRepository repository, RestTemplate restTemplate) {
    this.repository = repository;
    this.restTemplate = restTemplate;
  }

  public List<SupplierProductDto> findAll() {
    return repository.findAll().stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public SupplierProductDto create(SupplierProductDto dto) {
    SupplierProduct entity = new SupplierProduct();
    entity.setName(dto.getName());
    entity.setPrice(dto.getPrice());
    return toDto(repository.save(entity));
  }

  public SupplierProductDto update(Long id, SupplierProductDto dto) {
    SupplierProduct entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Supplier product not found: " + id));
    entity.setName(dto.getName());
    entity.setPrice(dto.getPrice());
    return toDto(repository.save(entity));
  }

  public void triggerSync() {
    restTemplate.postForEntity(n8nWebhookUrl, null, Void.class);
  }

  private SupplierProductDto toDto(SupplierProduct p) {
    SupplierProductDto dto = new SupplierProductDto();
    dto.setId(p.getId());
    dto.setName(p.getName());
    dto.setPrice(p.getPrice());
    return dto;
  }
}
