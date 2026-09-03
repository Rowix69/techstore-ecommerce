package com.restaurante.reservas.service;

import com.restaurante.reservas.model.*;
import com.restaurante.reservas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final PlatoRepository platoRepository;

    public ReservaService(ReservaRepository reservaRepository,
                           MesaRepository mesaRepository,
                           PlatoRepository platoRepository) {
        this.reservaRepository = reservaRepository;
        this.mesaRepository = mesaRepository;
        this.platoRepository = platoRepository;
    }

    private static final List<EstadoReserva> ESTADOS_ACTIVOS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    public Optional<Reserva> buscarReservaActiva(Usuario cliente) {
        return reservaRepository.findFirstByClienteAndEstadoIn(cliente, ESTADOS_ACTIVOS);
    }

    @Transactional
    public Reserva crearReserva(Usuario cliente, Long mesaId, Long platoPrincipalId,
                                 Long platoPostreId, Long platoBebidaId) {
        return crearReserva(cliente, mesaId, platoPrincipalId, platoPostreId, platoBebidaId,
                null, null, null, null, null);
    }

    @Transactional
    public Reserva crearReserva(Usuario cliente, Long mesaId, Long platoPrincipalId,
                                 Long platoPostreId, Long platoBebidaId,
                                 LocalDateTime fechaHora, Integer comensales, String ocasionEspecial,
                                 String restricciones, String notasEspeciales) {

        // Regla: una mesa/reserva activa por cliente
        if (buscarReservaActiva(cliente).isPresent()) {
            throw new IllegalStateException("Ya tienes una reserva activa. Cancélala antes de reservar otra mesa.");
        }

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new IllegalArgumentException("La mesa no existe."));
        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            throw new IllegalStateException("Esa mesa ya está reservada.");
        }

        // Regla: el plato principal es obligatorio
        if (platoPrincipalId == null) {
            throw new IllegalArgumentException("Debes elegir un plato principal.");
        }
        Plato principal = platoRepository.findById(platoPrincipalId)
                .orElseThrow(() -> new IllegalArgumentException("Plato principal inválido."));
        if (principal.getCategoria() != CategoriaPlato.PRINCIPAL) {
            throw new IllegalArgumentException("El plato seleccionado no es un plato principal.");
        }

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setMesa(mesa);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        if (fechaHora != null) {
            reserva.setFechaHora(fechaHora);
        }
        if (comensales != null) {
            reserva.setComensales(comensales);
        }
        reserva.setOcasionEspecial(ocasionEspecial);
        reserva.setRestricciones(restricciones);
        reserva.setNotasEspeciales(notasEspeciales);
        reserva.addDetalle(new DetalleReserva(principal, 1));

        // Postre: opcional
        if (platoPostreId != null) {
            Plato postre = platoRepository.findById(platoPostreId)
                    .orElseThrow(() -> new IllegalArgumentException("Postre inválido."));
            if (postre.getCategoria() != CategoriaPlato.POSTRE) {
                throw new IllegalArgumentException("El plato seleccionado no es un postre.");
            }
            reserva.addDetalle(new DetalleReserva(postre, 1));
        }

        // Bebida: opcional
        if (platoBebidaId != null) {
            Plato bebida = platoRepository.findById(platoBebidaId)
                    .orElseThrow(() -> new IllegalArgumentException("Bebida inválida."));
            if (bebida.getCategoria() != CategoriaPlato.BEBIDA) {
                throw new IllegalArgumentException("El plato seleccionado no es una bebida.");
            }
            reserva.addDetalle(new DetalleReserva(bebida, 1));
        }

        mesa.setEstado(EstadoMesa.RESERVADA);
        mesaRepository.save(mesa);

        return reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(Long reservaId, Usuario usuarioActual) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));

        boolean esDueno = reserva.getCliente().getId().equals(usuarioActual.getId());
        boolean esAdmin = usuarioActual.getRol() == Rol.ADMIN;
        if (!esDueno && !esAdmin) {
            throw new IllegalStateException("No tienes permiso para cancelar esta reserva.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        Mesa mesa = reserva.getMesa();
        mesa.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesa);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void moverReserva(Long reservaId, Long nuevaMesaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));

        Mesa nuevaMesa = mesaRepository.findById(nuevaMesaId)
                .orElseThrow(() -> new IllegalArgumentException("La mesa destino no existe."));
        if (nuevaMesa.getEstado() != EstadoMesa.LIBRE) {
            throw new IllegalStateException("La mesa destino no está libre.");
        }

        Mesa mesaAnterior = reserva.getMesa();
        mesaAnterior.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesaAnterior);

        nuevaMesa.setEstado(EstadoMesa.RESERVADA);
        mesaRepository.save(nuevaMesa);

        reserva.setMesa(nuevaMesa);
        reservaRepository.save(reserva);
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAllByOrderByFechaHoraDesc();
    }
}
