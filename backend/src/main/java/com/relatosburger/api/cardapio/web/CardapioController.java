package com.relatosburger.api.cardapio.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.relatosburger.api.cardapio.dto.CategoriaResponse;
import com.relatosburger.api.cardapio.dto.ProdutoResponse;
import com.relatosburger.api.cardapio.repository.CategoriaRepository;
import com.relatosburger.api.cardapio.repository.ProdutoRepository;
import com.relatosburger.api.shared.exception.RecursoNaoEncontradoException;

@RestController
@RequestMapping("/api/cardapio")
public class CardapioController {

	private final CategoriaRepository categoriaRepository;
	private final ProdutoRepository produtoRepository;

	public CardapioController(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
		this.categoriaRepository = categoriaRepository;
		this.produtoRepository = produtoRepository;
	}

	@GetMapping("/categorias")
	public List<CategoriaResponse> listarCategorias() {
		return categoriaRepository.findAll().stream()
				.map(CategoriaResponse::from)
				.toList();
	}

	@GetMapping("/produtos")
	public List<ProdutoResponse> listarProdutos(@RequestParam(required = false) Long categoria) {
		var produtos = categoria == null
				? produtoRepository.findAll()
				: produtoRepository.findByCategoriaId(categoria);
		return produtos.stream()
				.map(ProdutoResponse::from)
				.toList();
	}

	@GetMapping("/produtos/{id}")
	public ProdutoResponse detalharProduto(@PathVariable Long id) {
		return produtoRepository.findById(id)
				.map(ProdutoResponse::from)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
	}

}
