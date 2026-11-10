package com.relatosburger.api.institucional.dto;

import com.relatosburger.api.institucional.domain.InformacaoInstitucional;

public record InstitucionalResponse(
		String nome,
		String descricao,
		String endereco,
		String telefone,
		String horarioFuncionamento) {

	public static InstitucionalResponse from(InformacaoInstitucional informacao) {
		return new InstitucionalResponse(
				informacao.getNome(),
				informacao.getDescricao(),
				informacao.getEndereco(),
				informacao.getTelefone(),
				informacao.getHorarioFuncionamento());
	}

}
