package com.gaslac.sistema_encuestas.modules.service;

import com.gaslac.sistema_encuestas.modules.dto.SeguimientoDTO.*;
import com.gaslac.sistema_encuestas.modules.entity.*;
import com.gaslac.sistema_encuestas.modules.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeguimientoService {
    private final UsuarioRepository usuarios;
    private final EncuestaRepository encuestas;
    private final RespuestaRepository respuestas;

    public Pagina consultar(String publico, Integer idEncuesta, String facultad, String escuela,
                            String cohorte, String estado, String busqueda, int page, int size) {
        String tipo = opcion(publico, Set.of("EGRESADO", "EMPLEADOR", "SIN_CLASIFICAR"), "publico");
        String situacion = opcion(estado, Set.of("COMPLETO", "PENDIENTE", "SIN_ENVIOS", "NO_APLICA"), "estado");
        if (page < 0 || size < 1 || size > 200) throw error("page debe ser >= 0 y size entre 1 y 200");
        List<Encuesta> catalogo = catalogo(idEncuesta);
        Map<Integer, Map<Integer, LocalDateTime>> envios = envios();
        List<Persona> filas = usuarios.findAll().stream()
                .filter(u -> tipo.isEmpty() || clasificar(u.getDni()).equals(tipo))
                .filter(u -> coincide(u.getFacultad(), facultad) && coincide(u.getEscuelaProfesional(), escuela)
                        && coincide(u.getSemestre_egreso(), cohorte))
                .filter(u -> (vacio(facultad) && vacio(escuela) && vacio(cohorte)) || clasificar(u.getDni()).equals("EGRESADO"))
                .map(u -> persona(u, catalogo, envios.getOrDefault(u.getIdUsuario(), Map.of())))
                .filter(p -> normalizar(p.nombre() + " " + p.documento()).contains(normalizar(busqueda)))
                .filter(p -> situacion.isEmpty() || p.estado().equals(situacion))
                .sorted(Comparator.comparing(Persona::nombre, String.CASE_INSENSITIVE_ORDER).thenComparing(Persona::idUsuario))
                .toList();
        long inicio = (long) page * size;
        List<PersonaResumen> contenido = inicio >= filas.size() ? List.of()
                : filas.subList((int) inicio, (int) Math.min(inicio + size, filas.size())).stream()
                        .map(this::resumenPersona)
                        .toList();
        return new Pagina(resumir(filas), contenido, page, size, filas.size(),
                (int) ((filas.size() + (long) size - 1) / size));
    }

    public Persona detalle(Integer idUsuario, Integer idEncuesta) {
        Usuario usuario = usuarios.findById(idUsuario).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Map<Integer, LocalDateTime> enviosUsuario = new HashMap<>();
        for (RespuestaRepository.EnvioResumen envio : respuestas.resumirEnviosPorUsuario(idUsuario)) {
            enviosUsuario.put(envio.getIdEncuesta(), envio.getUltimaRespuesta());
        }
        return persona(usuario, catalogo(idEncuesta), enviosUsuario);
    }

    private List<Encuesta> catalogo(Integer idEncuesta) {
        if (idEncuesta == null) return encuestas.findAll();
        return List.of(encuestas.findById(idEncuesta).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Encuesta no encontrada")));
    }

    private Map<Integer, Map<Integer, LocalDateTime>> envios() {
        Map<Integer, Map<Integer, LocalDateTime>> resultado = new HashMap<>();
        for (RespuestaRepository.EnvioResumen envio : respuestas.resumirEnvios()) {
            resultado.computeIfAbsent(envio.getIdUsuario(), id -> new HashMap<>())
                    .put(envio.getIdEncuesta(), envio.getUltimaRespuesta());
        }
        return resultado;
    }

    private Persona persona(Usuario u, List<Encuesta> catalogo, Map<Integer, LocalDateTime> envios) {
        String tipo = clasificar(u.getDni());
        List<EncuestaDetalle> detalle = catalogo.stream().filter(e -> aplica(u, tipo, e))
                .sorted(Comparator.comparing(Encuesta::getIdEncuesta))
                .map(e -> new EncuestaDetalle(e.getIdEncuesta(), e.getNombre(),
                        envios.containsKey(e.getIdEncuesta()), envios.get(e.getIdEncuesta()))).toList();
        int total = detalle.size();
        int respondidas = (int) detalle.stream().filter(EncuestaDetalle::respondida).count();
        String estado = calcularEstado(respondidas, total);
        LocalDateTime ultima = detalle.stream().map(EncuestaDetalle::ultimaRespuesta)
                .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        return new Persona(u.getIdUsuario(), u.getDni(), nombre(u), tipo, u.getFacultad(),
                u.getEscuelaProfesional(), u.getSemestre_egreso(), total, respondidas,
                total - respondidas, porcentaje(respondidas, total), estado, ultima, detalle);
    }

    private PersonaResumen resumenPersona(Persona persona) {
        return new PersonaResumen(persona.idUsuario(), persona.documento(), persona.nombre(),
                persona.publico(), persona.facultad(), persona.escuelaProfesional(), persona.cohorte(),
                persona.aplicables(), persona.respondidas(), persona.pendientes(), persona.porcentaje());
    }

    private boolean aplica(Usuario u, String tipo, Encuesta e) {
        if (tipo.equals("SIN_CLASIFICAR") || !normalizar(e.getCargo()).equals(normalizar(tipo))) return false;
        if (tipo.equals("EMPLEADOR")) return true;
        
        if (vacio(u.getFechaEgreso())) return true;
        try {
            String fecha = u.getFechaEgreso().trim();
            LocalDate egreso = LocalDate.parse(fecha.length() >= 10 ? fecha.substring(0, 10) : fecha);
            long dias = java.time.temporal.ChronoUnit.DAYS.between(egreso, LocalDate.now(ZoneId.of("America/Lima")));
            long anios = (long) Math.floor(dias / 365.25);
            return anios >= Math.min(e.getInicio_rango(), e.getFin_rango())
                    && anios <= Math.max(e.getInicio_rango(), e.getFin_rango());
        } catch (java.time.format.DateTimeParseException ex) {
            return false;
        }
    }

    private Resumen resumir(List<Persona> filas) {
        long aplicables = filas.stream().mapToLong(Persona::aplicables).sum();
        long respondidas = filas.stream().mapToLong(Persona::respondidas).sum();
        return new Resumen(filas.size(), contarTipo(filas, "EGRESADO"), contarTipo(filas, "EMPLEADOR"),
                contarTipo(filas, "SIN_CLASIFICAR"), contarEstado(filas, "COMPLETO"),
                filas.stream().filter(p -> p.pendientes() > 0).count(), contarEstado(filas, "SIN_ENVIOS"),
                contarEstado(filas, "NO_APLICA"), aplicables, respondidas, aplicables - respondidas,
                porcentaje(respondidas, aplicables));
    }
    private long contarTipo(List<Persona> filas, String tipo) { return filas.stream().filter(p -> p.publico().equals(tipo)).count(); }
    private long contarEstado(List<Persona> filas, String estado) { return filas.stream().filter(p -> p.estado().equals(estado)).count(); }
    static String clasificar(String documento) {
        String valor = documento == null ? "" : documento.trim();
        return valor.matches("[0-9]{8}") ? "EGRESADO" : valor.matches("[0-9]{11}") ? "EMPLEADOR" : "SIN_CLASIFICAR";
    }
    static String calcularEstado(long respondidas, long aplicables) {
        return aplicables == 0 ? "NO_APLICA" : respondidas == aplicables ? "COMPLETO"
                : respondidas == 0 ? "SIN_ENVIOS" : "PENDIENTE";
    }
    static Double porcentaje(long parte, long total) {
        return total == 0 ? null : Math.round(parte * 10000.0 / total) / 100.0;
    }
    private String nombre(Usuario u) {
        return String.join(" ", Objects.toString(u.getName(), ""), Objects.toString(u.getPaternalSurname(), ""),
                Objects.toString(u.getMaternalSurname(), "")).trim().replaceAll("\\s+", " ");
    }
    private boolean coincide(String valor, String filtro) { return vacio(filtro) || normalizar(valor).equals(normalizar(filtro)); }
    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
    private String normalizar(String valor) {
        return Normalizer.normalize(Objects.toString(valor, "").trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
    private String opcion(String valor, Set<String> permitidos, String campo) {
        if (vacio(valor)) return "";
        String opcion = valor.trim().toUpperCase(Locale.ROOT);
        if (!permitidos.contains(opcion)) throw error("Valor inválido para " + campo);
        return opcion;
    }
    private ResponseStatusException error(String mensaje) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje); }
}
