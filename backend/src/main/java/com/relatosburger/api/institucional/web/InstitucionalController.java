package com.relatosburger.api.institucional.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.relatosburger.api.institucional.dto.InstitucionalResponse;
import com.relatosburger.api.institucional.repository.InformacaoInstitucionalRepository;
import com.relatosburger.api.shared.exception.RecursoNaoEncontradoException;

@RestController
@RequestMapping("/api")
public class InstitucionalController {

	private final InformacaoInstitucionalRepository informacaoRepository;

	public InstitucionalController(InformacaoInstitucionalRepository informacaoRepository) {
		this.informacaoRepository = informacaoRepository;
	}

	@GetMapping("/institucional")
	public InstitucionalResponse obterInformacoes() {
		return informacaoRepository.findTopByOrderByIdAsc()
				.map(InstitucionalResponse::from)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Informações institucionais não encontradas"));
	}

}
