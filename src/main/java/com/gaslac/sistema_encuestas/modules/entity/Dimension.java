package com.gaslac.sistema_encuestas.modules.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dimension")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDimension;

    private String nombre;
        private String codigo;


@ManyToOne
@JoinColumn(name = "id_encuesta")
private Encuesta encuesta;

}