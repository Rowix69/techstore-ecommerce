package com.restaurante.reservas.service;

import com.restaurante.reservas.model.Mesa;
import com.restaurante.reservas.repository.MesaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public List<Mesa> listarTodas() { return mesaRepository.findAllByOrderByNumeroAsc(); }
    public Mesa guardar(Mesa m) { return mesaRepository.save(m); }
    public void eliminar(Long id) { mesaRepository.deleteById(id); }
}
