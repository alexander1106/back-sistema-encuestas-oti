package com.gaslac.sistema_encuestas.modules.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaslac.sistema_encuestas.modules.entity.Usuario;
import com.gaslac.sistema_encuestas.modules.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/usuarios", "/api/usuario"})
@RequiredArgsConstructor
@CrossOrigin("*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

@PostMapping
public Usuario guardarUsuario(@RequestBody  Usuario usuario) {

    Usuario guardado = usuarioRepository.save(usuario);

    System.out.println("ID generado: " + guardado.getIdUsuario());

    return guardado;
}
    @GetMapping("/{dni}")
public ResponseEntity<Usuario> obtenerPorDni(@PathVariable String dni) {
    return usuarioRepository.findByDni(dni)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
}
