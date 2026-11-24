# Relatos Burger — Tutorial de execução (servidor + cliente)

Este guia permite executar o projeto completo do início ao fim: uma API Spring Boot rodando em **HTTPS/TLS** e um **cliente Java de linha de comando** que consome a API.

```
Cliente (cliente/target/cliente.jar) --HTTPS/TLS--> Servidor Spring Boot (8443) --> MySQL
```

- **Servidor:** `backend/`
- **Cliente:** `cliente/`
- **Banco:** MySQL 8 (recomenda-se Docker)

Os comandos abaixo são apresentados primeiro para **PowerShell (Windows)** e depois em **Bash (Linux/macOS)** quando diferentes.

---

## 1. Pré-requisitos

- **JDK 21** instalado, com `JAVA_HOME` apontando para o diretório do JDK (não apenas do JRE).
- **Docker** (ou um MySQL 8 local).
- **Git** para clonar o repositório.
- Não é necessário instalar Maven: os dois projetos incluem o **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

Verificação no PowerShell:

```powershell
java -version
$env:JAVA_HOME
docker --version
```

Verificação no Bash:

```bash
java -version
echo "$JAVA_HOME"
docker --version
```

Abra um terminal na **raiz do repositório** (`relatos-burger/`) antes de começar.

---

## 2. Visão dos ambientes

Você terá três “ambientes” rodando ao mesmo tempo:

| Ambiente | Função | Onde executar |
| -------- | ------ | ------------- |
| MySQL | Banco de dados | Docker (background) |
| **Terminal A** | Servidor Spring Boot com TLS | Pasta `backend/` |
| **Terminal B** | Cliente Java | Pasta `cliente/` |

Recomenda-se deixar o Terminal A e o Terminal B lado a lado para acompanhar as requisições no log do servidor.

---

## 3. Passo 1 — Subir o MySQL

Se o container já existir, apenas inicie-o.

PowerShell:

```powershell
docker start relatos-mysql
```

Bash:

```bash
docker start relatos-mysql
```

Se o container **não existir**, crie-o:

PowerShell:

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

Bash:

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

Confira se o banco está pronto:

```powershell
docker exec relatos-mysql mysqladmin ping -h 127.0.0.1 -u root -proot
```

```bash
docker exec relatos-mysql mysqladmin ping -h 127.0.0.1 -u root -proot
```

Saída esperada: `mysqld is alive`.

Dados de acesso usados pela aplicação:

- Usuário: `relatos`
- Senha: `relatos`
- Banco: `relatos_burger`
- Porta: `3306`

As tabelas e os dados de exemplo são criados automaticamente pelo Flyway na primeira subida da API.

---

## 4. Passo 2 — Preparar o certificado TLS

O servidor usa um keystore PKCS12 em `backend/relatos-burger.p12`. Se ele já existir, pule a geração e vá direto para a exportação do `.pem`.

### 4.1 Gerar o keystore (somente se não existir)

PowerShell (linha única):

```powershell
keytool -genkeypair -alias relatos-burger -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore backend/relatos-burger.p12 -validity 365 -storepass changeit -dname "CN=localhost, OU=PI, O=Relatos Burger, L=Goiania, ST=GO, C=BR" -ext "SAN=dns:localhost,ip:127.0.0.1"
```

Bash:

```bash
keytool -genkeypair -alias relatos-burger -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore backend/relatos-burger.p12 -validity 365 \
  -storepass changeit \
  -dname "CN=localhost, OU=PI, O=Relatos Burger, L=Goiania, ST=GO, C=BR" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
```

### 4.2 Exportar o certificado público para o cliente

O cliente precisa do `.pem` para validar o certificado do servidor:

```powershell
keytool -exportcert -alias relatos-burger -keystore backend/relatos-burger.p12 -storepass changeit -rfc -file backend/relatos-burger.pem
```

```bash
keytool -exportcert -alias relatos-burger -keystore backend/relatos-burger.p12 -storepass changeit -rfc -file backend/relatos-burger.pem
```

Saída esperada: `Certificado armazenado no arquivo <relatos-burger.pem>`.

> O `.pem` contém apenas o **certificado público**. A chave privada fica somente no `.p12`. O `.gitignore` já ignora `*.p12`, `*.jks`, `*.pem` e `cliente/target/`, portanto nenhum desses arquivos aparece no `git status`.

---

## 5. Passo 3 — Compilar os projetos

Faça isso uma vez, antes de rodar. A primeira execução pode demorar um pouco mais porque o Maven baixa as dependências.

