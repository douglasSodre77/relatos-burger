package com.relatosburger.api.cardapio.dto;

import com.relatosburger.api.cardapio.domain.Categoria;

public record CategoriaResponse(Long id, String nome) {

	public static CategoriaResponse from(Categoria categoria) {
		return new CategoriaResponse(categoria.getId(), categoria.getNome());
	}

}
