package com.relatosburger.api.institucional.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.relatosburger.api.institucional.domain.InformacaoInstitucional;

public interface InformacaoInstitucionalRepository extends JpaRepository<InformacaoInstitucional, Long> {

	Optional<InformacaoInstitucional> findTopByOrderByIdAsc();

}
