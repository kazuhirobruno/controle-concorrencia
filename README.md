# Controle de Concorrência — Desafio Rinha de Devs 2024

Projeto em Java com foco em controle de concorrência transacional, autenticação JWT e resilência de dados sob carga simultânea. O objetivo é demonstrar uma API REST capaz de processar transações financeiras concorrentes, manter integridade de saldo e proteger endpoints com segurança baseada em token.

> Este código foi estruturado para o desafio como um serviço de controle transacional de clientes, com duas instâncias da API atrás de um proxy NGINX e PostgreSQL como fonte única de verdade.

---

## 🚀 Stack e arquitetura

- **Java 21**
- **Spring Boot 3.5**
- **Spring Data JPA** (Hibernate)
- **Spring Security** com **JWT**
- **PostgreSQL** como banco de dados relacional
- **Docker Compose** para orquestração local
- **NGINX** como proxy para múltiplas instâncias de API
- **Prometheus + Grafana** para observabilidade
- **Swagger / OpenAPI** para documentação de API
- **JUnit + Mockito** para testes automatizados

### Visão de arquitetura

- `UserController` e `UserAuthController`: cadastro, autenticação e gerenciamento do cliente
- `TransactionController`: criação de transações e consulta de extrato
- `SecurityClienteFilter`: valida JWT e popula `cliente_id` para autorizações
- `CreateTransactionUseCase` + `UserBalanceService`: lógica de transação com bloqueio pessimista de usuário
- `UserRepository`: busca de usuário com lock para evitar condições de corrida

---

## 📦 Composição do ambiente local

O `docker-compose.yml` orquestra os seguintes serviços:

- `postgres`: banco de dados PostgreSQL
- `pgadmin`: painel de administração do banco
- `api01` e `api02`: duas instâncias da mesma aplicação Java
- `nginx`: proxy reverso para as instâncias de API
- `prometheus`: coleta de métricas
- `grafana`: visualização de métricas

> O objetivo de rodar duas instâncias de API é simular concorrência real em nível de rede e aplicação, enquanto o PostgreSQL garante a consistência dos dados.

---

## 🔧 Como usar

### 1. Subir o ambiente Docker

```bash
docker-compose up -d --build
```

### 2. Compilar o projeto

```bash
./mvnw clean package
```

### 3. Executar localmente (opcional)

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em:

- `http://localhost:9999` via NGINX
- `http://localhost:8080` para acesso direto (quando rodando localmente)
- `http://localhost:9090` para Prometheus
- `http://localhost:3000` para Grafana
- `http://localhost:8082` para pgAdmin

---

## 🧩 Endpoints principais

A API expõe os seguintes endpoints sob `/clientes`:

- `POST /clientes/` — cadastrar cliente
- `POST /clientes/auth` — autenticar e receber JWT
- `PATCH /clientes/password` — alterar senha do cliente autenticado
- `DELETE /clientes/` — deletar cliente autenticado
- `POST /clientes/{id}/transacoes` — criar transação de crédito (`c`) ou débito (`d`)
- `GET /clientes/{id}/extrato` — consultar extrato e saldo do cliente

### Fluxo típico

1. `POST /clientes/` para criar o cliente
2. `POST /clientes/auth` para receber `accessToken`
3. Usar `Authorization: Bearer <token>` nos endpoints protegidos
4. `POST /clientes/{id}/transacoes` para aplicar transação
5. `GET /clientes/{id}/extrato` para consultar saldo e histórico

---

## 🧠 Comportamento de concorrência

A lógica de transação trabalha com:

- validação de propriedade do token para o `id` fornecido
- lock pessimista na leitura do usuário (`findWithLockById`)
- controle de limite de saldo negativo
- gravação consistente do novo saldo e da transação

Isso garante que várias requisições concorrentes para o mesmo usuário não deixem o saldo em estado inconsistente.

---

## 🧪 Testes

Execute a suíte de testes:

```bash
./mvnw test
```

Os testes cobrem:

- autenticação JWT
- cadastro de usuário
- alteração de senha
- criação de transações
- validação de limites e erros de negócio

---

## 📌 Observabilidade e documentação

- **Swagger UI:** `http://localhost:9999/swagger-ui/index.html`
- **Prometheus:** `http://localhost:9090`
- **Grafana:** `http://localhost:3000`
- **pgAdmin:** `http://localhost:8082`

---

## 📝 Uso do `client.rest`

O arquivo `client.rest` contém exemplos de requests para:

- criação de cliente
- autenticação
- troca de senha
- exclusão de cliente
- criação de transação
- consulta de extrato

Use a extensão REST Client no VS Code ou importe os payloads para Postman/Insomnia.

---

## 🔐 Configuração de segurança

A aplicação utiliza JWT assinado com HMAC256. O segredo está definido em `src/main/resources/application.properties`:

```properties
security.token.secret.client="CONCORRENCIA_TRANSACTION@123#"
```

Os tokens têm tempo de expiração curto e são obrigatórios para os endpoints de alteração de senha, exclusão de cliente, transações e extrato.

---

## 💡 Observações finais

Este projeto foca no controle de concorrência em ambiente transacional, validação de integridade de dados e segurança de API. Ele é ideal para demonstrar como isolar operações financeiras concorrentes em arquitetura Java/Spring Boot com PostgreSQL e containers.
