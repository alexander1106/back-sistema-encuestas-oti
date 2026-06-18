package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilaExportacionDTO {

    private String dni;
    private String nombreCompleto;
    private String facultad;
    private String escuelaProfesional;

    /** Respuestas en el mismo orden que ExportacionEncuestaDTO.preguntas */
    private List<String> respuestas;
}
