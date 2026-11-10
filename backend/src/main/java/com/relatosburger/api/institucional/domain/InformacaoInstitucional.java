package com.relatosburger.api.institucional.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "informacoes_institucionais")
@Getter
@Setter
@NoArgsConstructor
public class InformacaoInstitucional {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(length = 1000)
	private String descricao;

	@Column(length = 255)
	private String endereco;

	@Column(length = 50)
	private String telefone;

	@Column(name = "horario_funcionamento", length = 255)
	private String horarioFuncionamento;

}
