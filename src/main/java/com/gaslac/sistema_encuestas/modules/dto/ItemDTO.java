package com.gaslac.sistema_encuestas.modules.dto;

import com.gaslac.sistema_encuestas.modules.entity.TipoItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
    private Integer idItem;
    private Integer numero;
    private String texto;
private TipoItem tipoPregunta;
}