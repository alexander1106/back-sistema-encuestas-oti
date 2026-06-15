package com.gaslac.sistema_encuestas.modules.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.gaslac.sistema_encuestas.modules.dto.ExamenCompletoDTO;
import com.gaslac.sistema_encuestas.modules.dto.ExamenDTO;
import com.gaslac.sistema_encuestas.modules.dto.ExamenPuntajeDTO;
import com.gaslac.sistema_encuestas.modules.dto.KpiExamenDTO;
import com.gaslac.sistema_encuestas.modules.dto.PromedioCarreraDTO;
import com.gaslac.sistema_encuestas.modules.dto.PuntajeItemDTO;
import com.gaslac.sistema_encuestas.modules.dto.ReporteCompletoDTO;
import com.gaslac.sistema_encuestas.modules.dto.RespuestaDTO;
import com.gaslac.sistema_encuestas.modules.dto.RespuestaExamenDTO;
import com.gaslac.sistema_encuestas.modules.dto.RespuestaUsuarioDTO;
import com.gaslac.sistema_encuestas.modules.dto.ResultadoExamenDTO;
import com.gaslac.sistema_encuestas.modules.entity.Item;
import com.gaslac.sistema_encuestas.modules.entity.Respuesta;
import com.gaslac.sistema_encuestas.modules.entity.TipoItem;
import com.gaslac.sistema_encuestas.modules.entity.Usuario;
import com.gaslac.sistema_encuestas.modules.repository.ItemRepository;
import com.gaslac.sistema_encuestas.modules.repository.RespuestaRepository;
import com.gaslac.sistema_encuestas.modules.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemRepository itemRepository;

    public Respuesta guardar(RespuestaDTO dto){

        Integer idUsuario = Objects.requireNonNull(dto.getIdUsuario(), "idUsuario no puede ser nulo");
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + idUsuario));

        Integer idItem = Objects.requireNonNull(dto.getIdItem(), "idItem no puede ser nulo");
        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado: " + idItem));

        Respuesta respuesta = new Respuesta();

        respuesta.setUsuario(usuario);
        respuesta.setItem(item);
        respuesta.setValor(dto.getValor());
        respuesta.setFecha(LocalDateTime.now());

        return respuestaRepository.save(respuesta);
    }
    public long contarParticipantes(Integer idEncuesta) {

    return respuestaRepository
            .findByItem_Dimension_Encuesta_IdEncuesta(idEncuesta)
            .stream()
            .map(r -> r.getUsuario().getIdUsuario())
            .distinct()
            .count();
}


public KpiExamenDTO obtenerKpi(Integer idEncuesta, String carrera) {

    List<Respuesta> respuestas = respuestaRepository
        .findByItem_Dimension_Encuesta_IdEncuesta(idEncuesta);

    if (carrera != null && !carrera.isBlank()) {
        respuestas = respuestas.stream()
            .filter(r -> r.getUsuario().getEscuelaProfesional() != null
                && r.getUsuario().getEscuelaProfesional().equalsIgnoreCase(carrera))
            .toList();
    }

double promedio = respuestas.stream()
    .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
    .mapToInt(r -> Integer.parseInt(r.getValor()))
    .average()
    .orElse(0);

int totalRespuestas = (int) respuestas.stream()
    .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
    .count();

double max = respuestas.stream()
    .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
    .mapToInt(r -> Integer.parseInt(r.getValor()))
    .max()
    .orElse(0);

    long participantes = respuestas.stream()
        .map(r -> r.getUsuario().getIdUsuario())
        .distinct()
        .count();

    return new KpiExamenDTO(
        promedio,
        totalRespuestas,
        max,
        5.0,
        participantes
    );
}
public ResultadoExamenDTO guardarExamen(ExamenDTO dto) {

    Integer idUsuario = Objects.requireNonNull(
            dto.getIdUsuario(),
            "idUsuario no puede ser nulo"
    );

    // Obtener encuesta a partir del primer item
    Integer idPrimerItem = dto.getRespuestas().get(0).getIdItem();

    Item primerItem = itemRepository.findById(idPrimerItem)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Item no encontrado: " + idPrimerItem
            ));

    Integer idEncuesta = primerItem.getDimension()
            .getEncuesta()
            .getIdEncuesta();

    // Verificar si ya respondió
    boolean yaRespondio =
            respuestaRepository
                    .existsByUsuario_IdUsuarioAndItem_Dimension_Encuesta_IdEncuesta(
                            idUsuario,
                            idEncuesta
                    );

    if (yaRespondio) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Esta encuesta ya fue respondida"
        );
    }

    Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado: " + idUsuario
            ));

    int puntajeTotal = 0;
    int respuestasGuardadas = 0;

