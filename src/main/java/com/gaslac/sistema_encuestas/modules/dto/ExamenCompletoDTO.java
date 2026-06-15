package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamenCompletoDTO {

    private Integer idUsuario;
    private Integer puntajeTotal;
    private Double puntajePromedio;
    private Integer respuestasGuardadas;
    private List<RespuestaUsuarioDTO> respuestas;
}
