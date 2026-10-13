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

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @RequestParam(required = false) Integer idEncuesta,
            @RequestParam(required = false) String facultad
    ) {
        if (facultad != null && facultad.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        DashboardResponseDTO response = dashboardService.obtenerDashboard(idEncuesta, facultad);
        return ResponseEntity.ok(response);
    }
}
