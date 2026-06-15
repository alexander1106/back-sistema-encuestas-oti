package com.gaslac.sistema_encuestas.modules.controller;
import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaslac.sistema_encuestas.modules.dto.EncuestaCompletaDTO;
import com.gaslac.sistema_encuestas.modules.dto.EncuestaNombreDTO;
import com.gaslac.sistema_encuestas.modules.dto.ExamenCompletoDTO;
import com.gaslac.sistema_encuestas.modules.dto.ExamenDTO;
import com.gaslac.sistema_encuestas.modules.dto.ExamenPuntajeDTO;
import com.gaslac.sistema_encuestas.modules.dto.KpiExamenDTO;
import com.gaslac.sistema_encuestas.modules.dto.PromedioCarreraDTO;
import com.gaslac.sistema_encuestas.modules.dto.ReporteCompletoDTO;
import com.gaslac.sistema_encuestas.modules.dto.ResultadoExamenDTO;
import com.gaslac.sistema_encuestas.modules.service.EncuestaService;
import com.gaslac.sistema_encuestas.modules.service.RespuestaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/examen")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ExamenController {

    private final RespuestaService respuestaService;
    private final EncuestaService encuestaService;

    @PostMapping
    public ResultadoExamenDTO guardarPuntajesExamen(@RequestBody ExamenDTO dto) {
        return respuestaService.guardarExamen(dto);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ExamenCompletoDTO obtenerExamenCompletoPorUsuario(@PathVariable Integer idUsuario) {
        return respuestaService.obtenerExamenCompletoPorUsuario(idUsuario);
    }
    

    @GetMapping
    public java.util.List<EncuestaCompletaDTO> obtenerTodosLosExamenes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String cargo) {
        return encuestaService.obtenerEncuestas(nombre, cargo);
    }

    @GetMapping("/nombres")
    public java.util.List<EncuestaNombreDTO> obtenerNombresEncuestas() {
        return encuestaService.obtenerNombresEncuestas();
    }

    @GetMapping("/{id}")
    public EncuestaCompletaDTO obtenerExamenCompleto(@PathVariable Integer id) {
        return encuestaService.obtenerEncuesta(id);
    }

@GetMapping("/{id}/puntajes")
public ExamenPuntajeDTO obtenerPuntajePorExamen(
        @PathVariable Integer id,
        @RequestParam(required = false) String carrera
) {
    return respuestaService.obtenerPuntajePorExamen(id, carrera);
}
    @GetMapping("/{idEncuesta}/promedio-carrera")
public List<PromedioCarreraDTO> promedioPorCarrera(
        @PathVariable Integer idEncuesta,
@RequestParam(required = false) String carrera        
) {
    return respuestaService.obtenerPromedioPorCarrera(idEncuesta, carrera);
}
@GetMapping("/{idEncuesta}/kpi")
public KpiExamenDTO kpi(
    @PathVariable Integer idEncuesta,
    @RequestParam(required = false) String carrera
) {
    return respuestaService.obtenerKpi(idEncuesta, carrera);
}

@GetMapping("/{idEncuesta}/reporte-completo")
public ReporteCompletoDTO reporteCompleto(
        @PathVariable Integer idEncuesta,
        @RequestParam(required = false) String carrera
) {
    return respuestaService.reporteCompleto(idEncuesta, carrera);
}
@GetMapping("/reporte-global")
public ReporteCompletoDTO reporteGlobal(
    @RequestParam(required = false) String carrera
) {
    return respuestaService.reporteGlobal(carrera);
}
}
