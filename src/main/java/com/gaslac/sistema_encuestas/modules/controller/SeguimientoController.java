package com.gaslac.sistema_encuestas.modules.controller;

import com.gaslac.sistema_encuestas.modules.dto.SeguimientoDTO.*;
import com.gaslac.sistema_encuestas.modules.service.SeguimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/seguimiento")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SeguimientoController {
    private final SeguimientoService seguimiento;

    @GetMapping
    public Pagina consultar(@RequestParam(required = false) String publico,
                            @RequestParam(required = false) Integer idEncuesta,
                            @RequestParam(required = false) String facultad,
                            @RequestParam(required = false) String escuelaProfesional,
                            @RequestParam(required = false) String cohorte,
                            @RequestParam(required = false) String estado,
                            @RequestParam(required = false) String busqueda,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return seguimiento.consultar(publico, idEncuesta, facultad, escuelaProfesional,
                cohorte, estado, busqueda, page, size);
    }

    @GetMapping("/{idUsuario}")
    public Persona detalle(@PathVariable Integer idUsuario, @RequestParam(required = false) Integer idEncuesta) {
        return seguimiento.detalle(idUsuario, idEncuesta);
    }
}
