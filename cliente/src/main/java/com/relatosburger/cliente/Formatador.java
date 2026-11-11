package com.relatosburger.cliente;

import com.relatosburger.cliente.ApiClient.Categoria;
import com.relatosburger.cliente.ApiClient.Institucional;
import com.relatosburger.cliente.ApiClient.Produto;
import com.relatosburger.cliente.ApiClient.Status;

import java.math.BigDecimal;
import java.util.Locale;

public final class Formatador {

    private Formatador() {
    }

    public static void exibirMenu() {
        System.out.println();
        System.out.println("=== Relatos Burger - Consulta ===");
        System.out.println("1. Listar categorias");
        System.out.println("2. Listar produtos (todos)");
        System.out.println("3. Listar produtos por categoria");
        System.out.println("4. Detalhar produto");
        System.out.println("5. Informações da hamburgueria");
        System.out.println("6. Status da API");
        System.out.println("0. Sair");
    }

    public static void exibirTls(String protocolo, String cipher) {
        if (protocolo != null && cipher != null) {
            System.out.printf("[conexão segura] %s | %s%n", protocolo, cipher);
        } else if (protocolo != null) {
            System.out.printf("[conexão segura] %s%n", protocolo);
        }
    }

    public static void exibirCategorias(Categoria[] categorias) {
        if (categorias == null || categorias.length == 0) {
            System.out.println("Nenhuma categoria cadastrada.");
            return;
        }
        System.out.println();
        System.out.printf("%4s  %s%n", "ID", "Categoria");
        System.out.println("-".repeat(40));
        for (Categoria categoria : categorias) {
            System.out.printf("%4d  %s%n", categoria.id(), categoria.nome());
        }
    }

    public static void exibirProdutos(Produto[] produtos) {
        if (produtos == null || produtos.length == 0) {
            System.out.println("Nenhum produto encontrado.");
            return;
        }
        System.out.println();
        String cabecalho = String.format("%4s  %-24s %-18s %11s  %s",
                "ID", "Nome", "Categoria", "Preço", "Status");
        System.out.println(cabecalho);
        System.out.println("-".repeat(cabecalho.length()));
        for (Produto produto : produtos) {
            String nome = truncar(produto.nome(), 24);
            String categoria = truncar(produto.categoriaNome(), 18);
            String status = produto.disponivel() ? "Disponível" : "Indisponível";
            System.out.printf("%4d  %-24s %-18s %11s  %s%n",
                    produto.id(), nome, categoria, formatarPreco(produto.preco()), status);
        }
    }

    public static void exibirProduto(Produto produto) {
        System.out.println();
        System.out.println("ID: " + produto.id());
        System.out.println("Nome: " + produto.nome());
        System.out.println("Categoria: " + produto.categoriaNome());
        System.out.println("Preço: " + formatarPreco(produto.preco()));
        System.out.println("Status: " + (produto.disponivel() ? "Disponível" : "Indisponível"));
        System.out.println("Descrição: " + produto.descricao());
    }

    public static void exibirInstitucional(Institucional dados) {
        System.out.println();
        System.out.println("Nome: " + dados.nome());
        System.out.println("Descrição: " + dados.descricao());
        System.out.println("Endereço: " + dados.endereco());
        System.out.println("Telefone: " + dados.telefone());
        System.out.println("Horário: " + dados.horarioFuncionamento());
    }

    public static void exibirStatus(Status dados) {
        System.out.println();
        System.out.println("Status: " + dados.status());
        System.out.println("Aplicação: " + dados.app());
    }

    public static String formatarPreco(BigDecimal valor) {
        if (valor == null) {
            return "R$ --";
        }
        return "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", valor);
    }

    private static String truncar(String texto, int tamanho) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= tamanho) {
            return texto;
        }
        return texto.substring(0, tamanho - 3) + "...";
    }
}
