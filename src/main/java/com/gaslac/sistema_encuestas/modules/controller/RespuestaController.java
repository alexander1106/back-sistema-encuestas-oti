package com.gaslac.sistema_encuestas.modules.controller;


import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaslac.sistema_encuestas.modules.dto.EncuestaCompletaDTO;
import com.gaslac.sistema_encuestas.modules.dto.ExamenDTO;
import com.gaslac.sistema_encuestas.modules.dto.RespuestaDTO;
import com.gaslac.sistema_encuestas.modules.dto.RespuestaUsuarioDTO;
import com.gaslac.sistema_encuestas.modules.dto.ResultadoExamenDTO;
import com.gaslac.sistema_encuestas.modules.entity.Respuesta;
import com.gaslac.sistema_encuestas.modules.service.EncuestaService;
import com.gaslac.sistema_encuestas.modules.service.RespuestaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/respuestas")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RespuestaController {

    private final RespuestaService respuestaService;
    private final EncuestaService encuestaService;


    @PostMapping
    public Respuesta guardar(
            @RequestBody RespuestaDTO dto){

        return respuestaService.guardar(dto);
    }

    @PostMapping("/examen")
    public ResultadoExamenDTO guardarPuntajesExamen(
            @RequestBody ExamenDTO dto) {

        return respuestaService.guardarExamen(dto);
    }

@GetMapping("/respondidas/{idUsuario}")
public List<Integer> obtenerEncuestasRespondidas(
        @PathVariable Integer idUsuario) {

    return respuestaService
            .obtenerEncuestasRespondidas(idUsuario);
}

    @GetMapping("/usuario/{idUsuario}")
    public List<RespuestaUsuarioDTO> obtenerRespuestasPorUsuario(
            @PathVariable Integer idUsuario) {

        return respuestaService.obtenerRespuestasPorUsuario(idUsuario);
    }

   @GetMapping("/{id}")
    public EncuestaCompletaDTO obtenerEncuesta(
            @PathVariable Integer id) {

        return encuestaService.obtenerEncuesta(id);
    }
}