for (RespuestaExamenDTO respuestaDTO : dto.getRespuestas()) {

    Integer idItem = Objects.requireNonNull(
            respuestaDTO.getIdItem(),
            "idItem no puede ser nulo"
    );

    Item item = itemRepository.findById(idItem)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Item no encontrado: " + idItem
            ));

    Respuesta respuesta = new Respuesta();
    respuesta.setUsuario(usuario);
    respuesta.setItem(item);
    respuesta.setValor(respuestaDTO.getValor());
    respuesta.setFecha(LocalDateTime.now());

    respuestaRepository.save(respuesta);

    if (item.getTipo() == TipoItem.ESCALA) {

        puntajeTotal += Integer.parseInt(respuestaDTO.getValor());

        respuestasGuardadas++;
    }
}

    double puntajePromedio = respuestasGuardadas > 0
            ? puntajeTotal / (double) respuestasGuardadas
            : 0.0;

    return new ResultadoExamenDTO(
            usuario.getIdUsuario(),
            puntajeTotal,
            puntajePromedio,
            respuestasGuardadas
    );
}
public List<RespuestaUsuarioDTO> obtenerRespuestasPorUsuario(Integer idUsuario) {
    return respuestaRepository.findByUsuario_IdUsuario(idUsuario)
            .stream()
            .map(respuesta -> new RespuestaUsuarioDTO(
                    respuesta.getIdRespuesta(),
                    respuesta.getItem().getIdItem(),
                    respuesta.getItem().getNumero(),
                    respuesta.getItem().getTexto(),
                    respuesta.getValor(),
                    respuesta.getFecha()))
            .toList();
}

    public ExamenCompletoDTO obtenerExamenCompletoPorUsuario(Integer idUsuario) {
        java.util.List<RespuestaUsuarioDTO> respuestas = obtenerRespuestasPorUsuario(idUsuario);

   int puntajeTotal = respuestas.stream()
        .mapToInt(r -> Integer.parseInt(r.getValor()))
        .sum();

        int respuestasGuardadas = respuestas.size();

        double puntajePromedio = respuestasGuardadas > 0
                ? puntajeTotal / (double) respuestasGuardadas
                : 0.0;

        return new ExamenCompletoDTO(
                idUsuario,
                puntajeTotal,
                puntajePromedio,
                respuestasGuardadas,
                respuestas);
    }

