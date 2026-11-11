# Cliente de linha de comando — Relatos Burger

Cliente HTTPS em **Java 21**, independente do backend, usando `java.net.http.HttpClient` e **Jackson** para ler o JSON.

## Pré-requisitos

- JDK 21 (`java -version`).
- API rodando com o profile `tls` em `https://localhost:8443`.
- Certificado público do servidor em formato `.pem` para validar a conexão TLS.

## Exportar o certificado do servidor

A partir de `backend/`, exporte o certificado público do keystore:

Bash / macOS / Linux:

```bash
cd backend
keytool -exportcert -alias relatos-burger \
  -keystore relatos-burger.p12 \
  -storepass changeit \
  -rfc \
  -file relatos-burger.pem
```

PowerShell (linha única):

```powershell
cd backend
keytool -exportcert -alias relatos-burger -keystore relatos-burger.p12 -storepass changeit -rfc -file relatos-burger.pem
```

> O `.pem` contém apenas o **certificado público**. A chave privada fica somente no `.p12`, que não deve ser versionado. O `.gitignore` ignora `*.p12`, `*.jks`, `*.pem` e `cliente/target/`.

## Compilar

A partir de `cliente/`:

```bash
./mvnw clean package
```

```powershell
.\mvnw.cmd clean package
```

O JAR executável é gerado em `target/cliente.jar`.

## Como rodar

A partir de `cliente/`:

```bash
java -jar target/cliente.jar --cafile ../backend/relatos-burger.pem
```

Modo de teste (sem validar o certificado, apenas para ambiente local):

```bash
java -jar target/cliente.jar --inseguro
```

Opções disponíveis:

| Opção        | Padrão      | Descrição                                                         |
| ------------ | ----------- | ----------------------------------------------------------------- |
| `--host`     | `localhost` | Host do servidor                                                  |
| `--porta`    | `8443`      | Porta HTTPS                                                       |
| `--cafile`   | —           | Certificado `.pem` do servidor para validar a conexão             |
| `--inseguro` | —           | Desativa a validação do certificado (exibe aviso; só para testes) |

## Menu

```
1. Listar categorias
2. Listar produtos (todos)
3. Listar produtos por categoria
4. Detalhar produto
5. Informações da hamburgueria
6. Status da API
0. Sair
```

Após cada requisição, o cliente mostra a versão do TLS e o cipher negociados, por exemplo:

```
[conexão segura] TLSv1.3 | TLS_AES_256_GCM_SHA384
```
