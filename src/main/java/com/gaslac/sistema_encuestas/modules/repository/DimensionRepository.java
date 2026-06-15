package com.gaslac.sistema_encuestas.modules.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaslac.sistema_encuestas.modules.entity.Dimension;

public interface DimensionRepository extends JpaRepository<Dimension, Integer> {

    List<Dimension> findByEncuesta_IdEncuesta(Integer idEncuesta);

}