public ExamenPuntajeDTO obtenerPuntajePorExamen(Integer idEncuesta, String carrera){        java.util.List<Item> items = itemRepository.findByDimension_Encuesta_IdEncuesta(idEncuesta);
List<Respuesta> respuestas = respuestaRepository
    .findByItem_Dimension_Encuesta_IdEncuesta(idEncuesta);

if (carrera != null && !carrera.isBlank()) {
    respuestas = respuestas.stream()
        .filter(r -> r.getUsuario().getEscuelaProfesional() != null
            && r.getUsuario().getEscuelaProfesional().equalsIgnoreCase(carrera))
        .toList();
}
 int puntajeTotal = respuestas.stream()
        .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
        .mapToInt(r -> Integer.parseInt(r.getValor()))
        .sum();

        int respuestasGuardadas = respuestas.size();

        double promedioGeneral = respuestasGuardadas > 0
                ? puntajeTotal / (double) respuestasGuardadas
                : 0.0;

     Map<Integer, List<Respuesta>> respuestasPorItem = respuestas.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getItem().getIdItem()));

    List<PuntajeItemDTO> detalleItems = items.stream()
        .map(item -> {

            List<Respuesta> respuestasItem =
                    respuestasPorItem.getOrDefault(
                            item.getIdItem(),
                            List.of()
                    );

            // Solo calcular métricas para preguntas ESCALA
            if (item.getTipo() != TipoItem.ESCALA) {

                return new PuntajeItemDTO(
                        item.getIdItem(),
                        item.getNumero(),
                        item.getTexto(),
                        0.0,
                        respuestasItem.size(),
                        0
                );
            }

            int totalItem = respuestasItem.stream()
                    .filter(r -> r.getValor() != null)
                    .mapToInt(r -> Integer.parseInt(r.getValor()))
                    .sum();

            int countItem = respuestasItem.size();

            double promedioItem =
                    countItem > 0
                            ? totalItem / (double) countItem
                            : 0.0;

            return new PuntajeItemDTO(
                    item.getIdItem(),
                    item.getNumero(),
                    item.getTexto(),
                    promedioItem,
                    countItem,
                    totalItem
            );
        })
        .toList();

        String nombreEncuesta = items.isEmpty() ? "" : items.get(0).getDimension().getEncuesta().getNombre();

        return new ExamenPuntajeDTO(
                idEncuesta,
                nombreEncuesta,
                puntajeTotal,
                promedioGeneral,
                respuestasGuardadas,
                detalleItems);
    }
public List<Integer> obtenerEncuestasRespondidas(Integer idUsuario) {

    return respuestaRepository
            .obtenerEncuestasRespondidas(idUsuario);
}
public List<PromedioCarreraDTO> obtenerPromedioPorCarrera(
        Integer idEncuesta,
        String carrera
) {

    List<Respuesta> respuestas = respuestaRepository
            .findByItem_Dimension_Encuesta_IdEncuesta(idEncuesta);

    if (carrera != null && !carrera.isBlank()) {
        respuestas = respuestas.stream()
                .filter(r -> r.getUsuario() != null
                        && r.getUsuario().getEscuelaProfesional() != null
                        && r.getUsuario().getEscuelaProfesional().equalsIgnoreCase(carrera))
                .toList();
    }

    Map<String, List<Respuesta>> agrupado = respuestas.stream()
            .filter(r -> r.getUsuario() != null)
            .collect(Collectors.groupingBy(r ->
                    r.getUsuario().getEscuelaProfesional() != null
                            ? r.getUsuario().getEscuelaProfesional()
                            : "SIN CARRERA"
            ));

  return agrupado.entrySet().stream()
        .map(entry -> new PromedioCarreraDTO(
                null,
                entry.getKey(),
                entry.getValue().stream()
                        .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                        .filter(r -> r.getValor() != null)
                        .mapToInt(r -> Integer.parseInt(r.getValor()))
                        .average()
                        .orElse(0),
                (int) entry.getValue().stream()
                        .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                        .count()
        ))
        .toList();
}
public ReporteCompletoDTO reporteCompleto(
        Integer idEncuesta,
        String carrera
) {

    // 1. KPI
    KpiExamenDTO kpi = obtenerKpi(idEncuesta, carrera);

    // 2. DETALLE POR PREGUNTA
    ExamenPuntajeDTO examen = obtenerPuntajePorExamen(idEncuesta, carrera);

    // 3. PROMEDIO POR CARRERA (YA VIENE LIMPIO)
    List<PromedioCarreraDTO> carreras =
            obtenerPromedioPorCarrera(idEncuesta, carrera);

    // 4. RESPONSE FINAL
    return new ReporteCompletoDTO(
            examen,
            carreras,
            kpi
    );
}private List<Respuesta> obtenerRespuestasFiltradas(Integer idEncuesta, String carrera) {
    List<Respuesta> respuestas = respuestaRepository
        .findByItem_Dimension_Encuesta_IdEncuesta(idEncuesta);

    if (carrera != null && !carrera.isBlank()) {
        respuestas = respuestas.stream()
            .filter(r -> r.getUsuario().getEscuelaProfesional() != null
                && r.getUsuario().getEscuelaProfesional().equalsIgnoreCase(carrera))
            .toList();
    }

    return respuestas;
}