PowerShell:

```powershell
cd backend
.\mvnw.cmd clean package
cd ..\cliente
.\mvnw.cmd clean package
```

Bash:

```bash
cd backend
./mvnw clean package
cd ../cliente
./mvnw clean package
```

Ao final, confirme que o JAR do cliente existe:

```powershell
Get-Item target\cliente.jar
```

```bash
ls -lh target/cliente.jar
```

Volte para a raiz antes de abrir os terminais:

```powershell
cd ..
```

```bash
cd ..
```

---

## 6. Passo 4 — Iniciar o servidor (Terminal A)

Abra o **Terminal A**, vá até `backend/` e defina as variáveis de ambiente.

PowerShell:

```powershell
cd backend
$env:DB_USER="relatos"
$env:DB_PASSWORD="relatos"
$env:SSL_KEYSTORE_PATH="$PWD\relatos-burger.p12"
$env:SSL_KEYSTORE_PASSWORD="changeit"
.\mvnw.cmd "-Dspring-boot.run.profiles=tls" spring-boot:run
```

Bash:

```bash
cd backend
export DB_USER=relatos
export DB_PASSWORD=relatos
export SSL_KEYSTORE_PATH="$PWD/relatos-burger.p12"
export SSL_KEYSTORE_PASSWORD=changeit
./mvnw -Dspring-boot.run.profiles=tls spring-boot:run
```

Espere até aparecer no log:

```text
Tomcat started on port 8443 (https)
Started RelatosBurgerApplication
```

O servidor estará disponível em `https://localhost:8443`.

---

## 7. Passo 5 — Iniciar o cliente (Terminal B)

Abra o **Terminal B**, vá até `cliente/` e execute o JAR informando o certificado público:

PowerShell:

```powershell
cd cliente
java -jar target/cliente.jar --cafile ../backend/relatos-burger.pem
```

Bash:

```bash
cd cliente
java -jar target/cliente.jar --cafile ../backend/relatos-burger.pem
```

Você verá:

```text
Servidor: https://localhost:8443

=== Relatos Burger - Consulta ===
1. Listar categorias
2. Listar produtos (todos)
3. Listar produtos por categoria
4. Detalhar produto
5. Informações da hamburgueria
6. Status da API
0. Sair
Escolha uma opção:
```

### Opções do cliente

| Opção | Descrição |
| ----- | --------- |
| `--host` | Host do servidor (padrão: `localhost`) |
| `--porta` | Porta HTTPS (padrão: `8443`) |
| `--cafile` | Caminho do certificado `.pem` usado para validar o TLS |
| `--inseguro` | Desativa a validação do certificado (apenas para testes locais) |

Forma alternativa, **apenas para teste local**:

```powershell
java -jar target/cliente.jar --inseguro
```

```bash
java -jar target/cliente.jar --inseguro
```

Nesse modo, o cliente exibe um aviso em destaque informando que a validação do certificado foi desativada. Prefira sempre `--cafile`.

---

## 8. Passo 6 — Testar pelo menu

Após **cada requisição**, o cliente imprime a linha TLS negociada, por exemplo:

```text
[conexão segura] TLSv1.3 | TLS_AES_256_GCM_SHA384
```

Ao mesmo tempo, no Terminal A aparece o log da requisição com `secure=true`.

| Opção | Entrada | Resultado esperado no cliente | Log no servidor |
| ----- | ------- | ----------------------------- | --------------- |
| `1` | — | 3 categorias: Burgers, Acompanhamentos, Bebidas | `GET /api/cardapio/categorias -> 200 secure=true` |
| `2` | — | 10 produtos, preços em `R$ ...` e status `Disponível`/`Indisponível` | `GET /api/cardapio/produtos -> 200 secure=true` |
| `3` | `1` | 4 produtos da categoria Burgers | `GET /api/cardapio/produtos?categoria=1 -> 200 secure=true` |
| `4` | `1` | Detalhes do X-Relatos | `GET /api/cardapio/produtos/1 -> 200 secure=true` |
| `4` | `9999` | `[erro] Produto não encontrado` | `GET /api/cardapio/produtos/9999 -> 404 secure=true` |
| `5` | — | Nome, descrição, endereço, telefone e horário | `GET /api/institucional -> 200 secure=true` |
| `6` | — | `Status: ok` e `Aplicação: relatos-burger-api` | `GET /api/status -> 200 secure=true` |
| `9` | — | `[aviso] Opção inválida. Digite um número de 0 a 6.` | — |
| `4` | `abc` | `[erro] Informe um número inteiro válido.` | — |
| `0` | — | `Até logo!` e o cliente encerra | — |

