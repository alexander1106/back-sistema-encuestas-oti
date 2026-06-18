package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportacionEncuestaDTO {

    private Integer idEncuesta;
    private String nombreEncuesta;

    /** Cabeceras de pregunta (con su dimensión), en orden (respuesta1, respuesta2, ...) */
    private List<PreguntaExportacionDTO> preguntas;

    private List<FilaExportacionDTO> filas;
}
