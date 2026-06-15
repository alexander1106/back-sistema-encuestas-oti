package com.gaslac.sistema_encuestas.modules.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaslac.sistema_encuestas.modules.entity.Usuario;


public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByDni(String dni);

}