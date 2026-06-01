package com.demo.seller.service;

import com.demo.seller.dto.SellerProductDto;
import com.demo.seller.entity.SellerProduct;
import com.demo.seller.repository.SellerProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SellerService {

  private final SellerProductRepository repository;
  private final RestTemplate restTemplate;

  @Value("${n8n.webhook-url}")
  private String n8nWebhookUrl;

  public SellerService(SellerProductRepository repository, RestTemplate restTemplate) {
    this.repository = repository;
    this.restTemplate = restTemplate;
  }

  public List<SellerProductDto> findAll() {
    return repository.findAll().stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public SellerProductDto create(SellerProductDto dto) {
    SellerProduct entity = new SellerProduct();
    entity.setName(dto.getName());
    entity.setPrice(dto.getPrice());
    return toDto(repository.save(entity));
  }

  public SellerProductDto update(Long id, SellerProductDto dto) {
    SellerProduct entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Seller product not found: " + id));
    entity.setName(dto.getName());
    entity.setPrice(dto.getPrice());
    return toDto(repository.save(entity));
  }

  public void triggerSync() {
    restTemplate.postForEntity(n8nWebhookUrl, null, Void.class);
  }

  private SellerProductDto toDto(SellerProduct p) {
    SellerProductDto dto = new SellerProductDto();
    dto.setId(p.getId());
    dto.setName(p.getName());
    dto.setPrice(p.getPrice());
    return dto;
  }
}
