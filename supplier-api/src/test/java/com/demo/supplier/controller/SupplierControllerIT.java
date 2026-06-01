package com.demo.supplier.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.demo.supplier.dto.SupplierProductDto;
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
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SupplierControllerIT {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("supplier_db")
      .withUsername("demo")
      .withPassword("demo");

  @Container
  static MockServerContainer mockServer = new MockServerContainer(
      DockerImageName.parse("mockserver/mockserver:5.15.0")
  );

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
    registry.add("n8n.webhook-url",
        () -> "http://" + mockServer.getHost() + ":" + mockServer.getServerPort() + "/webhook/catalog-sync");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void GET_products_returns200WithSeededData() {
    ResponseEntity<List<SupplierProductDto>> response = restTemplate.exchange(
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
    SupplierProductDto request = buildDto("Integration Product", "55.00");

    ResponseEntity<SupplierProductDto> response = restTemplate.postForEntity(
        "/api/products", request, SupplierProductDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getId()).isNotNull();
    assertThat(response.getBody().getName()).isEqualTo("Integration Product");
  }

  @Test
  void PUT_product_updatesInDatabase() {
    SupplierProductDto created = restTemplate.postForEntity(
        "/api/products",
        buildDto("Before Update", "10.00"),
        SupplierProductDto.class
    ).getBody();

    assertThat(created).isNotNull();

    SupplierProductDto update = buildDto("After Update", "30.00");
    ResponseEntity<SupplierProductDto> response = restTemplate.exchange(
        "/api/products/" + created.getId(),
        HttpMethod.PUT,
        new HttpEntity<>(update),
        SupplierProductDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getName()).isEqualTo("After Update");
    assertThat(response.getBody().getPrice()).isEqualByComparingTo("30.00");
  }

  @Test
  void POST_syncTrigger_returns202() {
    // MockServer aceita qualquer requisição por padrão — apenas validamos o status HTTP
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/sync/trigger",
        HttpMethod.POST,
        null,
        Void.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  private SupplierProductDto buildDto(String name, String price) {
    SupplierProductDto dto = new SupplierProductDto();
    dto.setName(name);
    dto.setPrice(new BigDecimal(price));
    return dto;
  }
}
