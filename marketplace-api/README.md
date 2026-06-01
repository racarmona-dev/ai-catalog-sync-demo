# marketplace-api

API REST do marketplace — expõe o catálogo de produtos e recebe atualizações disparadas pelo fluxo de sincronização do n8n.

- **Porta:** `8080`
- **Banco:** `marketplace_db` (MySQL 8)
- **Framework:** Spring Boot 3.2 + Spring Data JPA + Flyway

---

## Endpoints

| Método | Endpoint             | Status | Descrição                  |
|--------|----------------------|--------|----------------------------|
| GET    | `/api/products`      | 200    | Lista todos os produtos     |
| POST   | `/api/products`      | 201    | Cria um novo produto        |
| PUT    | `/api/products/{id}` | 200    | Atualiza nome e/ou preço    |

### GET /api/products

```bash
curl http://localhost:8080/api/products
```

```json
[
  { "id": 1, "name": "Wireless Headphones", "price": 89.90, "enabled": true },
  { "id": 2, "name": "USB-C Hub",           "price": 45.00, "enabled": true }
]
```

### POST /api/products

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Novo Produto","price":59.90}'
```

Campos obrigatórios: `name` (não vazio), `price` (maior que zero).

### PUT /api/products/{id}

Usado pelo n8n para aplicar as atualizações detectadas pela IA.

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Headphones Pro","price":129.90}'
```

---

## Modelo de dados

**Tabela:** `product`

| Coluna       | Tipo             | Descrição                          |
|--------------|------------------|------------------------------------|
| `id`         | BIGINT (PK)      | Identificador auto-incrementado    |
| `name`       | VARCHAR(255)     | Nome do produto                    |
| `price`      | DECIMAL(10,2)    | Preço atual                        |
| `enabled`    | BOOLEAN          | Produto ativo (`true` por padrão)  |
| `updated_at` | DATETIME         | Atualizado via `@PreUpdate`        |

**Seed (V2):**

| ID | Nome                 | Preço   |
|----|----------------------|---------|
| 1  | Wireless Headphones  | R$ 89,90 |
| 2  | USB-C Hub            | R$ 45,00 |
| 3  | Mechanical Keyboard  | R$ 120,00|
| 4  | Webcam HD            | R$ 75,50 |
| 5  | Mouse Pad XL         | R$ 25,00 |

---

## Configuração

`src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/marketplace_db
    username: ${DB_USER:demo}
    password: ${DB_PASSWORD:demo}
  flyway:
    enabled: true
    locations: classpath:db/migration
```

| Variável      | Padrão | Descrição       |
|---------------|--------|-----------------|
| `DB_USER`     | `demo` | Usuário do banco |
| `DB_PASSWORD` | `demo` | Senha do banco   |

---

## Como rodar

```bash
# Com MySQL já rodando via docker compose
mvn spring-boot:run

# Ou apontando para outro banco
DB_USER=myuser DB_PASSWORD=mypass mvn spring-boot:run
```

---

## Testes

O projeto tem três camadas de teste:

```
src/test/
└── java/com/demo/marketplace/
    ├── service/
    │   └── ProductServiceTest.java      # Unit — Mockito puro
    ├── controller/
    │   ├── ProductControllerTest.java   # Slice — @WebMvcTest + MockMvc
    │   └── ProductControllerIT.java     # Integração — @SpringBootTest + Testcontainers
    └── repository/
        └── ProductRepositoryTest.java   # Slice — @DataJpaTest + H2
```

### Unit test (`ProductServiceTest`)

Testa a lógica do `ProductService` com o `ProductRepository` mockado. Sem contexto Spring, executa em milissegundos.

Cenários cobertos:
- `findAll` retorna lista mapeada corretamente
- `findAll` com banco vazio retorna lista vazia
- `create` persiste e retorna DTO com ID
- `create` com `enabled=false` persiste o campo
- `update` atualiza nome e preço corretamente
- `update` com ID inexistente lança `IllegalArgumentException`

### Controller slice (`ProductControllerTest`)

Testa a camada HTTP com `MockMvc`. O `ProductService` é mockado com `@MockBean` — sem banco, sem contexto completo.

Cenários cobertos:
- `GET /api/products` → 200 com lista
- `GET /api/products` vazio → 200 com array vazio
- `POST /api/products` válido → 201 com produto criado
- `POST /api/products` sem `name` → 400
- `POST /api/products` com preço zero → 400
- `PUT /api/products/{id}` → 200 com produto atualizado

### Repository slice (`ProductRepositoryTest`)

Testa o `ProductRepository` com `@DataJpaTest` + H2 em memória. Flyway desabilitado; Hibernate cria o schema via `ddl-auto: create-drop`.

Cenários cobertos:
- `save` persiste e gera ID
- `findById` retorna produto existente
- `findAll` retorna todos os produtos salvos
- `save` atualiza produto existente
- `deleteById` remove o produto

### Integração (`ProductControllerIT`)

Sobe o contexto completo do Spring Boot com MySQL real via **Testcontainers**. Testa o fluxo de ponta a ponta: HTTP → Controller → Service → Repository → banco.

Cenários cobertos:
- `GET /api/products` retorna dados do seed do Flyway
- `POST /api/products` persiste no banco e retorna 201
- `PUT /api/products/{id}` atualiza no banco e retorna 200
- `PUT /api/products/99999` (inexistente) retorna 5xx
- `POST /api/products` inválido retorna 400

### Executando

```bash
# Unit + slices (sem Docker, ~5s)
mvn test -Dtest="ProductServiceTest,ProductControllerTest,ProductRepositoryTest"

# Integração (requer Docker, ~30s)
mvn test -Dtest="ProductControllerIT"

# Todos
mvn test
```

---

## Estrutura de pacotes

```
com.demo.marketplace/
├── MarketplaceApiApplication.java
├── controller/
│   └── ProductController.java
├── service/
│   └── ProductService.java
├── repository/
│   └── ProductRepository.java
├── entity/
│   └── Product.java
└── dto/
    └── ProductDto.java
```
