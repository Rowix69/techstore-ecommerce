package com.restaurante.reservas.controller;

import com.restaurante.reservas.model.Rol;
import com.restaurante.reservas.model.Usuario;
import com.restaurante.reservas.repository.UsuarioRepository;
import com.restaurante.reservas.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = ClienteController.class)
public class NavbarAdvice {

    private final UsuarioRepository usuarioRepository;
    private final ReservaService reservaService;

    public NavbarAdvice(UsuarioRepository usuarioRepository, ReservaService reservaService) {
        this.usuarioRepository = usuarioRepository;
        this.reservaService = reservaService;
    }

    @ModelAttribute("misReservasCount")
    public int misReservasCount(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return 0;
        Usuario usuario = usuarioRepository.findByDni(auth.getName()).orElse(null);
        if (usuario == null || usuario.getRol() != Rol.CLIENTE) return 0;
        return reservaService.buscarReservaActiva(usuario).isPresent() ? 1 : 0;
    }
}
