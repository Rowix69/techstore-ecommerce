package com.restaurante.reservas.repository;

import com.restaurante.reservas.model.EstadoMesa;
import com.restaurante.reservas.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByEstado(EstadoMesa estado);
    List<Mesa> findAllByOrderByNumeroAsc();
}
