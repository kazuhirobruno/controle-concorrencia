# ==============================================================================
# Etapa 1: Compilação e empacotamento da aplicação
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Define o diretório de trabalho dentro do container de build
WORKDIR /app

# Copia o arquivo de configuração do Maven (pom.xml) para baixar as dependências
COPY pom.xml .

# Baixa as dependências em cache (evita baixar tudo de novo se o pom não mudar)
RUN mvn dependency:go-offline -B

# Copia o código-fonte da aplicação para o container
COPY src ./src

# Compila o projeto e gera o arquivo .jar ignorando os testes de unidade
RUN mvn clean package -DskipTests

# ==============================================================================
# Etapa 2: Ambiente de execução da aplicação
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

# Define o diretório onde a aplicação vai rodar
WORKDIR /app

# Copia o .jar gerado na Etapa 1 para esta imagem limpa
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta interna que o Spring Boot vai escutar
EXPOSE 8080

# Comando para iniciar a aplicação definindo parâmetros de otimização de memória
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
