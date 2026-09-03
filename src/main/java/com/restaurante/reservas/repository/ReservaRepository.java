package com.restaurante.reservas.repository;

import com.restaurante.reservas.model.EstadoReserva;
import com.restaurante.reservas.model.Reserva;
import com.restaurante.reservas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findFirstByClienteAndEstadoIn(Usuario cliente, List<EstadoReserva> estados);

    List<Reserva> findByClienteOrderByFechaHoraDesc(Usuario cliente);

    List<Reserva> findAllByOrderByFechaHoraDesc();
}
