package com.demo.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.demo.marketplace.dto.ProductDto;
import com.demo.marketplace.entity.Product;
import com.demo.marketplace.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ProductRepository repository;

  @InjectMocks
  private ProductService service;

  private Product product;

  @BeforeEach
  void setUp() {
    product = new Product();
    product.setId(1L);
    product.setName("Wireless Headphones");
    product.setPrice(new BigDecimal("89.90"));
    product.setEnabled(true);
  }

  @Test
  void findAll_returnsAllProducts() {
    when(repository.findAll()).thenReturn(List.of(product));

    List<ProductDto> result = service.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Wireless Headphones");
    assertThat(result.get(0).getPrice()).isEqualByComparingTo("89.90");
  }

  @Test
  void findAll_whenEmpty_returnsEmptyList() {
    when(repository.findAll()).thenReturn(List.of());

    assertThat(service.findAll()).isEmpty();
  }

  @Test
  void create_savesAndReturnsDto() {
    ProductDto dto = new ProductDto();
    dto.setName("USB-C Hub");
    dto.setPrice(new BigDecimal("45.00"));

    when(repository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(2L);
      return p;
    });

    ProductDto result = service.create(dto);

    assertThat(result.getId()).isEqualTo(2L);
    assertThat(result.getName()).isEqualTo("USB-C Hub");
    assertThat(result.getEnabled()).isTrue();
    verify(repository).save(any(Product.class));
  }

  @Test
  void create_withEnabledFalse_persistsEnabledFalse() {
    ProductDto dto = new ProductDto();
    dto.setName("Old Product");
    dto.setPrice(new BigDecimal("10.00"));
    dto.setEnabled(false);

    when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    ProductDto result = service.create(dto);

    assertThat(result.getEnabled()).isFalse();
  }

  @Test
  void update_existingProduct_updatesFields() {
    ProductDto dto = new ProductDto();
    dto.setName("Wireless Headphones Pro");
    dto.setPrice(new BigDecimal("129.90"));

    when(repository.findById(1L)).thenReturn(Optional.of(product));
    when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    ProductDto result = service.update(1L, dto);

    assertThat(result.getName()).isEqualTo("Wireless Headphones Pro");
    assertThat(result.getPrice()).isEqualByComparingTo("129.90");
  }

  @Test
  void update_nonExistingProduct_throwsIllegalArgumentException() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    ProductDto dto = new ProductDto();
    dto.setName("Ghost");
    dto.setPrice(BigDecimal.TEN);

    assertThatThrownBy(() -> service.update(99L, dto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("99");
  }
}
