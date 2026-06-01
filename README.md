# Catalog Sync Demo

Demo de sincronização automática de catálogo de produtos usando **n8n** e **IA (GPT-4o-mini)**.

O fluxo detecta divergências entre o catálogo de um fornecedor e o de um marketplace e aplica as atualizações automaticamente — sem intervenção humana.

---

## Visão geral

```
Supplier API  ──►  POST /api/sync/trigger
                         │
                         ▼
                   [n8n Webhook]
                         │
          ┌──────────────┴──────────────┐
          ▼                             ▼
  Fetch Marketplace              Fetch Supplier
     Products                      Products
          │                             │
     Wrap Marketplace            Wrap Supplier
          └──────────────┬──────────────┘
                         ▼
                  Merge Catalogs
                         ▼
               Aggregate Catalogs
                         ▼
           Catalog Sync AI Agent  ◄── GPT-4o-mini
                         ▼
                Parse AI Output
                         ▼
                   Has Updates?
                ┌─────────┴─────────┐
               YES                  NO
                ▼                   ▼
       Update Marketplace       No Changes
        PUT /api/products/{id}
```

---

## Estrutura do projeto

```
ai-catalog-sync-demo/
├── docker-compose.yml          # MySQL + n8n
├── init.sql                    # Cria os bancos e concede permissões
├── catalog-sync-workflow.json  # Workflow n8n para importar
├── marketplace-api/            # API do marketplace (porta 8080)
└── supplier-api/               # API do fornecedor (porta 8081)
```

---

## Pré-requisitos

- Docker e Docker Compose
- Java 17+
- Maven 3.8+
- Créditos na OpenAI (o modelo usado é `gpt-4o-mini` — ~U$5 dá para muitos testes)

---

## Como rodar

### 1. Suba MySQL e n8n

```bash
docker compose up -d
```

Isso inicializa:
- **MySQL 8.0** na porta `3306` com os bancos `marketplace_db` e `supplier_db`
- **n8n** na porta `5678` (usuário: `admin` / senha: `admin`)

### 2. Suba as APIs Java

Em terminais separados:

```bash
# Terminal 1 — Marketplace API
cd marketplace-api
mvn spring-boot:run

# Terminal 2 — Supplier API
cd supplier-api
mvn spring-boot:run
```

O Flyway cria as tabelas e insere os dados de exemplo automaticamente na primeira execução.

### 3. Importe o workflow no n8n

1. Acesse **http://localhost:5678** e crie sua conta
2. Clique em **⋯ → Import from file**
3. Selecione `catalog-sync-workflow.json`
4. No nó **OpenAI Chat Model**, adicione sua API Key
5. Ative o workflow com o toggle no canto superior direito

### 4. Teste o fluxo

```bash
# 1. Atualize um produto no fornecedor
curl -X PUT http://localhost:8081/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Headphones Pro","price":129.90}'

# 2. Fornecedor dispara o sync
curl -X POST http://localhost:8081/api/sync/trigger

# 3. Verifique o marketplace atualizado
curl http://localhost:8080/api/products
```

---

## Dados de exemplo

Os dois bancos são populados com 5 produtos intencionalmente com preços divergentes, para que o fluxo tenha sempre algo para sincronizar na primeira execução.

| ID | Produto              | Marketplace | Fornecedor |
|----|----------------------|-------------|------------|
| 1  | Wireless Headphones  | R$ 89,90    | R$ 99,90   |
| 2  | USB-C Hub            | R$ 45,00    | R$ 49,90   |
| 3  | Mechanical Keyboard  | R$ 120,00   | R$ 110,00  |
| 4  | Webcam HD            | R$ 75,50    | R$ 80,00   |
| 5  | Mouse Pad XL         | R$ 25,00    | R$ 25,00   |

---

## Variáveis de ambiente

| Variável         | Padrão                                          | Descrição                  |
|------------------|-------------------------------------------------|----------------------------|
| `DB_USER`        | `demo`                                          | Usuário do banco           |
| `DB_PASSWORD`    | `demo`                                          | Senha do banco             |
| `N8N_WEBHOOK_URL`| `http://localhost:5678/webhook/catalog-sync`    | URL do webhook do n8n      |

---

## APIs disponíveis

| Método | Endpoint                        | Serviço          | Descrição                        |
|--------|---------------------------------|------------------|----------------------------------|
| GET    | `/api/products`                 | marketplace:8080 | Lista todos os produtos          |
| POST   | `/api/products`                 | marketplace:8080 | Cria um produto                  |
| PUT    | `/api/products/{id}`            | marketplace:8080 | Atualiza um produto              |
| GET    | `/api/products`                 | supplier:8081    | Lista produtos do fornecedor     |
| POST   | `/api/products`                 | supplier:8081    | Cria produto no fornecedor       |
| PUT    | `/api/products/{id}`            | supplier:8081    | Atualiza produto no fornecedor   |
| POST   | `/api/sync/trigger`             | supplier:8081    | Dispara a sincronização via n8n  |

---

## Testes

Cada API tem três camadas de testes independentes:

```bash
# Unit tests e slices (rápido, sem Docker)
mvn test -pl marketplace-api -Dtest="ProductServiceTest,ProductControllerTest,ProductRepositoryTest"
mvn test -pl supplier-api   -Dtest="SupplierServiceTest,SupplierControllerTest,SupplierRepositoryTest"

# Integração com MySQL real via Testcontainers (requer Docker)
mvn test -pl marketplace-api -Dtest="ProductControllerIT"
mvn test -pl supplier-api   -Dtest="SupplierControllerIT"

# Todos os testes
mvn test -pl marketplace-api
mvn test -pl supplier-api
```

---

## Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Data JPA** + **Flyway** + **MySQL 8**
- **n8n** (self-hosted)
- **OpenAI GPT-4o-mini**
- **Testcontainers** (testes de integração)
- **Docker Compose**
