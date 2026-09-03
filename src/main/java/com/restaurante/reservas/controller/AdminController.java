package com.restaurante.reservas.controller;

import com.restaurante.reservas.model.*;
import com.restaurante.reservas.repository.UsuarioRepository;
import com.restaurante.reservas.service.CartaService;
import com.restaurante.reservas.service.MesaService;
import com.restaurante.reservas.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MesaService mesaService;
    private final CartaService cartaService;
    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;

    public AdminController(MesaService mesaService, CartaService cartaService,
                            ReservaService reservaService, UsuarioRepository usuarioRepository) {
        this.mesaService = mesaService;
        this.cartaService = cartaService;
        this.reservaService = reservaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("admin", usuarioRepository.findByDni(auth.getName()).orElseThrow());
        model.addAttribute("totalMesas", mesaService.listarTodas().size());
        model.addAttribute("totalReservas", reservaService.listarTodas().size());
        return "admin/dashboard";
    }

    // ---- Mesas ----
    @GetMapping("/mesas")
    public String mesas(Model model) {
        model.addAttribute("mesas", mesaService.listarTodas());
        model.addAttribute("mesaNueva", new Mesa());
        return "admin/mesas";
    }

    @PostMapping("/mesas")
    public String crearMesa(@RequestParam Integer numero, @RequestParam Integer capacidad) {
        mesaService.guardar(new Mesa(numero, capacidad));
        return "redirect:/admin/mesas";
    }

    @PostMapping("/mesas/eliminar/{id}")
    public String eliminarMesa(@PathVariable Long id) {
        mesaService.eliminar(id);
        return "redirect:/admin/mesas";
    }

    // ---- Carta / Platos ----
    @GetMapping("/platos")
    public String platos(Model model) {
        model.addAttribute("platos", cartaService.todos());
        model.addAttribute("categorias", CategoriaPlato.values());
        return "admin/platos";
    }

    @PostMapping("/platos")
    public String crearPlato(@RequestParam String nombre, @RequestParam String descripcion,
                              @RequestParam BigDecimal precio, @RequestParam CategoriaPlato categoria) {
        cartaService.guardar(new Plato(nombre, descripcion, precio, categoria));
        return "redirect:/admin/platos";
    }

    @PostMapping("/platos/eliminar/{id}")
    public String eliminarPlato(@PathVariable Long id) {
        cartaService.eliminar(id);
        return "redirect:/admin/platos";
    }

    // ---- Reservas ----
    @GetMapping("/reservas")
    public String reservas(Model model) {
        model.addAttribute("reservas", reservaService.listarTodas());
        model.addAttribute("mesasLibres", mesaService.listarTodas().stream()
                .filter(m -> m.getEstado() == EstadoMesa.LIBRE).toList());
        return "admin/reservas";
    }

    @PostMapping("/reservas/cancelar/{id}")
    public String cancelarReserva(Authentication auth, @PathVariable Long id) {
        reservaService.cancelarReserva(id, usuarioRepository.findByDni(auth.getName()).orElseThrow());
        return "redirect:/admin/reservas";
    }

    @PostMapping("/reservas/mover/{id}")
    public String moverReserva(@PathVariable Long id, @RequestParam Long nuevaMesaId) {
        reservaService.moverReserva(id, nuevaMesaId);
        return "redirect:/admin/reservas";
    }
}
