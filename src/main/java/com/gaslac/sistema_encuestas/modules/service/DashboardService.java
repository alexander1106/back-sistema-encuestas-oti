package com.gaslac.sistema_encuestas.modules.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.gaslac.sistema_encuestas.modules.dto.dashboard.ComparativoEscuelaDTO;
import com.gaslac.sistema_encuestas.modules.dto.dashboard.DashboardResponseDTO;
import com.gaslac.sistema_encuestas.modules.dto.dashboard.KpiDashboardDTO;
import com.gaslac.sistema_encuestas.modules.dto.dashboard.RankingPreguntaDTO;
import com.gaslac.sistema_encuestas.modules.dto.dashboard.SentimientoDTO;
import com.gaslac.sistema_encuestas.modules.entity.Respuesta;
import com.gaslac.sistema_encuestas.modules.entity.TipoItem;
import com.gaslac.sistema_encuestas.modules.repository.EncuestaRepository;
import com.gaslac.sistema_encuestas.modules.repository.RespuestaRepository;
import com.gaslac.sistema_encuestas.modules.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RespuestaRepository respuestaRepository;
    private final EncuestaRepository encuestaRepository;
    private final UsuarioRepository usuarioRepository;

    // Categorías de sentimiento en orden fijo
    private static final List<String> CATEGORIAS_SENTIMIENTO = List.of(
            "Muy Satisfecho", "Satisfecho", "Neutral", "Insatisfecho", "Muy Insatisfecho"
    );

    public DashboardResponseDTO obtenerDashboard(Integer idEncuesta, String facultad) {

        // ── 1. Validar encuesta si se proporcionó ──────────────────────────────────
        if (idEncuesta != null) {
            encuestaRepository.findById(idEncuesta)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "No existe una Encuesta con idEncuesta: " + idEncuesta
                    ));
        }

        // ── 2. Obtener respuestas base ─────────────────────────────────────────────
        List<Respuesta> respuestas;
        if (idEncuesta != null) {
            respuestas = respuestaRepository.findByItem_Dimension_Encuesta_IdEncuesta(idEncuesta);
        } else {
            respuestas = respuestaRepository.findAll();
        }

        // ── 3. Filtro por escuela profesional (parámetro: facultad) ───────────────
        if (facultad != null && !facultad.isBlank()) {
            // Normalizar: quitar acentos y comparar en minúsculas para evitar
            // problemas de encoding con tildes y ñ (ej. INGENIERÍA vs INGENIERIA)
            final String escuelaNorm = normalizarTexto(facultad);
            respuestas = respuestas.stream()
                    .filter(r -> r.getUsuario() != null
                            && r.getUsuario().getEscuelaProfesional() != null
                            && normalizarTexto(r.getUsuario().getEscuelaProfesional()).equals(escuelaNorm))
                    .collect(Collectors.toList());
        }

        // ── 4. Respuesta vacía ─────────────────────────────────────────────────────
        if (respuestas.isEmpty()) {
            return buildEmptyResponse();
        }

        // ── 5. Calcular secciones ──────────────────────────────────────────────────
        KpiDashboardDTO kpis = calcularKpis(respuestas);
        List<ComparativoEscuelaDTO> comparativo = calcularComparativoEscuela(respuestas);
        List<SentimientoDTO> sentimiento = calcularDistribucionSentimiento(respuestas);
        List<RankingPreguntaDTO> ranking = calcularRankingPreguntas(respuestas);

        return new DashboardResponseDTO(kpis, comparativo, sentimiento, ranking);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // KPIs
    // ─────────────────────────────────────────────────────────────────────────────
    private KpiDashboardDTO calcularKpis(List<Respuesta> respuestas) {

        // Total respuestas (todos los tipos)
        int totalRespuestas = respuestas.size();

        // Egresados encuestados (usuarios distintos)
        int egresadosEncuestados = (int) respuestas.stream()
                .map(r -> r.getUsuario().getIdUsuario())
                .distinct()
                .count();

        // Tasa de participación: egresados con respuesta / total usuarios en el sistema
        long totalUsuariosSistema = usuarioRepository.count();
        double tasaParticipacion = totalUsuariosSistema > 0
                ? redondear((egresadosEncuestados * 100.0) / totalUsuariosSistema)
                : 0.0;

        // Respuestas ESCALA con valor parseable [1-5]
        List<Respuesta> escalaValidas = respuestas.stream()
                .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                .filter(r -> parsearEscala(r.getValor()) != null)
                .collect(Collectors.toList());

        if (escalaValidas.isEmpty()) {
            return new KpiDashboardDTO(
                    totalRespuestas, egresadosEncuestados,
                    0.0, null, null, tasaParticipacion
            );
        }

        // Promedio general de satisfacción
        double promedioSatisfaccion = redondear(
                escalaValidas.stream()
                        .mapToInt(r -> parsearEscala(r.getValor()))
                        .average()
                        .orElse(0.0)
        );

        // Promedio por escuela para mejor/peor
        Map<String, Double> promediosPorEscuela = calcularPromediosPorEscuela(escalaValidas);

        String mejorEscuela = promediosPorEscuela.entrySet().stream()
                .max(Comparator.<Map.Entry<String, Double>, Double>comparing(Map.Entry::getValue)
                        .thenComparing(Comparator.<Map.Entry<String, Double>, String>comparing(Map.Entry::getKey).reversed()))
                .map(Map.Entry::getKey)
                .orElse(null);

        String peorEscuela = promediosPorEscuela.entrySet().stream()
                .min(Comparator.<Map.Entry<String, Double>, Double>comparing(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);

        return new KpiDashboardDTO(
                totalRespuestas, egresadosEncuestados,
                promedioSatisfaccion, mejorEscuela, peorEscuela, tasaParticipacion
        );
    }

  
    private List<ComparativoEscuelaDTO> calcularComparativoEscuela(List<Respuesta> respuestas) {

        // Solo ESCALA con valor parseable
        Map<String, List<Respuesta>> porEscuela = respuestas.stream()
                .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                .filter(r -> parsearEscala(r.getValor()) != null)
                .collect(Collectors.groupingBy(r -> {
                    String ep = r.getUsuario().getEscuelaProfesional();
                    return (ep != null && !ep.isBlank()) ? ep : "SIN ESCUELA";
                }));

        return porEscuela.entrySet().stream()
                .map(entry -> {
                    List<Respuesta> grupo = entry.getValue();
                    double prom = redondear(
                            grupo.stream()
                                    .mapToInt(r -> parsearEscala(r.getValor()))
                                    .average()
                                    .orElse(0.0)
                    );
                    return new ComparativoEscuelaDTO(entry.getKey(), prom, grupo.size());
                })
                // Ordenar de mayor a menor promedio, empate → alfabético ascendente
                .sorted(Comparator.<ComparativoEscuelaDTO>comparingDouble(ComparativoEscuelaDTO::getPromedioSatisfaccion)
                        .reversed()
                        .thenComparing(ComparativoEscuelaDTO::getEscuelaProfesional))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Distribución de sentimiento
    // ─────────────────────────────────────────────────────────────────────────────
    private List<SentimientoDTO> calcularDistribucionSentimiento(List<Respuesta> respuestas) {

        // Promedio individual por egresado (solo ESCALA parseables)
        Map<Integer, List<Integer>> escalasPorUsuario = respuestas.stream()
                .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
                .filter(r -> parsearEscala(r.getValor()) != null)
                .collect(Collectors.groupingBy(
                        r -> r.getUsuario().getIdUsuario(),
                        Collectors.mapping(r -> parsearEscala(r.getValor()), Collectors.toList())
                ));

        // Egresados que sí tienen respuestas ESCALA válidas
        List<Double> promediosIndividuales = escalasPorUsuario.values().stream()
                .map(vals -> vals.stream().mapToInt(Integer::intValue).average().orElse(0.0))
                .collect(Collectors.toList());

        long total = promediosIndividuales.size();

        // Contar por categoría
        Map<String, Long> conteo = promediosIndividuales.stream()
                .collect(Collectors.groupingBy(this::clasificarSentimiento, Collectors.counting()));

        // Construir lista con las 5 categorías siempre presentes
        List<SentimientoDTO> resultado = new ArrayList<>();
        for (String cat : CATEGORIAS_SENTIMIENTO) {
            double porcentaje = total > 0
                    ? redondear((conteo.getOrDefault(cat, 0L) * 100.0) / total)
                    : 0.00;
            resultado.add(new SentimientoDTO(cat, porcentaje));
        }
        return resultado;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Ranking de calidad por pregunta
    // ─────────────────────────────────────────────────────────────────────────────
private List<RankingPreguntaDTO> calcularRankingPreguntas(List<Respuesta> respuestas) {

    // Solo preguntas tipo ESCALA con respuestas válidas
    Map<Integer, List<Respuesta>> porItem = respuestas.stream()
            .filter(r -> r.getItem() != null)
            .filter(r -> r.getItem().getTipo() == TipoItem.ESCALA)
            .filter(r -> parsearEscala(r.getValor()) != null)
            .collect(Collectors.groupingBy(r -> r.getItem().getIdItem()));

    return porItem.entrySet().stream()
            .map(entry -> {

                List<Respuesta> grupo = entry.getValue();

                // Todas las respuestas pertenecen al mismo item
                com.gaslac.sistema_encuestas.modules.entity.Item item =
                        grupo.get(0).getItem();

                double promedio = redondear(
                        grupo.stream()
                                .mapToInt(r -> parsearEscala(r.getValor()))
                                .average()
                                .orElse(0.0)
                );

                String codigo = item.getDimension() != null
                        && item.getDimension().getCodigo() != null
                        && !item.getDimension().getCodigo().isBlank()
                        ? item.getDimension().getCodigo()
                        : "SIN_CODIGO";

                Integer numero = item.getNumero();

                String descripcion = item.getTexto() != null
                        ? item.getTexto()
                        : "";

                String estado = clasificarEstadoItem(promedio);

                return new RankingPreguntaDTO(
                        codigo,
                        numero,
                        descripcion,
                        promedio,
                        estado
                );
            })
            .sorted(
                    Comparator.comparingDouble(RankingPreguntaDTO::getPromedio)
                            .reversed()
                            .thenComparing(RankingPreguntaDTO::getDescripcion)
            )
            .collect(Collectors.toList());
}

    // ─────────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────────

    /** Parsea el valor de una respuesta ESCALA. Retorna null si es inválido o fuera de [1,5]. */
    private Integer parsearEscala(String valor) {
        if (valor == null) return null;
        try {
            int v = Integer.parseInt(valor.trim());
            return (v >= 1 && v <= 5) ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Clasifica el promedio individual de un egresado en una categoría de sentimiento. */
    private String clasificarSentimiento(double promedio) {
        if (promedio >= 4.5) return "Muy Satisfecho";
        if (promedio >= 3.5) return "Satisfecho";
        if (promedio >= 2.5) return "Neutral";
        if (promedio >= 1.5) return "Insatisfecho";
        return "Muy Insatisfecho";
    }

    /** Asigna el estado de calidad a un ítem según su promedio. */
    private String clasificarEstadoItem(double promedio) {
        if (promedio >= 4.5) return "Excelente";
        if (promedio >= 3.5) return "Bueno";
        if (promedio >= 2.5) return "Regular";
        return "Crítico";
    }

    /** Calcula el promedio de satisfacción por escuela (para mejor/peor). */
    private Map<String, Double> calcularPromediosPorEscuela(List<Respuesta> escalaValidas) {
        Map<String, List<Integer>> porEscuela = escalaValidas.stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            String ep = r.getUsuario().getEscuelaProfesional();
                            return (ep != null && !ep.isBlank()) ? ep : "SIN ESCUELA";
                        },
                        Collectors.mapping(r -> parsearEscala(r.getValor()), Collectors.toList())
                ));

        return porEscuela.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0)
                ));
    }

    /** Redondea un double a 2 decimales con half-up. */
    private double redondear(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Normaliza un texto para comparación robusta:
     * convierte a minúsculas y elimina diacríticos (tildes, ñ → n, etc.)
     * Así "INGENIERÍA" y "INGENIERIA" se comparan igual.
     */
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String lower = texto.toLowerCase();
        // Descomponer caracteres Unicode y quitar marcas diacríticas
        String normalized = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}", "");
    }

    /** Construye la respuesta vacía cuando no hay datos. */
    private DashboardResponseDTO buildEmptyResponse() {
        List<SentimientoDTO> sentimientoVacio = CATEGORIAS_SENTIMIENTO.stream()
                .map(cat -> new SentimientoDTO(cat, 0.00))
                .collect(Collectors.toList());

        KpiDashboardDTO kpisVacios = new KpiDashboardDTO(
                0, 0, 0.0, null, null, 0.0
        );

        return new DashboardResponseDTO(kpisVacios, List.of(), sentimientoVacio, List.of());
    }
}
