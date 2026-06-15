package com.gaslac.sistema_encuestas.modules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;
    
    @Column(unique = true)
    private String dni;

    private String name;
    private String paternalSurname;
    private String maternalSurname;

    private String email;
    private String phoneNumber;

    private String escuelaProfesional;
    private String facultad;

    private String fechaEgreso;
    private String semestreEgreso;
    
}