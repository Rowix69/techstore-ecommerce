package com.restaurante.reservas.controller;

import com.restaurante.reservas.model.*;
import com.restaurante.reservas.repository.*;
import com.restaurante.reservas.service.CartaService;
import com.restaurante.reservas.service.MesaService;
import com.restaurante.reservas.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String SESSION_WIZARD_KEY = "reservaWizard";
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FECHA_VISTA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("principales", cartaService.principales());
        model.addAttribute("postres", cartaService.postres());
        model.addAttribute("bebidas", cartaService.bebidas());
        return "cliente/inicio";
    }

    @GetMapping("/mesas")
    public String verMesas(Authentication auth, Model model) {
        Usuario cliente = usuarioActual(auth);
        model.addAttribute("mesas", mesaService.listarTodas());
        model.addAttribute("reservaActiva", reservaService.buscarReservaActiva(cliente).orElse(null));
        model.addAttribute("principales", cartaService.principales());
        model.addAttribute("postres", cartaService.postres());
        model.addAttribute("bebidas", cartaService.bebidas());
        return "cliente/mesas";
    }

    // Paso 1 y 2 del asistente (¿Cuándo vienen? + Preferencias) se completan en un modal
    // dentro de cliente/mesas.html. Al confirmar el paso 3 del modal, se guarda todo en
    // sesión y se redirige a "Arma tu pedido", que no cambia.
    @PostMapping("/reservar/iniciar")
    public String iniciarReserva(@RequestParam Long mesaId,
                                  @RequestParam String fecha,
                                  @RequestParam String hora,
                                  @RequestParam Integer comensales,
                                  @RequestParam(required = false, defaultValue = "Ninguna") String ocasion,
                                  @RequestParam(required = false) List<String> restricciones,
                                  @RequestParam(required = false) String notas,
                                  HttpSession session) {
        Map<String, Object> wizard = new HashMap<>();
        wizard.put("mesaId", mesaId);
        wizard.put("fecha", fecha);
        wizard.put("hora", hora);
        wizard.put("comensales", comensales);
        wizard.put("ocasion", ocasion);
        wizard.put("restricciones", restricciones == null ? List.of() : restricciones);
        wizard.put("notas", notas);
        session.setAttribute(SESSION_WIZARD_KEY, wizard);
        return "redirect:/cliente/reservar/" + mesaId;
    }

    @SuppressWarnings("unchecked")
    private void agregarDatosWizard(Model model, Long mesaId, HttpSession session) {
        Map<String, Object> wizard = (Map<String, Object>) session.getAttribute(SESSION_WIZARD_KEY);
        if (wizard != null && mesaId.equals(wizard.get("mesaId"))) {
            model.addAttribute("resFecha", wizard.get("fecha"));
            String fechaFormateada;
            try {
                fechaFormateada = LocalDate.parse((String) wizard.get("fecha"), FECHA_FMT).format(FECHA_VISTA_FMT);
            } catch (Exception e) {
                fechaFormateada = (String) wizard.get("fecha");
            }
            model.addAttribute("resFechaFormateada", fechaFormateada);
            model.addAttribute("resHora", wizard.get("hora"));
            model.addAttribute("resComensales", wizard.get("comensales"));
            model.addAttribute("resOcasion", wizard.get("ocasion"));
            model.addAttribute("resRestricciones", wizard.get("restricciones"));
        }
    }

    @GetMapping("/reservar/{mesaId}")
    public String formularioCarta(@PathVariable Long mesaId, Model model, HttpSession session) {
        Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();
        model.addAttribute("mesa", mesa);
        model.addAttribute("principales", cartaService.principales());
        model.addAttribute("postres", cartaService.postres());
        model.addAttribute("bebidas", cartaService.bebidas());
        agregarDatosWizard(model, mesaId, session);
        return "cliente/carta";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/reservar")
    public String reservar(Authentication auth,
                            @RequestParam Long mesaId,
                            @RequestParam Long platoPrincipalId,
                            @RequestParam(required = false) Long platoPostreId,
                            @RequestParam(required = false) Long platoBebidaId,
                            HttpSession session,
                            Model model) {
        Usuario cliente = usuarioActual(auth);
        Map<String, Object> wizard = (Map<String, Object>) session.getAttribute(SESSION_WIZARD_KEY);

        LocalDateTime fechaHora = null;
        Integer comensales = null;
        String ocasion = null;
        String restricciones = null;
        String notas = null;

        if (wizard != null && mesaId.equals(wizard.get("mesaId"))) {
            try {
                LocalDate fecha = LocalDate.parse((String) wizard.get("fecha"), FECHA_FMT);
                LocalTime hora = LocalTime.parse((String) wizard.get("hora"));
                fechaHora = LocalDateTime.of(fecha, hora);
            } catch (Exception ignored) {
                // si la fecha/hora no es válida, se usará la fecha actual por defecto
            }
            comensales = (Integer) wizard.get("comensales");
            ocasion = (String) wizard.get("ocasion");
            List<String> restriccionesList = (List<String>) wizard.get("restricciones");
            if (restriccionesList != null && !restriccionesList.isEmpty()) {
                restricciones = String.join(", ", restriccionesList);
            }
            notas = (String) wizard.get("notas");
        }

        try {
            reservaService.crearReserva(cliente, mesaId, platoPrincipalId, platoPostreId, platoBebidaId,
                    fechaHora, comensales, ocasion, restricciones, notas);
            session.removeAttribute(SESSION_WIZARD_KEY);
            return "redirect:/cliente/mireserva";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();
            model.addAttribute("mesa", mesa);
            model.addAttribute("principales", cartaService.principales());
            model.addAttribute("postres", cartaService.postres());
            model.addAttribute("bebidas", cartaService.bebidas());
            agregarDatosWizard(model, mesaId, session);
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
