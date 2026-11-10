# Relatos Burger — API

API REST de consulta do site da hamburgueria **Relatos Burger**.

O backend expõe, em modo **somente leitura**, os dados do cardápio (categorias e produtos) e as informações institucionais. A pilha é **Java 21 + Spring Boot 4.1.2**, com **MySQL 8**, migrações **Flyway**, **Spring Data JPA/Hibernate** e **Spring Security 7**.

> **Estado atual (`main`):** API de consulta implementada, com perfis `dev` (HTTP) e `tls` (HTTPS), tratamento padronizado de erros, log de requisições e testes automatizados.

---

## Stack

| Camada        | Tecnologia                                   |
| ------------- | -------------------------------------------- |
| Linguagem     | Java 21                                      |
| Framework     | Spring Boot 4.1.2                            |
| Build         | Maven (via Maven Wrapper)                    |
| Banco         | MySQL 8                                      |
| Migrações     | Flyway (`flyway-mysql`)                      |
| Persistência  | Spring Data JPA / Hibernate                  |
| Segurança     | Spring Security 7                            |
| Produtividade | Lombok, Spring Boot DevTools                 |

## Estrutura do repositório

```
relatos-burger/
└── backend/
    ├── mvnw / mvnw.cmd            # Maven Wrapper (não precisa instalar Maven)
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/relatosburger/api/
        │   │   ├── RelatosBurgerApplication.java
        │   │   ├── status/StatusController.java
        │   │   ├── cardapio/
        │   │   │   ├── domain/            # Categoria, Produto
        │   │   │   ├── dto/               # CategoriaResponse, ProdutoResponse
        │   │   │   ├── repository/        # CategoriaRepository, ProdutoRepository
        │   │   │   └── web/               # CardapioController
        │   │   ├── institucional/
        │   │   │   ├── domain/            # InformacaoInstitucional
        │   │   │   ├── dto/               # InstitucionalResponse
        │   │   │   ├── repository/        # InformacaoInstitucionalRepository
        │   │   │   └── web/               # InstitucionalController
        │   │   └── shared/
        │   │       ├── exception/         # ErroResponse, GlobalExceptionHandler
        │   │       ├── log/               # RequestLoggingFilter
        │   │       └── security/          # SecurityConfig, SecurityConfigTls, CorsConfig
        │   └── resources/
        │       ├── application.yaml       # configuração base (porta 8081, profile dev)
        │       ├── application-dev.yaml   # CORS HTTP
        │       ├── application-tls.yaml   # HTTPS, CORS HTTPS
        │       └── db/migration/          # V1 (schema) e V2 (seed fictício)
        └── test/java/com/relatosburger/api/
            ├── cardapio/CardapioControllerTest.java
            ├── institucional/ApiConsultaTest.java
            ├── shared/security/TlsSecurityTest.java
            └── RelatosBurgerApplicationTests.java
```

> O `frontend/` (Flutter Web) e o `docs/` ainda não estão versionados neste repositório.

---

## Pré-requisitos

- **JDK 21** instalado (`java -version` deve reportar 21).
- **`JAVA_HOME`** apontando para o diretório do **JDK** (não apenas o JRE). O Maven Wrapper depende dessa variável.
- **Docker** (recomendado, para o MySQL) **ou** um **MySQL 8** local.
- Não é necessário instalar Maven: use o wrapper incluído (`mvnw` / `mvnw.cmd`).

Verificações rápidas:

```powershell
java -version
$env:JAVA_HOME
docker --version
```

```bash
java -version
echo $JAVA_HOME
docker --version
```

---

## Variáveis de ambiente

| Variável                | Perfil | Obrigatória | Padrão                    | Descrição                                      |
| ----------------------- | ------ | ----------- | ------------------------- | ---------------------------------------------- |
| `DB_USER`               | todos  | Não         | `relatos`                 | Usuário do MySQL                               |
| `DB_PASSWORD`           | todos  | **Sim**     | —                         | Senha do usuário do MySQL                      |
| `CORS_ORIGENS`          | dev    | Não         | `http://localhost:3000`   | Origens permitidas (separadas por vírgula)     |
| `CORS_ORIGENS`          | tls    | Não         | `https://localhost:3000`  | Origens permitidas (separadas por vírgula)     |
| `SSL_KEYSTORE_PATH`     | tls    | **Sim**     | —                         | Caminho do keystore PKCS12 (ex.: `relatos-burger.p12`) |
| `SSL_KEYSTORE_PASSWORD` | tls    | **Sim**     | —                         | Senha do keystore                              |
| `SERVER_PORT`           | todos  | Não         | `8081` (dev) / `8443` (tls) | Porta da API (pode ser sobrescrita)         |

