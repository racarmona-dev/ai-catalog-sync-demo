# seller-api

API REST do fornecedor — gerencia o catálogo de produtos do lado do fornecedor e dispara a sincronização com o marketplace via webhook do n8n.

- **Porta:** `8081`
- **Banco:** `seller_db` (MySQL 8)
- **Framework:** Spring Boot 3.2 + Spring Data JPA + Flyway

---

## Endpoints

| Método | Endpoint             | Status | Descrição                              |
|--------|----------------------|--------|----------------------------------------|
| GET    | `/api/products`      | 200    | Lista os produtos do fornecedor        |
| POST   | `/api/products`      | 201    | Cria um produto no catálogo            |
| PUT    | `/api/products/{id}` | 200    | Atualiza nome e/ou preço               |
| POST   | `/api/sync/trigger`  | 202    | Dispara a sincronização via n8n        |

### GET /api/products

```bash
curl http://localhost:8081/api/products
```

```json
[
  { "id": 1, "name": "Wireless Headphones", "price": 99.90 },
  { "id": 2, "name": "USB-C Hub",           "price": 49.90 }
]
```

### PUT /api/products/{id}

Atualiza o catálogo do fornecedor. Após atualizar, chame `/api/sync/trigger` para propagar ao marketplace.

```bash
curl -X PUT http://localhost:8081/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Headphones Pro","price":129.90}'
```

### POST /api/sync/trigger

Notifica o n8n para iniciar o fluxo de sincronização. O n8n busca os dois catálogos, usa IA para detectar divergências e atualiza o marketplace automaticamente.

```bash
curl -X POST http://localhost:8081/api/sync/trigger
# HTTP 202 Accepted
```

O endpoint retorna `202 Accepted` imediatamente — o processamento continua de forma assíncrona no n8n.

---

## Modelo de dados

**Tabela:** `supplier_product`

| Coluna  | Tipo           | Descrição                       |
|---------|----------------|---------------------------------|
| `id`    | BIGINT (PK)    | Identificador auto-incrementado |
| `name`  | VARCHAR(255)   | Nome do produto                 |
| `price` | DECIMAL(10,2)  | Preço no catálogo do fornecedor |

**Seed (V2) — intencionalmente divergente do marketplace:**

| ID | Nome                 | Preço (fornecedor) | Preço (marketplace) |
|----|----------------------|--------------------|---------------------|
| 1  | Wireless Headphones  | R$ 99,90           | R$ 89,90            |
| 2  | USB-C Hub            | R$ 49,90           | R$ 45,00            |
| 3  | Mechanical Keyboard  | R$ 110,00          | R$ 120,00           |
| 4  | Webcam HD            | R$ 80,00           | R$ 75,50            |
| 5  | Mouse Pad XL         | R$ 25,00           | R$ 25,00            |

---

## Configuração

`src/main/resources/application.yml`

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seller_db
    username: ${DB_USER:demo}
    password: ${DB_PASSWORD:demo}
  flyway:
    enabled: true
    locations: classpath:db/migration

n8n:
  webhook-url: ${N8N_WEBHOOK_URL:http://localhost:5678/webhook/catalog-sync}
```

| Variável           | Padrão                                            | Descrição              |
|--------------------|---------------------------------------------------|------------------------|
| `DB_USER`          | `demo`                                            | Usuário do banco        |
| `DB_PASSWORD`      | `demo`                                            | Senha do banco          |
| `N8N_WEBHOOK_URL`  | `http://localhost:5678/webhook/catalog-sync`      | URL do webhook do n8n   |

---

## Como rodar

```bash
# Com MySQL e n8n rodando via docker compose
mvn spring-boot:run

# Apontando para outro n8n
N8N_WEBHOOK_URL=http://meu-n8n/webhook/catalog-sync mvn spring-boot:run
```

---

## Testes

O projeto tem três camadas de teste:

```
src/test/
└── java/com/demo/supplier/
    ├── service/
    │   └── SupplierServiceTest.java      # Unit — Mockito puro
    ├── controller/
    │   ├── SupplierControllerTest.java   # Slice — @WebMvcTest + MockMvc
    │   └── SupplierControllerIT.java     # Integração — @SpringBootTest + Testcontainers
    └── repository/
        └── SupplierRepositoryTest.java   # Slice — @DataJpaTest + H2
```

### Unit test (`SupplierServiceTest`)

Testa o `SupplierService` com `SupplierProductRepository` e `RestTemplate` mockados. Sem contexto Spring.

Cenários cobertos:
- `findAll` retorna lista mapeada corretamente
- `findAll` vazio retorna lista vazia
- `create` persiste e retorna DTO com ID
- `update` atualiza nome e preço corretamente
- `update` com ID inexistente lança `IllegalArgumentException`
- `triggerSync` chama a URL do webhook do n8n

### Controller slice (`SupplierControllerTest`)

Testa a camada HTTP com `MockMvc`. O `SupplierService` é mockado com `@MockBean`.

Cenários cobertos:
- `GET /api/products` → 200 com lista
- `POST /api/products` válido → 201
- `POST /api/products` sem `name` → 400
- `PUT /api/products/{id}` → 200 com produto atualizado
- `POST /api/sync/trigger` → 202 Accepted
- `POST /api/sync/trigger` com falha no n8n → exceção propagada

### Repository slice (`SupplierRepositoryTest`)

Testa o `SupplierProductRepository` com `@DataJpaTest` + H2 em memória.

Cenários cobertos:
- `save` persiste e gera ID
- `findById` retorna produto existente
- `findAll` retorna todos os salvos
- `save` atualiza produto existente
- `deleteById` remove o produto

### Integração (`SupplierControllerIT`)

Sobe o contexto completo com MySQL real via **Testcontainers**. O endpoint `/api/sync/trigger` aponta para um **MockServer** (também via Testcontainers) que simula o n8n, evitando dependência de serviços externos nos testes.

Cenários cobertos:
- `GET /api/products` retorna dados do seed do Flyway
- `POST /api/products` persiste no banco e retorna 201
- `PUT /api/products/{id}` atualiza no banco e retorna 200
- `POST /api/sync/trigger` retorna 202 (n8n simulado pelo MockServer)

### Executando

```bash
# Unit + slices (sem Docker, ~5s)
mvn test -Dtest="SupplierServiceTest,SupplierControllerTest,SupplierRepositoryTest"

# Integração (requer Docker, ~30s)
mvn test -Dtest="SupplierControllerIT"

# Todos
mvn test
```

---

## Estrutura de pacotes

```
com.demo.supplier/
├── SupplierApiApplication.java   # @Bean RestTemplate declarado aqui
├── controller/
│   └── SupplierController.java
├── service/
│   └── SupplierService.java
├── repository/
│   └── SupplierProductRepository.java
├── entity/
│   └── SupplierProduct.java
└── dto/
    └── SupplierProductDto.java
```

---

## Nota sobre o RestTemplate

O `RestTemplate` é declarado como `@Bean` em `SupplierApiApplication` e injetado no `SupplierService` via construtor. Isso permite que os testes substituam o bean por um mock, tornando o `triggerSync` completamente testável sem precisar de n8n rodando.
