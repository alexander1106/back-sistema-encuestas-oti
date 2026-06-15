package com.gaslac.sistema_encuestas.modules.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gaslac.sistema_encuestas.modules.entity.Respuesta;

public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {

    List<Respuesta> findByUsuario_IdUsuario(Integer idUsuario);

    List<Respuesta> findByItem_Dimension_Encuesta_IdEncuesta(Integer idEncuesta);
        boolean existsByUsuario_IdUsuarioAndItem_Dimension_Encuesta_IdEncuesta(
            Integer idUsuario,
            Integer idEncuesta
    );
    @Query("""
    SELECT DISTINCT r.item.dimension.encuesta.idEncuesta
    FROM Respuesta r
    WHERE r.usuario.idUsuario = :idUsuario
""")
List<Integer> obtenerEncuestasRespondidas(
        @Param("idUsuario") Integer idUsuario
);


}