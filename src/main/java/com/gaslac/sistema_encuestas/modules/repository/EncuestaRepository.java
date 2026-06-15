package com.gaslac.sistema_encuestas.modules.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gaslac.sistema_encuestas.modules.entity.Encuesta;


public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {

    List<Encuesta> findByNombreContainingIgnoreCase(String nombre);

    List<Encuesta> findByCargoContainingIgnoreCase(String cargo);
    @Query("""
    SELECT DISTINCT i.dimension.encuesta.idEncuesta
    FROM Respuesta r
    JOIN r.item i
    WHERE r.usuario.idUsuario = :idUsuario
""")
List<Integer> obtenerEncuestasRespondidas(
        @Param("idUsuario") Integer idUsuario
);

    List<Encuesta> findByNombreContainingIgnoreCaseAndCargoContainingIgnoreCase(String nombre, String cargo);
}