# Sistema de Controle de Concorrência 🏢

Este repositório contém o microsserviço corporativo focado estritamente no estudo, validação e implementação de mecanismos avançados de **Controle de Concorrência**. A aplicação foi projetada para garantir a consistência de dados, isolamento de transações simultâneas de alta criticidade e mitigação de condições de corrida (_race conditions_).

O projeto serve como um ambiente controlado e resiliente para simulação de cargas extremas em endpoints sensíveis do ecossistema.

> ℹ️ **Nota de Escopo:** Aplicação desenvolvida originalmente para fins de estudo e validação arquitetural.

---

## 🚀 Arquitetura e Stack Tecnológica

O ecossistema do projeto foi desenhado sob os padrões de mercado para sistemas de alta disponibilidade e performance:

- **Linguagem:** Java 21+ (LTS)
- **Framework Core:** Spring Boot 3.x
- **Segurança e Autenticação:** JWT (JSON Web Tokens)
- **Banco de Dados:** PostgreSQL (Persistência relacional e controle de locks de tabelas/linhas)
- **Containerização:** Docker e Docker Compose (Orquestração de ambiente local)
- **Observabilidade:** Prometheus & Grafana (Coleta de métricas e dashboards analíticos de tráfego)
- **Documentação:** Swagger / OpenAPI
- **Qualidade de Código:** SonarQube

---

## 📂 Estrutura do Projeto

```text
├── src/
│   ├── main/
│   │   ├── java/            # Camadas de Controller, Service, Security (JWT), Domain e Repository
│   │   └── resources/       # Configurações de ambiente (application.yml, migrations, etc.)
│   └── test/                # Suíte de testes automatizados (Unitários e de Integração)
├── client.rest              # Caderno de testes funcionais (Simulação de chamadas HTTP concorrentes)
├── docker-compose.yml       # Infraestrutura local (PostgreSQL, SonarQube, Prometheus, Grafana)
└── pom.xml                  # Manifesto de dependências do Apache Maven
```

---

## 🛠️ Configuração do Ambiente de Desenvolvimento

### Pré-requisitos Obrigatórios

1. **Java JDK 21** ou superior instalado localmente.
2. **Docker & Docker Compose** ativos na máquina.

### Passo 1: Inicialização da Infraestrutura (Containers)

Para subir o banco de dados PostgreSQL, a esteira do SonarQube e as ferramentas de observabilidade (Grafana/Prometheus), execute:

```bash
docker-compose up -d --build
```

### Passo 2: Compilação e Build do Artefato

Garantindo a resolução de dependências e execução das validações de build do Maven:

```bash
./mvnw clean package
```

### Passo 3: Execução da Aplicação

Para inicializar o serviço Spring Boot localmente:

```bash
./mvnw spring-boot:run
```

_A API estará disponível para consumo em `http://localhost:8080` (ou na porta configurada no seu `application.yml`)._

---

## 🧪 Estratégia de Testes e Validação de Concorrência

O projeto adota uma abordagem rigorosa para analisar o comportamento do sistema sob estresse transacional.

### Testes Automatizados (Garantia de Qualidade)

A cobertura de código utiliza as ferramentas padrão de mercado para simulação de comportamento de componentes e isolamento de regras de negócio:

```bash
./mvnw test
```

- **JUnit**: Execução e estruturação dos cenários de teste transacionais.
- **Mockito**: Criação de mocks para simulação das camadas de persistência e integrações externas.

### Disparos Simultâneos de Carga Manual

O arquivo `client.rest` na raiz do projeto está estruturado com requisições HTTP pré-configuradas para validar os mecanismos de travamento (_locking_).

1. Utilize a extensão **REST Client** (no VS Code) ou importe os payloads na sua ferramenta de preferência (Postman/Insomnia).
2. Execute os fluxos de teste para gerar tokens **JWT** válidos e disparar requisições concorrentes imediatas no servidor, analisando as respostas e o comportamento do banco de dados.

---

## 📊 Governança, Observabilidade e Documentação

### Análise Estática de Código (Quality Gate)

Para submeter o código e rodar a análise de qualidade do SonarQube localmente, valide se o container está ativo e execute:

```bash
./mvnw clean verify sonar:sonar
```

### Observabilidade em Tempo Real

Métricas de telemetria da JVM e volumetria de concorrência HTTP podem ser monitoradas pelas ferramentas expostas nos containers:

- **Prometheus:** Coleta e armazenamento de métricas de performance e infraestrutura.
- **Grafana:** Visualização de dashboards analíticos (saúde da aplicação e bottlenecks de requisições).

### Documentação da API

Para consultar formalmente os contratos, payloads e esquemas de dados expostos pela aplicação, acesse a interface interativa do **Swagger UI** com a aplicação rodando:

- URL padrão: `http://localhost:8080/swagger-ui/index.html`

---
