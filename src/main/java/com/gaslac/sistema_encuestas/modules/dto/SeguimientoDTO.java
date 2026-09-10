package com.gaslac.sistema_encuestas.modules.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class SeguimientoDTO {
    private SeguimientoDTO() {}
    public record EncuestaDetalle(Integer idEncuesta, String nombre, boolean respondida,
                                  LocalDateTime ultimaRespuesta) {}
    public record Persona(Integer idUsuario, String documento, String nombre, String publico,
                          String facultad, String escuelaProfesional, String cohorte,
                          int aplicables, int respondidas, int pendientes, Double porcentaje,
                          String estado, LocalDateTime ultimaRespuesta,
                          List<EncuestaDetalle> encuestas) {}
    public record PersonaResumen(Integer idUsuario, String documento, String nombre, String publico,
                                 String facultad, String escuelaProfesional, String cohorte,
                                 int aplicables, int respondidas, int pendientes,
                                 Double porcentaje) {}
    public record Resumen(long registrados, long egresados, long empleadores, long sinClasificar,
                          long completos, long pendientes, long sinEnvios, long noAplica,
                          long encuestasAplicables, long encuestasRespondidas,
                          long encuestasPendientes, Double porcentaje) {}
    public record Pagina(Resumen resumen, List<PersonaResumen> contenido, int page, int size,
                         long totalElementos, int totalPaginas) {}
}