public ReporteCompletoDTO reporteGlobal(String carrera) {

    // =========================
    // 1. TRAER RESPUESTAS
    // =========================
    List<Respuesta> respuestas = respuestaRepository.findAll();

    // =========================
    // 2. FILTRO POR CARRERA
    // =========================
    if (carrera != null && !carrera.isBlank()) {
        respuestas = respuestas.stream()
                .filter(r -> r.getUsuario().getEscuelaProfesional() != null
                        && r.getUsuario().getEscuelaProfesional().equalsIgnoreCase(carrera))
                .toList();
    }

    if (respuestas.isEmpty()) {
        return new ReporteCompletoDTO(
                new ExamenPuntajeDTO(0, "GLOBAL", 0, 0.0, 0, List.of()),
                List.of(),
                new KpiExamenDTO(0.0, 0, 0.0, 3.0, 0)
        );
    }

double promedio = respuestas.stream()
        .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
        .filter(r -> r.getValor() != null)
        .mapToInt(r -> Integer.parseInt(r.getValor()))
        .average()
        .orElse(0);

    int totalRespuestas = respuestas.size();

    double puntajeMaximo = 5.0; // escala Likert fija (CORRECTO)

    long participantes = respuestas.stream()
            .map(r -> r.getUsuario().getIdUsuario())
            .distinct()
            .count();

    KpiExamenDTO kpi = new KpiExamenDTO(
            promedio,
            totalRespuestas,
            puntajeMaximo,
            3.0,
            participantes
    );
// =========================
// 📊 PROMEDIO POR CARRERA
// =========================
Map<String, List<Respuesta>> porCarrera = respuestas.stream()
        .filter(r -> r.getUsuario() != null)
        .collect(Collectors.groupingBy(r ->
                r.getUsuario().getEscuelaProfesional() != null
                        ? r.getUsuario().getEscuelaProfesional()
                        : "SIN CARRERA"
        ));

List<PromedioCarreraDTO> carreras = porCarrera.entrySet()
        .stream()
        .map(e -> new PromedioCarreraDTO(
                null,
                e.getKey(),
                e.getValue().stream()
                        .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                        .filter(r -> r.getValor() != null)
                        .mapToInt(r -> Integer.parseInt(r.getValor()))
                        .average()
                        .orElse(0),
                (int) e.getValue().stream()
                        .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                        .count()
        ))
        .toList();

// =========================
// 📌 DETALLE GLOBAL POR ITEM
// =========================
Map<Integer, List<Respuesta>> porItem = respuestas.stream()
        .collect(Collectors.groupingBy(r -> r.getItem().getIdItem()));

List<Item> items = itemRepository.findAll();

ExamenPuntajeDTO examen = new ExamenPuntajeDTO(
        0,
        "GLOBAL",
        totalRespuestas,
        promedio,
        totalRespuestas,
        items.stream().map(item -> {

            List<Respuesta> resItem =
                    porItem.getOrDefault(item.getIdItem(), List.of());

            // Si no es ESCALA no calculamos promedio
            if (item.getTipo() != TipoItem.ESCALA) {

                return new PuntajeItemDTO(
                        item.getIdItem(),
                        item.getNumero(),
                        item.getTexto(),
                        0.0,
                        resItem.size(),
                        0
                );
            }

            double promItem = resItem.stream()
                    .filter(r -> r.getValor() != null)
                    .mapToInt(r -> Integer.parseInt(r.getValor()))
                    .average()
                    .orElse(0);

            int totalItem = resItem.stream()
                    .filter(r -> r.getValor() != null)
                    .mapToInt(r -> Integer.parseInt(r.getValor()))
                    .sum();

            return new PuntajeItemDTO(
                    item.getIdItem(),
                    item.getNumero(),
                    item.getTexto(),
                    promItem,
                    resItem.size(),
                    totalItem
            );

        }).toList()
);

// =========================
// 4. RESPONSE FINAL
// =========================
return new ReporteCompletoDTO(
        examen,
        carreras,
        kpi
);
}};