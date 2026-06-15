package com.gaslac.sistema_encuestas.modules.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaslac.sistema_encuestas.modules.dto.dashboard.DashboardResponseDTO;
import com.gaslac.sistema_encuestas.modules.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard
     *
     * Parámetros opcionales:
     *   - idEncuesta : filtra por encuesta específica (Integer)
     *   - facultad   : filtra por nombre de facultad (String, case-insensitive)
     *
     * Sin parámetros → devuelve el dashboard de todas las encuestas y egresados.
     */
    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @RequestParam(required = false) Integer idEncuesta,
            @RequestParam(required = false) String facultad
    ) {
        // Validar que facultad no sea blank si fue enviada
        if (facultad != null && facultad.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        DashboardResponseDTO response = dashboardService.obtenerDashboard(idEncuesta, facultad);
        return ResponseEntity.ok(response);
    }
}
