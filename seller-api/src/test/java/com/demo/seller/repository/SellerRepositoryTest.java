package com.demo.seller.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.seller.entity.SellerProduct;
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
class SellerRepositoryTest {

  @Autowired
  private TestEntityManager em;

  @Autowired
  private SellerProductRepository repository;

  @Test
  void save_persistsProduct() {
    SellerProduct product = buildProduct("Wireless Headphones", "99.90");

    SellerProduct saved = repository.save(product);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("Wireless Headphones");
  }

  @Test
  void findById_returnsProduct() {
    SellerProduct product = em.persistAndFlush(buildProduct("USB-C Hub", "49.90"));

    Optional<SellerProduct> found = repository.findById(product.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("USB-C Hub");
  }

  @Test
  void findAll_returnsAllSavedProducts() {
    em.persistAndFlush(buildProduct("Keyboard", "110.00"));
    em.persistAndFlush(buildProduct("Webcam", "80.00"));

    List<SellerProduct> result = repository.findAll();

    assertThat(result).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void save_updatesExistingProduct() {
    SellerProduct product = em.persistAndFlush(buildProduct("Old Name", "10.00"));

    product.setName("New Name");
    product.setPrice(new BigDecimal("25.00"));
    repository.save(product);
    em.flush();
    em.clear();

    SellerProduct updated = repository.findById(product.getId()).orElseThrow();
    assertThat(updated.getName()).isEqualTo("New Name");
    assertThat(updated.getPrice()).isEqualByComparingTo("25.00");
  }

  @Test
  void delete_removesProduct() {
    SellerProduct product = em.persistAndFlush(buildProduct("To Delete", "5.00"));
    Long id = product.getId();

    repository.deleteById(id);

    assertThat(repository.findById(id)).isEmpty();
  }

  private SellerProduct buildProduct(String name, String price) {
    SellerProduct p = new SellerProduct();
    p.setName(name);
    p.setPrice(new BigDecimal(price));
    return p;
  }
}