> O segredo do keystore **não** deve ser versionado. O `.gitignore` já ignora `*.p12` e `*.jks`.

---

## Banco de dados (Docker)

O jeito mais simples é subir um MySQL 8 em container já com o banco e o usuário criados:

```bash
docker run --name relatos-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=relatos_burger \
  -e MYSQL_USER=relatos \
  -e MYSQL_PASSWORD=relatos \
  -v relatos_mysql_data:/var/lib/mysql \
  -p 3306:3306 \
  -d mysql:8
```

No PowerShell, use crase (`` ` ``) para quebrar linhas:

```powershell
docker run --name relatos-mysql `
  -e MYSQL_ROOT_PASSWORD=root `
  -e MYSQL_DATABASE=relatos_burger `
  -e MYSQL_USER=relatos `
  -e MYSQL_PASSWORD=relatos `
  -v relatos_mysql_data:/var/lib/mysql `
  -p 3306:3306 `
  -d mysql:8
```

Aguarde o banco ficar pronto:

```bash
docker exec relatos-mysql mysqladmin ping -h 127.0.0.1 -u root -proot
```

Se o container já existir, apenas inicie:

```bash
docker start relatos-mysql
```

Conexão usada pela aplicação:

```
jdbc:mysql://localhost:3306/relatos_burger?serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true&useSSL=false
```

### MySQL local (alternativa ao Docker)

Crie o banco e o usuário manualmente:

```sql
CREATE DATABASE relatos_burger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'relatos'@'localhost' IDENTIFIED BY 'relatos';
GRANT ALL PRIVILEGES ON relatos_burger.* TO 'relatos'@'localhost';
FLUSH PRIVILEGES;
```

### Migrações e dados de exemplo

- `V1__criar_tabelas_cardapio.sql`: cria as tabelas `categorias`, `produtos` e `informacoes_institucionais`.
- `V2__seed_cardapio.sql`: insere dados **fictícios** de exemplo (3 categorias, 10 produtos e 1 registro institucional).

> O seed é apenas para desenvolvimento/demonstração. Substitua os dados pelos reais antes de publicar. O schema é responsabilidade do Flyway (`spring.jpa.hibernate.ddl-auto: validate`).

---

## Endpoints

Todos os endpoints são **somente leitura** (`GET`). Não há `POST`, `PUT`, `PATCH` ou `DELETE` nesta versão.

| Método | Caminho                              | Descrição                                      |
| ------ | ------------------------------------ | ---------------------------------------------- |
| `GET`  | `/api/status`                        | Health check simples da API                    |
| `GET`  | `/api/cardapio/categorias`           | Lista as categorias do cardápio                |
| `GET`  | `/api/cardapio/produtos`             | Lista todos os produtos                        |
| `GET`  | `/api/cardapio/produtos?categoria=1` | Filtra produtos por categoria                  |
| `GET`  | `/api/cardapio/produtos/{id}`        | Detalha um produto (404 se não existir)        |
| `GET`  | `/api/institucional`                 | Nome, descrição, endereço, telefone e horário  |

Exemplos de resposta:

```json
// GET /api/status
{"status":"ok","app":"relatos-burger-api"}
```

```json
// GET /api/cardapio/produtos/1
{
  "id": 1,
  "nome": "X-Relatos",
  "descricao": "Pao brioche, blend artesanal 180g, queijo cheddar, alface, tomate e molho da casa.",
  "preco": 32.90,
  "categoriaId": 1,
  "categoriaNome": "Burgers",
  "disponivel": true
}
```

Erros são padronizados:

```json
// GET /api/cardapio/produtos/9999
{
  "status": 404,
  "mensagem": "Produto não encontrado",
  "timestamp": "2026-11-10T23:21:17.077366200Z"
}
```

> O campo `disponivel` indica se o produto está disponível no momento. Produtos indisponíveis continuam aparecendo na listagem (ex.: `Onion Rings`).

---

## Como rodar (profile `dev`, HTTP)

### 1. Clonar e entrar no projeto

```bash
git clone https://github.com/douglasSodre77/relatos-burger.git
cd relatos-burger/backend
```

### 2. Definir as variáveis de ambiente

PowerShell:

```powershell
$env:DB_USER="relatos"
$env:DB_PASSWORD="relatos"
```

Bash / macOS / Linux:

```bash
export DB_USER=relatos
export DB_PASSWORD=relatos
```

### 3. Subir a aplicação

Com o MySQL rodando, execute a partir de `backend/`:

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
./mvnw spring-boot:run
```

A API sobe em **http://localhost:8081** (o profile `dev` é o padrão em `application.yaml`).

Para usar outra porta, sem alterar arquivos:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"
```

Ou defina `SERVER_PORT=8080` no ambiente.

### 4. Verificar

No log devem aparecer linhas como:

```
Tomcat started on port 8081 (http)
Started RelatosBurgerApplication in ... seconds
```

Teste os endpoints:

```bash
curl http://localhost:8081/api/status
curl http://localhost:8081/api/cardapio/categorias
curl http://localhost:8081/api/cardapio/produtos
curl "http://localhost:8081/api/cardapio/produtos?categoria=1"
curl http://localhost:8081/api/cardapio/produtos/1
curl http://localhost:8081/api/institucional
```

> No profile `dev` a segurança está liberada (`SecurityConfig` com `permitAll`) e o CORS aceita `http://localhost:3000`. A raiz `/` retorna **404** porque não há endpoint mapeado nela — use `/api/status`.

### Via IntelliJ IDEA

1. Abra a pasta raiz do repositório (`relatos-burger/`).
2. Em **Project Structure → Project SDK**, selecione o **JDK 21**.
3. Reimporte o Maven (ícone do Maven → *Reload All Maven Projects*).
4. Garanta `DB_PASSWORD` nas variáveis de execução (*Run/Debug Configurations → Environment variables*).
5. Execute `RelatosBurgerApplication`.

---

## HTTPS/TLS (profile `tls`)

O requisito de segurança do PI é atendido pelo profile `tls`, que sobe a API apenas em **HTTPS (porta 8443)** com um certificado PKCS12.

### 1. Gerar o certificado autoassinado

A partir de `backend/`:

```bash
keytool -genkeypair -alias relatos-burger -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore relatos-burger.p12 -validity 365 \
  -storepass changeit \
  -dname "CN=localhost, OU=PI, O=Relatos Burger, L=Goiania, ST=GO, C=BR" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
```

No PowerShell (linha única):

```powershell
keytool -genkeypair -alias relatos-burger -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore relatos-burger.p12 -validity 365 -storepass changeit -dname "CN=localhost, OU=PI, O=Relatos Burger, L=Goiania, ST=GO, C=BR" -ext "SAN=dns:localhost,ip:127.0.0.1"
```

> O arquivo `relatos-burger.p12` é ignorado pelo Git. Nunca versione a chave privada.

### 2. Subir com o profile `tls`

PowerShell:

```powershell
$env:DB_USER="relatos"
$env:DB_PASSWORD="relatos"
$env:SSL_KEYSTORE_PATH="$PWD\relatos-burger.p12"
$env:SSL_KEYSTORE_PASSWORD="changeit"
.\mvnw.cmd "-Dspring-boot.run.profiles=tls" spring-boot:run
```

Bash / macOS / Linux:

```bash
export DB_USER=relatos
export DB_PASSWORD=relatos
export SSL_KEYSTORE_PATH="$PWD/relatos-burger.p12"
export SSL_KEYSTORE_PASSWORD=changeit
./mvnw -Dspring-boot.run.profiles=tls spring-boot:run
```

No log deve aparecer:

```
Tomcat started on port 8443 (https)
```

### 3. Testar HTTPS

Como o certificado é autoassinado, use `-k` (curl) ou aceite o certificado no navegador:

```bash
curl -k https://localhost:8443/api/status
curl -k https://localhost:8443/api/cardapio/produtos
curl -k "https://localhost:8443/api/cardapio/produtos?categoria=1"
curl -k https://localhost:8443/api/institucional
```

Inspecionar o certificado (se tiver OpenSSL):

```bash
openssl s_client -connect localhost:8443 -servername localhost
```

### Regras de segurança do profile `tls`

- Apenas `GET /api/**` é permitido; qualquer outra rota/método retorna **403**.
- Requisições não seguras são redirecionadas para HTTPS (`redirectToHttps`).
- CSRF está desabilitado porque a API é stateless e somente leitura.
- CORS liberado para `https://localhost:3000` por padrão (`CORS_ORIGENS`).
- No Spring Security 7, o antigo `requiresChannel().anyRequest().requiresSecure()` foi removido e substituído por `redirectToHttps()`, usado neste projeto como equivalente.

> O certificado autoassinado serve para desenvolvimento e demonstração. Em produção, use um certificado válido (CA) ou um proxy reverso (Nginx, por exemplo) terminando o TLS.

---

## Build e testes

```powershell
# Windows
.\mvnw.cmd clean package        # gera target/api-0.0.1-SNAPSHOT.jar
.\mvnw.cmd test                 # executa os testes
```

```bash
# Linux / macOS
./mvnw clean package
./mvnw test
```

Executar o JAR gerado:

```bash
java -jar target/api-0.0.1-SNAPSHOT.jar
```

### Testes automatizados

| Teste                          | Cobertura                                                        |
| ------------------------------ | ---------------------------------------------------------------- |
| `CardapioControllerTest`       | Listagem, filtro por categoria, detalhe e 404                    |
| `ApiConsultaTest`              | `/api/status` e `/api/institucional`                             |
| `TlsSecurityTest`              | Profile `tls`: GET autenticado por HTTPS e POST/DELETE com 403   |
| `RelatosBurgerApplicationTests`| Carregamento do contexto Spring                                  |

> Os testes usam `@SpringBootTest` e carregam o contexto completo, portanto **o MySQL precisa estar acessível** para `./mvnw test` funcionar. O `TlsSecurityTest` simula HTTPS no MockMvc e desabilita o listener SSL real apenas no teste.

---

## Configuração e perfis

- O profile ativo é definido em `application.yaml` (`spring.profiles.active: dev`).
- `application.yaml` (base): datasource, JPA, Flyway, porta `8081` e `server.error.include-stacktrace: never`.
- `application-dev.yaml`: CORS HTTP (`http://localhost:3000`).
- `application-tls.yaml`: porta `8443`, SSL PKCS12 (`key-alias: relatos-burger`) e CORS HTTPS.
- `spring.jpa.hibernate.ddl-auto: validate`: o Hibernate **não cria tabelas**; o schema é responsabilidade do Flyway.
- Migrações ficam em `src/main/resources/db/migration`.
- O `RequestLoggingFilter` registra método, URI, status, `secure` e duração de cada requisição.
- Erros 404 e 500 retornam JSON padronizado (`status`, `mensagem`, `timestamp`), sem stack trace na resposta.

---

## Solução de problemas

| Sintoma | Causa provável | Solução |
| ------- | -------------- | ------- |
| `The JAVA_HOME environment variable is not defined correctly` | `JAVA_HOME` ausente ou apontando para caminho inválido/JRE | Aponte para o JDK 21 e reabra o terminal |
| `Communications link failure` / `Connection refused` | MySQL não está rodando na porta 3306 | `docker start relatos-mysql` ou inicie o MySQL local |
| `Access denied for user 'relatos'` | `DB_PASSWORD` ausente/incorreta | Defina `DB_PASSWORD` corretamente |
| `Port 8081 was already in use` | Porta ocupada | Use `SERVER_PORT`/`--server.port` com outra porta |
| `Port 8443 was already in use` | Porta ocupada no profile `tls` | Encerre o processo anterior ou use outra porta |
| `Could not load keystore` / `keystore password was incorrect` | `SSL_KEYSTORE_PATH`/`SSL_KEYSTORE_PASSWORD` ausentes ou incorretos | Confira as variáveis e o caminho do `.p12` |
| `PKIX path building failed` ao usar `curl`/navegador | Certificado autoassinado não confiável | Use `curl -k` em desenvolvimento ou confie no certificado |
| `HTTP 403` em `POST`/`DELETE` no profile `tls` | Comportamento esperado: a API é somente leitura | Use apenas `GET` |
| `HTTP 404` em `/` | Não há endpoint mapeado na raiz | Use `GET /api/status` para validar a API |
| `No migrations found` (WARN) | Migrações ausentes no classpath | Verifique `src/main/resources/db/migration` |

---

## Branches

- `main`: API de consulta (cardápio + institucional), perfis `dev` e `tls`, testes e documentação.
- Trabalhos futuros (usuários, pedidos, autenticação) devem ser feitos em branches próprias a partir da `main`.
