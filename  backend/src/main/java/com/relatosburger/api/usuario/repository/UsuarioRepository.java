package com.relatosburger.api.usuario.repository;

import com.relatosburger.api.usuario.domain.TipoUsuario;
import com.relatosburger.api.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByTipoUsuarioOrderByNomeAsc(TipoUsuario tipoUsuario);
}