Sequência sugerida para digitar no cliente:

```text
1
2
3
1
4
1
4
9999
5
6
9
4
abc
0
```

---

## 9. Passo 7 — Verificação rápida com `curl` (opcional)

Como o certificado é autoassinado, use `-k` nos testes diretos com `curl`.

PowerShell (use `curl.exe` para não cair no alias do PowerShell):

```powershell
curl.exe -k https://localhost:8443/api/status
curl.exe -k https://localhost:8443/api/cardapio/categorias
curl.exe -k https://localhost:8443/api/cardapio/produtos
```

Bash:

```bash
curl -k https://localhost:8443/api/status
curl -k https://localhost:8443/api/cardapio/categorias
curl -k https://localhost:8443/api/cardapio/produtos
```

---

## 10. Passo 8 — Executar os testes automatizados (opcional)

Com o MySQL no ar, execute no Terminal A (ou em um terceiro terminal), a partir da pasta `backend/`:

PowerShell:

```powershell
cd backend
.\mvnw.cmd test
```

Bash:

```bash
cd backend
./mvnw test
```

Resultado esperado:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 11. Passo 9 — Encerrar

1. No **cliente**, digite `0` e pressione `Enter`.
2. No **servidor**, pressione `Ctrl+C`.
3. Opcionalmente, pare o MySQL:
   ```powershell
   docker stop relatos-mysql
   ```
   ```bash
   docker stop relatos-mysql
   ```

Para rodar novamente, comece pelo **Passo 1**. O keystore, o `.pem` e o JAR do cliente já estarão prontos.

---

## 12. Solução de problemas

| Sintoma | Causa provável | Solução |
| ------- | -------------- | ------- |
| `The JAVA_HOME environment variable is not defined correctly` | `JAVA_HOME` ausente ou apontando para JRE | Aponte `JAVA_HOME` para o JDK 21 e reabra o terminal |
| `Communications link failure` / `Connection refused` no servidor | MySQL não está rodando | `docker start relatos-mysql` |
| `Access denied for user 'relatos'` | `DB_PASSWORD` incorreta ou ausente | Defina `$env:DB_PASSWORD="relatos"` (ou `export DB_PASSWORD=relatos`) |
| `Could not load keystore` / `keystore password was incorrect` | Caminho ou senha do `.p12` incorretos | Confira `SSL_KEYSTORE_PATH` e `SSL_KEYSTORE_PASSWORD`; use senha `changeit` |
| `Port 8443 was already in use` | Outro processo ocupando a porta | Encerre o processo anterior (comandos abaixo) |
| `Não foi possível validar o certificado TLS` no cliente | Cliente não confia no certificado autoassinado | Rode com `--cafile ../backend/relatos-burger.pem` |
| Acentos aparecem trocados no terminal | Codificação do console | Rode com `java "-Dstdout.encoding=UTF-8" -jar target/cliente.jar --cafile ../backend/relatos-burger.pem` |

Para liberar a porta 8443:

PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8443 -State Listen | Select-Object OwningProcess
Stop-Process -Id <PID> -Force
```

Bash (Linux/macOS):

```bash
lsof -i :8443
kill -9 <PID>
```

---

## Apêndice A — Sequência completa em Bash / macOS / Linux

```bash
# Raiz do repositório
docker start relatos-mysql

# Certificado (pule a primeira linha se o .p12 já existir)
keytool -genkeypair -alias relatos-burger -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore backend/relatos-burger.p12 -validity 365 \
  -storepass changeit \
  -dname "CN=localhost, OU=PI, O=Relatos Burger, L=Goiania, ST=GO, C=BR" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
keytool -exportcert -alias relatos-burger -keystore backend/relatos-burger.p12 \
  -storepass changeit -rfc -file backend/relatos-burger.pem

# Build
(cd backend && ./mvnw clean package)
(cd cliente && ./mvnw clean package)

# Terminal A — servidor
cd backend
export DB_USER=relatos
export DB_PASSWORD=relatos
export SSL_KEYSTORE_PATH="$PWD/relatos-burger.p12"
export SSL_KEYSTORE_PASSWORD=changeit
./mvnw -Dspring-boot.run.profiles=tls spring-boot:run

# Terminal B — cliente (em outro terminal, a partir da raiz)
cd cliente
java -jar target/cliente.jar --cafile ../backend/relatos-burger.pem
```
