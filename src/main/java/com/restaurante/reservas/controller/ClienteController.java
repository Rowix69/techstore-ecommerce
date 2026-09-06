package com.restaurante.reservas.controller;

import com.restaurante.reservas.model.*;
import com.restaurante.reservas.repository.*;
import com.restaurante.reservas.service.CartaService;
import com.restaurante.reservas.service.MesaService;
import com.restaurante.reservas.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final MesaService mesaService;
    private final CartaService cartaService;
    private final ReservaService reservaService;
    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;

    public ClienteController(MesaService mesaService, CartaService cartaService, ReservaService reservaService,
                              UsuarioRepository usuarioRepository, MesaRepository mesaRepository) {
        this.mesaService = mesaService;
        this.cartaService = cartaService;
        this.reservaService = reservaService;
        this.usuarioRepository = usuarioRepository;
        this.mesaRepository = mesaRepository;
    }

    private Usuario usuarioActual(Authentication auth) {
        return usuarioRepository.findByDni(auth.getName()).orElseThrow();
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("entrantes", cartaService.entrantes());
        model.addAttribute("principales", cartaService.principales());
        model.addAttribute("postres", cartaService.postres());
        return "cliente/inicio";
    }

    @GetMapping("/eventos")
    public String eventos() {
        return "cliente/eventos";
    }

    @GetMapping("/mesas")
    public String verMesas(Authentication auth, Model model) {
        Usuario cliente = usuarioActual(auth);
        model.addAttribute("mesas", mesaService.listarTodas());
        model.addAttribute("reservaActiva", reservaService.buscarReservaActiva(cliente).orElse(null));
        model.addAttribute("preSeleccionables", cartaService.preSeleccionables());
        return "cliente/mesas";
    }

    @PostMapping("/reservar-mesa")
    public String reservarMesa(Authentication auth,
                                @RequestParam Long mesaId,
                                @RequestParam("fecha") String fechaStr,
                                @RequestParam("hora") String horaStr,
                                @RequestParam Integer personas,
                                @RequestParam(required = false) String ocasion,
                                @RequestParam(required = false) List<Long> platoIds,
                                @RequestParam(required = false) List<String> restricciones,
                                @RequestParam(required = false) String notas,
                                RedirectAttributes redirectAttributes) {
        Usuario cliente = usuarioActual(auth);
        try {
            LocalDateTime fechaHora = LocalDateTime.of(LocalDate.parse(fechaStr), LocalTime.parse(horaStr));
            reservaService.crearReservaConDetalle(cliente, mesaId, fechaHora, personas, ocasion, platoIds, restricciones, notas);
            return "redirect:/cliente/mireserva";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cliente/mesas";
        }
    }

    @GetMapping("/reservar/{mesaId}")
    public String formularioCarta(@PathVariable Long mesaId, Model model) {
        Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();
        model.addAttribute("mesa", mesa);
        model.addAttribute("principales", cartaService.principales());
        model.addAttribute("postres", cartaService.postres());
        model.addAttribute("bebidas", cartaService.bebidas());
        return "cliente/carta";
    }

    @PostMapping("/reservar")
    public String reservar(Authentication auth,
                            @RequestParam Long mesaId,
                            @RequestParam Long platoPrincipalId,
                            @RequestParam(required = false) Long platoPostreId,
                            @RequestParam(required = false) Long platoBebidaId,
                            Model model) {
        Usuario cliente = usuarioActual(auth);
        try {
            reservaService.crearReserva(cliente, mesaId, platoPrincipalId, platoPostreId, platoBebidaId);
            return "redirect:/cliente/mireserva";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();
            model.addAttribute("mesa", mesa);
            model.addAttribute("principales", cartaService.principales());
            model.addAttribute("postres", cartaService.postres());
            model.addAttribute("bebidas", cartaService.bebidas());
            return "cliente/carta";
        }
    }

    @GetMapping("/mireserva")
    public String miReserva(Authentication auth, Model model) {
        Usuario cliente = usuarioActual(auth);
        Optional<Reserva> reserva = reservaService.buscarReservaActiva(cliente);
        model.addAttribute("reserva", reserva.orElse(null));
        return "cliente/mireserva";
    }

    @PostMapping("/mireserva/cancelar/{id}")
    public String cancelar(Authentication auth, @PathVariable Long id) {
        reservaService.cancelarReserva(id, usuarioActual(auth));
        return "redirect:/cliente/mesas";
    }

    @GetMapping("/perfil")
    public String perfil(Authentication auth, Model model) {
        model.addAttribute("usuario", usuarioActual(auth));
        return "cliente/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(Authentication auth,
                                    @RequestParam String nombre,
                                    @RequestParam(required = false) String telefono,
                                    Model model) {
        Usuario cliente = usuarioActual(auth);
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        usuarioRepository.save(cliente);
        model.addAttribute("usuario", cliente);
        model.addAttribute("guardado", true);
        return "cliente/perfil";
    }
}
