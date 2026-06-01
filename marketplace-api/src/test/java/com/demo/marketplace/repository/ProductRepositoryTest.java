package com.demo.marketplace.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.marketplace.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

  @Autowired
  private TestEntityManager em;

  @Autowired
  private ProductRepository repository;

  @Test
  void save_persistsProduct() {
    Product product = buildProduct("Wireless Headphones", "89.90", true);

    Product saved = repository.save(product);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("Wireless Headphones");
  }

  @Test
  void findById_returnsProduct() {
    Product product = em.persistAndFlush(buildProduct("USB-C Hub", "45.00", true));

    Optional<Product> found = repository.findById(product.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("USB-C Hub");
  }

  @Test
  void findAll_returnsAllSavedProducts() {
    em.persistAndFlush(buildProduct("Keyboard", "120.00", true));
    em.persistAndFlush(buildProduct("Webcam", "75.50", true));

    List<Product> result = repository.findAll();

    assertThat(result).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void save_updatesExistingProduct() {
    Product product = em.persistAndFlush(buildProduct("Old Name", "10.00", true));

    product.setName("New Name");
    product.setPrice(new BigDecimal("20.00"));
    repository.save(product);
    em.flush();
    em.clear();

    Product updated = repository.findById(product.getId()).orElseThrow();
    assertThat(updated.getName()).isEqualTo("New Name");
    assertThat(updated.getPrice()).isEqualByComparingTo("20.00");
  }

  @Test
  void delete_removesProduct() {
    Product product = em.persistAndFlush(buildProduct("To Delete", "5.00", true));
    Long id = product.getId();

    repository.deleteById(id);

    assertThat(repository.findById(id)).isEmpty();
  }

  private Product buildProduct(String name, String price, boolean enabled) {
    Product p = new Product();
    p.setName(name);
    p.setPrice(new BigDecimal(price));
    p.setEnabled(enabled);
    return p;
  }
}
