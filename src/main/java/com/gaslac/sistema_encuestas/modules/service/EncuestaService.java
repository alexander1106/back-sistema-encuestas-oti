package com.gaslac.sistema_encuestas.modules.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gaslac.sistema_encuestas.modules.dto.DimensionDTO;
import com.gaslac.sistema_encuestas.modules.dto.EncuestaCompletaDTO;
import com.gaslac.sistema_encuestas.modules.dto.EncuestaNombreDTO;
import com.gaslac.sistema_encuestas.modules.dto.ItemDTO;
import com.gaslac.sistema_encuestas.modules.entity.Encuesta;
import com.gaslac.sistema_encuestas.modules.repository.DimensionRepository;
import com.gaslac.sistema_encuestas.modules.repository.EncuestaRepository ;
import com.gaslac.sistema_encuestas.modules.repository.ItemRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class EncuestaService {

    private final EncuestaRepository encuestaRepository;
    private final DimensionRepository dimensionRepository;
    private final ItemRepository itemRepository;

    public EncuestaCompletaDTO obtenerEncuesta(Integer idEncuesta) {

        Encuesta encuesta = encuestaRepository.findById(idEncuesta)
                .orElseThrow(() -> new RuntimeException("Encuesta no encontrada"));

        List<DimensionDTO> dimensiones = dimensionRepository
                .findByEncuesta_IdEncuesta(idEncuesta)
                .stream()
                .map(d -> {

                    List<ItemDTO> items = itemRepository.findByDimension_IdDimension(d.getIdDimension())
                            .stream()
                           .map(i -> new ItemDTO(
        i.getIdItem(),
        i.getNumero(),
        i.getTexto(),
        i.getTipo() // 👈 ESTE CAMPO
))
                            .toList();

                    return new DimensionDTO(
                            d.getIdDimension(),
                            d.getNombre(),
                            items,
                            d.getCodigo());
                })
                .toList();

        return new EncuestaCompletaDTO(
                encuesta.getIdEncuesta(),
                encuesta.getNombre(),
                dimensiones,
                encuesta.getInicio_rango(),
                encuesta.getFin_rango(),
                encuesta.getCargo()
        );
    }

    public java.util.List<EncuestaCompletaDTO> obtenerTodasEncuestas() {
        return obtenerEncuestas(null, null);
    }

    public java.util.List<EncuestaNombreDTO> obtenerNombresEncuestas() {
        return encuestaRepository.findAll()
                .stream()
                .map(encuesta -> new EncuestaNombreDTO(encuesta.getIdEncuesta(), encuesta.getNombre()))
                .toList();
    }

    public java.util.List<EncuestaCompletaDTO> obtenerEncuestas(String nombre, String cargo) {
        java.util.List<Encuesta> encuestas;

        boolean tieneNombre = nombre != null && !nombre.isBlank();
        boolean tieneCargo = cargo != null && !cargo.isBlank();

        if (tieneNombre && tieneCargo) {
            encuestas = encuestaRepository.findByNombreContainingIgnoreCaseAndCargoContainingIgnoreCase(nombre, cargo);
        } else if (tieneNombre) {
            encuestas = encuestaRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (tieneCargo) {
            encuestas = encuestaRepository.findByCargoContainingIgnoreCase(cargo);
        } else {
            encuestas = encuestaRepository.findAll();
        }

        return encuestas.stream()
                .map(this::mapEncuestaCompletaDTO)
                .toList();
    }

    private EncuestaCompletaDTO mapEncuestaCompletaDTO(Encuesta encuesta) {
        java.util.List<DimensionDTO> dimensiones = dimensionRepository
                .findByEncuesta_IdEncuesta(encuesta.getIdEncuesta())
                .stream()
                .map(d -> {

                    java.util.List<ItemDTO> items = itemRepository.findByDimension_IdDimension(d.getIdDimension())
                            .stream()
                           .map(i -> new ItemDTO(
        i.getIdItem(),
        i.getNumero(),
        i.getTexto(),
        i.getTipo()
))
                            .toList();

                    return new DimensionDTO(
                            d.getIdDimension(),
                            d.getNombre(),
                            items,
                            d.getCodigo());
                })
                .toList();

        return new EncuestaCompletaDTO(
                encuesta.getIdEncuesta(),
                encuesta.getNombre(),
                dimensiones,
                encuesta.getInicio_rango(),
                encuesta.getFin_rango(),
                encuesta.getCargo()
        );
    }
}
