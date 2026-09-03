package com.restaurante.reservas.service;

import com.restaurante.reservas.model.CategoriaPlato;
import com.restaurante.reservas.model.Plato;
import com.restaurante.reservas.repository.PlatoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartaService {

    private final PlatoRepository platoRepository;

    public CartaService(PlatoRepository platoRepository) {
        this.platoRepository = platoRepository;
    }

    public List<Plato> principales() { return platoRepository.findByCategoria(CategoriaPlato.PRINCIPAL); }
    public List<Plato> postres() { return platoRepository.findByCategoria(CategoriaPlato.POSTRE); }
    public List<Plato> bebidas() { return platoRepository.findByCategoria(CategoriaPlato.BEBIDA); }
    public List<Plato> todos() { return platoRepository.findAll(); }
    public Plato guardar(Plato p) { return platoRepository.save(p); }
    public void eliminar(Long id) { platoRepository.deleteById(id); }
}
