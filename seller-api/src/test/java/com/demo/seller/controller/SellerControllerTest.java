package com.demo.seller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.seller.dto.SellerProductDto;
import com.demo.seller.service.SellerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SellerController.class)
class SellerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private SellerService service;

  @Test
  void GET_products_returns200WithList() throws Exception {
    SellerProductDto dto = buildDto(1L, "Wireless Headphones", "99.90");
    when(service.findAll()).thenReturn(List.of(dto));

    mockMvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Wireless Headphones"))
        .andExpect(jsonPath("$[0].price").value(99.90));
  }

  @Test
  void POST_product_returns201() throws Exception {
    SellerProductDto request = buildDto(null, "USB-C Hub", "49.90");
    SellerProductDto response = buildDto(2L, "USB-C Hub", "49.90");

    when(service.create(any(SellerProductDto.class))).thenReturn(response);

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.name").value("USB-C Hub"));
  }

  @Test
  void POST_product_withoutName_returns400() throws Exception {
    SellerProductDto invalid = buildDto(null, null, "10.00");

    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void PUT_product_returns200WithUpdated() throws Exception {
    SellerProductDto request = buildDto(null, "New Name", "129.90");
    SellerProductDto response = buildDto(1L, "New Name", "129.90");

    when(service.update(eq(1L), any(SellerProductDto.class))).thenReturn(response);

    mockMvc.perform(put("/api/products/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"))
        .andExpect(jsonPath("$.price").value(129.90));
  }

  @Test
  void POST_syncTrigger_returns202() throws Exception {
    doNothing().when(service).triggerSync();

    mockMvc.perform(post("/api/sync/trigger"))
        .andExpect(status().isAccepted());
  }

  @Test
  void POST_syncTrigger_whenN8nFails_exceptionPropagates() {
    doThrow(new RuntimeException("n8n unavailable")).when(service).triggerSync();

    // @WebMvcTest propaga exceções não tratadas — em produção o Spring converte para 500
    assertThatThrownBy(() -> mockMvc.perform(post("/api/sync/trigger")))
        .hasRootCauseInstanceOf(RuntimeException.class)
        .hasMessageContaining("n8n unavailable");
  }

  private SellerProductDto buildDto(Long id, String name, String price) {
    SellerProductDto dto = new SellerProductDto();
    dto.setId(id);
    dto.setName(name);
    dto.setPrice(price != null ? new BigDecimal(price) : null);
    return dto;
  }
}
