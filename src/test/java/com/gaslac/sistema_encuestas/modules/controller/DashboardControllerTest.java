package com.gaslac.sistema_encuestas.modules.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.gaslac.sistema_encuestas.modules.dto.dashboard.DashboardResponseDTO;
import com.gaslac.sistema_encuestas.modules.dto.dashboard.KpiDashboardDTO;
import com.gaslac.sistema_encuestas.modules.service.DashboardService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void devuelveElDashboardConLosFiltros() throws Exception {
        DashboardResponseDTO respuesta = new DashboardResponseDTO(
                new KpiDashboardDTO(10, 4, 4.2, "A", "B", 40.0),
                List.of(), List.of(), List.of());
        when(dashboardService.obtenerDashboard(eq(3), eq("Enfermería"))).thenReturn(respuesta);

        mockMvc.perform(get("/api/dashboard").param("idEncuesta", "3").param("facultad", "Enfermería"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis.totalRespuestas").value(10))
                .andExpect(jsonPath("$.kpis.mejorEscuela").value("A"));
    }

    @Test
    void sinParametrosLlamaAlServicioSinFiltros() throws Exception {
        when(dashboardService.obtenerDashboard(null, null)).thenReturn(
                new DashboardResponseDTO(null, List.of(), List.of(), List.of()));

        mockMvc.perform(get("/api/dashboard")).andExpect(status().isOk());

        verify(dashboardService).obtenerDashboard(null, null);
    }

    @Test
    void facultadEnBlancoDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/api/dashboard").param("facultad", " "))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(dashboardService);
    }
}
