package com.restaurante.reservas.controller;

import com.restaurante.reservas.model.Usuario;
import com.restaurante.reservas.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UsuarioRepository usuarioRepository;

    public HomeController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        Usuario usuario = usuarioRepository.findByDni(auth.getName()).orElseThrow();
        if (usuario.getRol().name().equals("ADMIN")) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/cliente/inicio";
    }
}
