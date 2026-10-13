package com.gaslac.sistema_encuestas.modules.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.gaslac.sistema_encuestas.modules.dto.dashboard.DashboardResponseDTO;
import com.gaslac.sistema_encuestas.modules.entity.Dimension;
import com.gaslac.sistema_encuestas.modules.entity.Encuesta;
import com.gaslac.sistema_encuestas.modules.entity.Item;
import com.gaslac.sistema_encuestas.modules.entity.Respuesta;
import com.gaslac.sistema_encuestas.modules.entity.TipoItem;
import com.gaslac.sistema_encuestas.modules.entity.Usuario;
import com.gaslac.sistema_encuestas.modules.repository.EncuestaRepository;
import com.gaslac.sistema_encuestas.modules.repository.RespuestaRepository;
import com.gaslac.sistema_encuestas.modules.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private RespuestaRepository respuestaRepository;
    @Mock
    private EncuestaRepository encuestaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DashboardService service;

    private Usuario usuario(int id, String escuela) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setEscuelaProfesional(escuela);
        return u;
    }

    private Item item(int id, int numero, String texto, TipoItem tipo) {
        Dimension d = new Dimension();
        d.setCodigo("D1");
        Item i = new Item();
        i.setIdItem(id);
        i.setNumero(numero);
        i.setTexto(texto);
        i.setTipo(tipo);
        i.setDimension(d);
        return i;
    }

    private Respuesta respuesta(Usuario u, Item i, String valor) {
        Respuesta r = new Respuesta();
        r.setUsuario(u);
        r.setItem(i);
        r.setValor(valor);
        return r;
    }

    @Test
    void sinRespuestasDevuelveDashboardVacio() {
        when(respuestaRepository.findAll()).thenReturn(List.of());

        DashboardResponseDTO dto = service.obtenerDashboard(null, null);

        assertEquals(0, dto.getKpis().getTotalRespuestas());
        assertEquals(0.0, dto.getKpis().getPromedioSatisfaccion());
        assertNull(dto.getKpis().getMejorEscuela());
        assertEquals(5, dto.getDistribucionSentimiento().size());
        assertEquals(0.0, dto.getDistribucionSentimiento().get(0).getPorcentaje());
        assertEquals(List.of(), dto.getComparativoPorEscuela());
        assertEquals(List.of(), dto.getRankingCalidadPreguntas());
    }

    @Test
    void encuestaInexistenteLanzaNotFound() {
        when(encuestaRepository.findById(anyInt())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.obtenerDashboard(99, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void filtraPorEncuestaUsandoElRepositorio() {
        when(encuestaRepository.findById(1)).thenReturn(Optional.of(new Encuesta()));
        when(respuestaRepository.findByItem_Dimension_Encuesta_IdEncuesta(1)).thenReturn(List.of());

        service.obtenerDashboard(1, null);

        verify(respuestaRepository).findByItem_Dimension_Encuesta_IdEncuesta(1);
    }

    @Test
    void calculaKpisComparativoSentimientoYRanking() {
        Usuario u1 = usuario(1, "Ingeniería de Sistemas");
        Usuario u2 = usuario(2, "Enfermería");
        Item p1 = item(1, 1, "Pregunta A", TipoItem.ESCALA);
        Item p2 = item(2, 2, "Pregunta B", TipoItem.ESCALA);
        Item texto = item(3, 3, "Comentarios", TipoItem.TEXTO);

        when(respuestaRepository.findAll()).thenReturn(List.of(
                respuesta(u1, p1, "5"),
                respuesta(u1, p2, "4"),
                respuesta(u2, p1, "1"),
                respuesta(u2, p2, "7"),
                respuesta(u2, texto, "bien")
        ));
        when(usuarioRepository.count()).thenReturn(4L);

        DashboardResponseDTO dto = service.obtenerDashboard(null, null);

        assertEquals(5, dto.getKpis().getTotalRespuestas());
        assertEquals(2, dto.getKpis().getEgresadosEncuestados());
        assertEquals(3.33, dto.getKpis().getPromedioSatisfaccion());
        assertEquals("Ingeniería de Sistemas", dto.getKpis().getMejorEscuela());
        assertEquals("Enfermería", dto.getKpis().getPeorEscuela());
        assertEquals(50.0, dto.getKpis().getTasaParticipacion());

        assertEquals(2, dto.getComparativoPorEscuela().size());
        assertEquals("Ingeniería de Sistemas", dto.getComparativoPorEscuela().get(0).getEscuelaProfesional());
        assertEquals(4.5, dto.getComparativoPorEscuela().get(0).getPromedioSatisfaccion());

        assertEquals("Muy Satisfecho", dto.getDistribucionSentimiento().get(0).getCategoria());
        assertEquals(50.0, dto.getDistribucionSentimiento().get(0).getPorcentaje());
        assertEquals(50.0, dto.getDistribucionSentimiento().get(4).getPorcentaje());

        assertEquals(2, dto.getRankingCalidadPreguntas().size());
        assertEquals("Pregunta B", dto.getRankingCalidadPreguntas().get(0).getDescripcion());
        assertEquals(4.0, dto.getRankingCalidadPreguntas().get(0).getPromedio());
        assertEquals("Bueno", dto.getRankingCalidadPreguntas().get(0).getEstado());
        assertEquals("Regular", dto.getRankingCalidadPreguntas().get(1).getEstado());
    }

    @Test
    void filtroDeFacultadIgnoraTildesYMayusculas() {
        Usuario u1 = usuario(1, "INGENIERÍA DE SISTEMAS");
        Usuario u2 = usuario(2, "Enfermería");
        Item p = item(1, 1, "Pregunta A", TipoItem.ESCALA);

        when(respuestaRepository.findAll()).thenReturn(List.of(
                respuesta(u1, p, "5"),
                respuesta(u2, p, "1")
        ));
        when(usuarioRepository.count()).thenReturn(2L);

        DashboardResponseDTO dto = service.obtenerDashboard(null, "ingenieria de sistemas");

        assertEquals(1, dto.getKpis().getTotalRespuestas());
        assertEquals(5.0, dto.getKpis().getPromedioSatisfaccion());
        assertEquals(1, dto.getComparativoPorEscuela().size());
    }

    @Test
    void sinEscalasValidasNoCalculaPromedioNiEscuelas() {
        Usuario u = usuario(1, "Enfermería");
        Item p = item(1, 1, "Pregunta A", TipoItem.ESCALA);

        when(respuestaRepository.findAll()).thenReturn(List.of(respuesta(u, p, "abc")));
        when(usuarioRepository.count()).thenReturn(1L);

        DashboardResponseDTO dto = service.obtenerDashboard(null, null);

        assertEquals(1, dto.getKpis().getTotalRespuestas());
        assertEquals(0.0, dto.getKpis().getPromedioSatisfaccion());
        assertNull(dto.getKpis().getMejorEscuela());
        assertNull(dto.getKpis().getPeorEscuela());
    }
}
