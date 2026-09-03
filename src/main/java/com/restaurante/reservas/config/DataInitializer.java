package com.restaurante.reservas.config;

import com.restaurante.reservas.model.*;
import com.restaurante.reservas.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MesaRepository mesaRepository;
    private final PlatoRepository platoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, MesaRepository mesaRepository,
                            PlatoRepository platoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.mesaRepository = mesaRepository;
        this.platoRepository = platoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Usuario admin por defecto
        if (usuarioRepository.findByDni("00000000").isEmpty()) {
            usuarioRepository.save(new Usuario("00000000", passwordEncoder.encode("admin123"), "Administrador", Rol.ADMIN));
        }

        // Usuario cliente de prueba
        if (usuarioRepository.findByDni("11111111").isEmpty()) {
            Usuario clienteDemo = new Usuario("11111111", passwordEncoder.encode("cliente123"), "Cliente Demo", Rol.CLIENTE);
            clienteDemo.setTelefono("+51 987 654 321");
            usuarioRepository.save(clienteDemo);
        }

        // Mesas de ejemplo
        if (mesaRepository.count() == 0) {
            for (int i = 1; i <= 8; i++) {
                mesaRepository.save(new Mesa(i, (i % 2 == 0) ? 4 : 2));
            }
        }

        // Carta de ejemplo
        if (platoRepository.count() == 0) {
            platoRepository.save(new Plato("Lomo Saltado", "Clásico peruano con papas fritas", new BigDecimal("32.00"), CategoriaPlato.PRINCIPAL));
            platoRepository.save(new Plato("Arroz con Pollo", "Arroz verde con pollo y papa a la huancaína", new BigDecimal("28.00"), CategoriaPlato.PRINCIPAL));
            platoRepository.save(new Plato("Tallarines Verdes", "Con bistec apanado", new BigDecimal("30.00"), CategoriaPlato.PRINCIPAL));

            platoRepository.save(new Plato("Suspiro a la Limeña", "Postre tradicional", new BigDecimal("12.00"), CategoriaPlato.POSTRE));
            platoRepository.save(new Plato("Mazamorra Morada", "Postre a base de maíz morado", new BigDecimal("10.00"), CategoriaPlato.POSTRE));

            platoRepository.save(new Plato("Chicha Morada", "Bebida tradicional", new BigDecimal("8.00"), CategoriaPlato.BEBIDA));
            platoRepository.save(new Plato("Gaseosa", "500 ml", new BigDecimal("6.00"), CategoriaPlato.BEBIDA));
        }
    }
}
