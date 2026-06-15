package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncuestaCompletaDTO {

    private Integer idEncuesta;
    private String nombre;
    private List<DimensionDTO> dimensiones;
    private Integer inicioRango;
    private Integer finRango;
    private String cargo;
}