package com.gaslac.sistema_encuestas.modules.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaslac.sistema_encuestas.modules.entity.Item;


public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByDimension_IdDimension(Integer id);

    List<Item> findByDimension_Encuesta_IdEncuesta(Integer idEncuesta);
}