package com.demo.marketplace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.marketplace.dto.ProductDto;
import com.demo.marketplace.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ProductService service;

  @Test
  void GET_products_returns200WithList() throws Exception {
    ProductDto dto = buildDto(1L, "Wireless Headphones", "89.90", true);
    when(service.findAll()).thenReturn(List.of(dto));

    mockMvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Wireless Headphones"))
        .andExpect(jsonPath("$[0].price").value(89.90));
  }

  @Test
  void GET_products_whenEmpty_returns200WithEmptyArray() throws Exception {
    when(service.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void POST_product_returns201WithCreatedProduct() throws Exception {
    ProductDto request = buildDto(null, "USB-C Hub", "45.00", true);
    ProductDto response = buildDto(2L, "USB-C Hub", "45.00", true);

    when(service.create(any(ProductDto.class))).thenReturn(response);

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.name").value("USB-C Hub"));
  }

  @Test
  void POST_product_withoutName_returns400() throws Exception {
    ProductDto request = buildDto(null, null, "45.00", true);

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void POST_product_withZeroPrice_returns400() throws Exception {
    ProductDto request = buildDto(null, "Produto", "0.00", true);

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void PUT_product_returns200WithUpdatedProduct() throws Exception {
    ProductDto request = buildDto(null, "Wireless Headphones Pro", "129.90", true);
    ProductDto response = buildDto(1L, "Wireless Headphones Pro", "129.90", true);

    when(service.update(eq(1L), any(ProductDto.class))).thenReturn(response);

    mockMvc.perform(put("/api/products/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Wireless Headphones Pro"))
        .andExpect(jsonPath("$.price").value(129.90));
  }

  private ProductDto buildDto(Long id, String name, String price, Boolean enabled) {
    ProductDto dto = new ProductDto();
    dto.setId(id);
    dto.setName(name);
    dto.setPrice(price != null ? new BigDecimal(price) : null);
    dto.setEnabled(enabled);
    return dto;
  }
}
