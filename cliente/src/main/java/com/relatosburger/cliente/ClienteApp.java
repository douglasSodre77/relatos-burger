package com.relatosburger.cliente;

import com.relatosburger.cliente.ApiClient.ApiException;
import com.relatosburger.cliente.ApiClient.Categoria;
import com.relatosburger.cliente.ApiClient.Institucional;
import com.relatosburger.cliente.ApiClient.Produto;
import com.relatosburger.cliente.ApiClient.Status;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class ClienteApp {

    private final ApiClient api;

    private ClienteApp(ApiClient api) {
        this.api = api;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int porta = 8443;
        String cafile = null;
        boolean inseguro = false;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--host" -> host = valor(args, ++i, "--host");
                    case "--porta" -> porta = lerPorta(valor(args, ++i, "--porta"));
                    case "--cafile" -> cafile = valor(args, ++i, "--cafile");
                    case "--inseguro" -> inseguro = true;
                    default -> throw new IllegalArgumentException("Argumento desconhecido: " + args[i]);
                }
            }
            if (cafile != null && inseguro) {
                throw new IllegalArgumentException("Use --cafile ou --inseguro, não os dois.");
            }
        } catch (IllegalArgumentException erro) {
            System.err.println("[erro] " + erro.getMessage());
            System.err.println("Uso: java -jar cliente.jar [--host HOST] [--porta PORTA] [--cafile CAMINHO.pem] [--inseguro]");
            return;
        }

        if (inseguro) {
            System.out.println("=".repeat(66));
            System.out.println("AVISO: a validação do certificado TLS está DESATIVADA (--inseguro).");
            System.out.println("Use apenas para testes locais. Prefira --cafile em qualquer outro caso.");
            System.out.println("=".repeat(66));
        }

        ApiClient api;
        try {
            api = new ApiClient(host, porta, cafile, inseguro);
        } catch (IOException erro) {
            System.err.println("[erro] Não foi possível carregar o certificado " + cafile + ": " + erro.getMessage());
            return;
        } catch (GeneralSecurityException erro) {
            System.err.println("[erro] Falha ao configurar o TLS: " + erro.getMessage());
            return;
        }

        System.out.println("Servidor: https://" + host + ":" + porta);
        new ClienteApp(api).executar();
    }

    private void executar() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                Formatador.exibirMenu();
                System.out.print("Escolha uma opção: ");
                String opcao = scanner.nextLine().trim();

                if (opcao.equals("0")) {
                    System.out.println("Até logo!");
                    break;
                }

                try {
                    switch (opcao) {
                        case "1" -> Formatador.exibirCategorias(api.get("/api/cardapio/categorias", Categoria[].class));
                        case "2" -> Formatador.exibirProdutos(api.get("/api/cardapio/produtos", Produto[].class));
                        case "3" -> {
                            Integer id = lerId(scanner, "ID da categoria: ");
                            if (id != null) {
                                Formatador.exibirProdutos(api.get("/api/cardapio/produtos?categoria=" + id, Produto[].class));
                            }
                        }
                        case "4" -> {
                            Integer id = lerId(scanner, "ID do produto: ");
                            if (id != null) {
                                Formatador.exibirProduto(api.get("/api/cardapio/produtos/" + id, Produto.class));
                            }
                        }
                        case "5" -> Formatador.exibirInstitucional(api.get("/api/institucional", Institucional.class));
                        case "6" -> Formatador.exibirStatus(api.get("/api/status", Status.class));
                        default -> System.out.println("[aviso] Opção inválida. Digite um número de 0 a 6.");
                    }
                } catch (ApiException erro) {
                    System.out.println("\n[erro] " + erro.getMessage());
                }
            }
        }
    }

    private static Integer lerId(Scanner scanner, String mensagem) {
        System.out.print(mensagem);
        String entrada = scanner.nextLine().trim();
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException erro) {
            System.out.println("[erro] Informe um número inteiro válido.");
            return null;
        }
    }

    private static String valor(String[] args, int indice, String opcao) {
        if (indice >= args.length || args[indice].startsWith("--")) {
            throw new IllegalArgumentException("A opção " + opcao + " exige um valor.");
        }
        return args[indice];
    }

    private static int lerPorta(String valor) {
        try {
            int porta = Integer.parseInt(valor);
            if (porta < 1 || porta > 65535) {
                throw new NumberFormatException();
            }
            return porta;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Porta inválida: " + valor);
        }
    }
}
