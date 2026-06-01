package com.demo.supplier.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.demo.supplier.dto.SupplierProductDto;
import com.demo.supplier.entity.SupplierProduct;
import com.demo.supplier.repository.SupplierProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

  @Mock
  private SupplierProductRepository repository;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private SupplierService service;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "n8nWebhookUrl", "http://mock-n8n/webhook/catalog-sync");
  }

  @Test
  void findAll_returnsAllProducts() {
    SupplierProduct p = buildEntity(1L, "Wireless Headphones", "99.90");
    when(repository.findAll()).thenReturn(List.of(p));

    List<SupplierProductDto> result = service.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Wireless Headphones");
    assertThat(result.get(0).getPrice()).isEqualByComparingTo("99.90");
  }

  @Test
  void findAll_whenEmpty_returnsEmptyList() {
    when(repository.findAll()).thenReturn(List.of());

    assertThat(service.findAll()).isEmpty();
  }

  @Test
  void create_savesAndReturnsDto() {
    SupplierProductDto dto = new SupplierProductDto();
    dto.setName("USB-C Hub");
    dto.setPrice(new BigDecimal("49.90"));

    when(repository.save(any(SupplierProduct.class))).thenAnswer(inv -> {
      SupplierProduct entity = inv.getArgument(0);
      entity.setId(2L);
      return entity;
    });

    SupplierProductDto result = service.create(dto);

    assertThat(result.getId()).isEqualTo(2L);
    assertThat(result.getName()).isEqualTo("USB-C Hub");
    verify(repository).save(any(SupplierProduct.class));
  }

  @Test
  void update_existingProduct_updatesFields() {
    SupplierProduct entity = buildEntity(1L, "Old Name", "10.00");
    when(repository.findById(1L)).thenReturn(Optional.of(entity));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    SupplierProductDto dto = new SupplierProductDto();
    dto.setName("New Name");
    dto.setPrice(new BigDecimal("20.00"));

    SupplierProductDto result = service.update(1L, dto);

    assertThat(result.getName()).isEqualTo("New Name");
    assertThat(result.getPrice()).isEqualByComparingTo("20.00");
  }

  @Test
  void update_nonExistingProduct_throwsException() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    SupplierProductDto dto = new SupplierProductDto();
    dto.setName("Ghost");
    dto.setPrice(BigDecimal.TEN);

    assertThatThrownBy(() -> service.update(99L, dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("99");
  }

  @Test
  void triggerSync_callsN8nWebhook() {
    when(restTemplate.postForEntity(
        eq("http://mock-n8n/webhook/catalog-sync"),
        eq(null),
        eq(Void.class)
    )).thenReturn(ResponseEntity.ok().build());

    service.triggerSync();

    verify(restTemplate).postForEntity(
        eq("http://mock-n8n/webhook/catalog-sync"),
        eq(null),
        eq(Void.class)
    );
  }

  private SupplierProduct buildEntity(Long id, String name, String price) {
    SupplierProduct p = new SupplierProduct();
    p.setId(id);
    p.setName(name);
    p.setPrice(new BigDecimal(price));
    return p;
  }
}
