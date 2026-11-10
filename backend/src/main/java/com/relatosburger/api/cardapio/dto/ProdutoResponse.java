package com.relatosburger.api.cardapio.dto;

import java.math.BigDecimal;

import com.relatosburger.api.cardapio.domain.Produto;

public record ProdutoResponse(
		Long id,
		String nome,
		String descricao,
		BigDecimal preco,
		Long categoriaId,
		String categoriaNome,
		boolean disponivel) {

	public static ProdutoResponse from(Produto produto) {
		return new ProdutoResponse(
				produto.getId(),
				produto.getNome(),
				produto.getDescricao(),
				produto.getPreco(),
				produto.getCategoria().getId(),
				produto.getCategoria().getNome(),
				produto.isDisponivel());
	}

}
