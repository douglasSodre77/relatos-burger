# Relatos Burger — API

API REST do site da hamburgueria **Relatos Burger**.

Backend em **Java 21 + Spring Boot**, com persistência em **MySQL 8**, migrações via **Flyway**, segurança com **Spring Security** e mapeamento JPA/Hibernate.

> **Estado atual (`main`):** esqueleto do backend. A aplicação sobe, conecta no banco e executa o Flyway, mas ainda não expõe endpoints de negócio. A implementação da API de usuários está na branch `feature/core-usuario-base`.

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
| Segurança     | Spring Security                              |
| Validação     | Bean Validation                              |
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
        │   │   ├── status/StatusController.java              # GET /api/status
        │   │   └── shared/security/SecurityConfig.java       # segurança do profile dev
        │   └── resources/
        │       ├── application.yaml        # configuração base (porta 8081, profile dev)
        │       └── application-dev.yaml    # datasource, JWT e CORS
        └── test/
            └── java/com/relatosburger/api/
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

A aplicação usa as seguintes variáveis (lidas pelo `application-dev.yaml`):

| Variável       | Obrigatória | Padrão                                                   | Descrição                          |
| -------------- | ----------- | -------------------------------------------------------- | ---------------------------------- |
| `DB_USER`      | Não         | `relatos`                                                | Usuário do MySQL                   |
| `DB_PASSWORD`  | **Sim**     | —                                                        | Senha do usuário do MySQL          |
| `JWT_SECRET`   | Não         | `troque-esta-chave-dev-com-no-minimo-32-caracteres`      | Chave HMAC para assinatura do JWT  |
| `SERVER_PORT`  | Não         | `8081`                                                   | Porta HTTP da API                  |

> Em produção, sempre defina `DB_PASSWORD` e um `JWT_SECRET` forte (mínimo 32 caracteres).

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

---

## Como rodar

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

A API sobe em **http://localhost:8081**.

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

Teste o endpoint de status:

```bash
curl http://localhost:8081/api/status
```

Resposta esperada:

```json
{"status":"ok","app":"relatos-burger-api"}
```

> No profile `dev` a segurança está liberada (`SecurityConfig` com `permitAll`), então não há tela de login. A raiz `/` retorna **404** porque ainda não há endpoint mapeado nela — use `/api/status` para validar que a API está no ar.

### Via IntelliJ IDEA

1. Abra a pasta raiz do repositório (`relatos-burger/`).
2. Em **Project Structure → Project SDK**, selecione o **JDK 21**.
3. Reimporte o Maven (ícone do Maven → *Reload All Maven Projects*).
4. Garanta `DB_PASSWORD` nas variáveis de execução (*Run/Debug Configurations → Environment variables*).
5. Execute `RelatosBurgerApplication`.

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

> Os testes usam `@SpringBootTest` e carregam o contexto completo, portanto **o MySQL precisa estar acessível** para `./mvnw test` funcionar.

---

## Configuração e perfis

- O profile ativo é definido em `application.yaml` (`spring.profiles.active: dev`).
- `application-dev.yaml` contém datasource, JWT e CORS.
- `spring.jpa.hibernate.ddl-auto: validate`: o Hibernate **não cria tabelas**; o schema é responsabilidade do Flyway.
- Migrações ficam em `src/main/resources/db/migration` (presentes a partir da branch `feature/core-usuario-base`).
- CORS liberado para `http://localhost:3000` por padrão.
- No profile `dev`, `SecurityConfig` libera todas as requisições (`permitAll`) para facilitar o desenvolvimento. Remova ou ajuste essa configuração antes de ir para produção.
- `GET /api/status` retorna `{"status":"ok","app":"relatos-burger-api"}` e serve como health check simples.

---

## Solução de problemas

| Sintoma | Causa provável | Solução |
| ------- | -------------- | ------- |
| `The JAVA_HOME environment variable is not defined correctly` | `JAVA_HOME` ausente ou apontando para caminho inválido/JRE | Aponte para o JDK 21 e reabra o terminal |
| `Communications link failure` / `Connection refused` | MySQL não está rodando na porta 3306 | `docker start relatos-mysql` ou inicie o MySQL local |
| `Access denied for user 'relatos'` | `DB_PASSWORD` ausente/incorreta | Defina `DB_PASSWORD` corretamente |
| `Port 8081 was already in use` | Porta ocupada | Use `SERVER_PORT`/`--server.port` com outra porta |
| `No migrations found` (WARN) | `main` ainda não possui migrações | Esperado no esqueleto; ignore ou use a branch de feature |
| `HTTP 404` em `/` | Não há endpoint mapeado na raiz | Use `GET /api/status` para validar a API |
| Tela de login do Spring Security | `dev` sem `SecurityConfig` (ex.: profile diferente) | Rode com o profile `dev` (padrão) ou use `user` + senha gerada no log |

---

## Branches

- `main`: esqueleto do backend (aplicação sobe, sem endpoints de negócio).
- `feature/core-usuario-base`: modelo de usuário, repositório, tratamento de erros e migração `V1__criar_usuarios.sql`.
