package com.demo.marketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.marketplace.dto.ProductDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerIT {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("marketplace_db")
      .withUsername("demo")
      .withPassword("demo");

  @DynamicPropertySource
  static void overrideDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void GET_products_returns200WithSeededData() {
    ResponseEntity<List<ProductDto>> response = restTemplate.exchange(
        "/api/products",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void POST_product_persistsAndReturns201() {
    ProductDto request = buildDto("Integration Test Product", "99.99", true);

    ResponseEntity<ProductDto> response = restTemplate.postForEntity(
        "/api/products", request, ProductDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isNotNull();
    assertThat(response.getBody().getName()).isEqualTo("Integration Test Product");
  }

  @Test
  void PUT_product_updatesInDatabase() {
    // Cria o produto
    ProductDto created = restTemplate.postForEntity(
        "/api/products",
        buildDto("Before Update", "10.00", true),
        ProductDto.class
    ).getBody();

    assertThat(created).isNotNull();

    // Atualiza
    ProductDto update = buildDto("After Update", "20.00", true);
    ResponseEntity<ProductDto> response = restTemplate.exchange(
        "/api/products/" + created.getId(),
        HttpMethod.PUT,
        new HttpEntity<>(update),
        ProductDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getName()).isEqualTo("After Update");
    assertThat(response.getBody().getPrice()).isEqualByComparingTo("20.00");
  }

  @Test
  void PUT_nonExistingProduct_returns500() {
    ProductDto update = buildDto("Ghost", "1.00", true);

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/products/99999",
        HttpMethod.PUT,
        new HttpEntity<>(update),
        String.class
    );

    assertThat(response.getStatusCode().is5xxServerError()).isTrue();
  }

  @Test
  void POST_product_withoutName_returns400() {
    ProductDto invalid = buildDto(null, "10.00", true);

    ResponseEntity<String> response = restTemplate.postForEntity(
        "/api/products", invalid, String.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private ProductDto buildDto(String name, String price, boolean enabled) {
    ProductDto dto = new ProductDto();
    dto.setName(name);
    dto.setPrice(new BigDecimal(price));
    dto.setEnabled(enabled);
    return dto;
  }
}
