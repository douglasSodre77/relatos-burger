package com.relatosburger.cliente;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;

public class ApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    public ApiClient(String host, int porta, String cafile, boolean inseguro)
            throws GeneralSecurityException, IOException {
        if (inseguro) {
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        }
        this.baseUrl = "https://" + host + ":" + porta;
        SSLContext sslContext = criarSslContext(cafile, inseguro);

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .sslContext(sslContext);
        if (inseguro) {
            SSLParameters parametros = new SSLParameters();
            parametros.setEndpointIdentificationAlgorithm(null);
            builder.sslParameters(parametros);
        }
        this.httpClient = builder.build();
    }

    public <T> T get(String caminho, Class<T> tipo) throws ApiException {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + caminho))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> resposta;
        try {
            resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException erro) {
            throw new ApiException("Tempo esgotado ao conectar em " + baseUrl + " (limite de 5s).");
        } catch (ConnectException erro) {
            throw new ApiException("Servidor fora do ar ou inacessível em " + baseUrl
                    + ". Verifique se a API está rodando com o profile tls.");
        } catch (SSLHandshakeException erro) {
            throw new ApiException(mensagemCertificado());
        } catch (IOException erro) {
            if (erro instanceof SSLHandshakeException || erro.getCause() instanceof SSLHandshakeException) {
                throw new ApiException(mensagemCertificado());
            }
            throw new ApiException("Falha de comunicação com o servidor: " + erro.getMessage());
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new ApiException("Consulta interrompida.");
        }

        SslInfo ssl = obterSsl(resposta);
        Formatador.exibirTls(ssl.protocolo(), ssl.cipher());

        int status = resposta.statusCode();
        if (status == 404) {
            throw new ApiException(mensagemErro(resposta.body(), "Recurso não encontrado (HTTP 404)."));
        }
        if (status == 403) {
            throw new ApiException("Acesso negado (HTTP 403). A API de consulta aceita apenas requisições GET.");
        }
        if (status < 200 || status >= 300) {
            throw new ApiException("O servidor respondeu com HTTP " + status + ".");
        }

        try {
            return mapper.readValue(resposta.body(), tipo);
        } catch (JsonProcessingException erro) {
            throw new ApiException("O servidor respondeu em um formato inesperado (não é JSON).");
        }
    }

    private static SSLContext criarSslContext(String cafile, boolean inseguro)
            throws GeneralSecurityException, IOException {
        if (inseguro) {
            TrustManager[] confiarEmTodos = { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] cadeia, String tipoAutenticacao) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] cadeia, String tipoAutenticacao) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } };
            SSLContext contexto = SSLContext.getInstance("TLS");
            contexto.init(null, confiarEmTodos, new SecureRandom());
            return contexto;
        }

        if (cafile == null) {
            return SSLContext.getDefault();
        }

        CertificateFactory fabrica = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificados;
        try (InputStream entrada = Files.newInputStream(Path.of(cafile))) {
            certificados = fabrica.generateCertificates(entrada);
        }
        if (certificados.isEmpty()) {
            throw new GeneralSecurityException("O arquivo --cafile não contém certificados.");
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        int indice = 0;
        for (Certificate certificado : certificados) {
            trustStore.setCertificateEntry("servidor-" + indice++, certificado);
        }

        TrustManagerFactory fabricaTrust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        fabricaTrust.init(trustStore);
        SSLContext contexto = SSLContext.getInstance("TLS");
        contexto.init(null, fabricaTrust.getTrustManagers(), null);
        return contexto;
    }

    private static SslInfo obterSsl(HttpResponse<?> resposta) {
        return resposta.sslSession()
                .map(sessao -> new SslInfo(sessao.getProtocol(), sessao.getCipherSuite()))
                .orElse(new SslInfo(null, null));
    }

    private String mensagemErro(String corpo, String padrao) {
        try {
            Erro erro = mapper.readValue(corpo, Erro.class);
            if (erro.mensagem() != null && !erro.mensagem().isBlank()) {
                return erro.mensagem();
            }
        } catch (JsonProcessingException ignorado) {
            // usa a mensagem padrão
        }
        return padrao;
    }

    private static String mensagemCertificado() {
        return """
                Não foi possível validar o certificado TLS do servidor.
                Se o servidor usa certificado autoassinado, exporte o certificado público a partir de backend/:
                  keytool -exportcert -alias relatos-burger -keystore relatos-burger.p12 -storepass changeit -rfc -file relatos-burger.pem
                Depois execute o cliente com:
                  java -jar target/cliente.jar --cafile ../backend/relatos-burger.pem
                Para testes locais, use --inseguro (não recomendado).
                """;
    }

    public static class ApiException extends Exception {
        public ApiException(String mensagem) {
            super(mensagem);
        }
    }

    private record SslInfo(String protocolo, String cipher) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Categoria(int id, String nome) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Produto(int id, String nome, String descricao, BigDecimal preco,
                          int categoriaId, String categoriaNome, boolean disponivel) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Institucional(String nome, String descricao, String endereco,
                                String telefone, String horarioFuncionamento) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(String status, String app) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Erro(int status, String mensagem, String timestamp) {
    }
}
