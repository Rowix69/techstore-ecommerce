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
            String[] zonas = {"Terraza", "Interior", "Salón Principal"};
            for (int i = 1; i <= 8; i++) {
                mesaRepository.save(new Mesa(i, (i % 2 == 0) ? 4 : 2, zonas[i % zonas.length]));
            }
        }

        // Carta de ejemplo
        if (platoRepository.count() == 0) {
            platoRepository.save(new Plato("Tartar de Atún", "Atún rojo, aguacate, yuzu", new BigDecimal("18.00"), CategoriaPlato.ENTRANTE));
            platoRepository.save(new Plato("Croquetas Ibéricas", "Jamón 100% ibérico", new BigDecimal("14.00"), CategoriaPlato.ENTRANTE));

            platoRepository.save(new Plato("Salmón a la Plancha", "Eneldo, puré de chirivía", new BigDecimal("26.00"), CategoriaPlato.PRINCIPAL));
            platoRepository.save(new Plato("Entrecot Madurado", "45 días, mantequilla de trufa", new BigDecimal("34.00"), CategoriaPlato.PRINCIPAL));

            platoRepository.save(new Plato("Coulant de Chocolate", "Chocolate 70%, helado vainilla", new BigDecimal("10.00"), CategoriaPlato.POSTRE));
            platoRepository.save(new Plato("Tarta de Queso", "Estilo vasco, frutos rojos", new BigDecimal("9.00"), CategoriaPlato.POSTRE));

            platoRepository.save(new Plato("Agua con Gas", "500 ml", new BigDecimal("4.00"), CategoriaPlato.BEBIDA));
            platoRepository.save(new Plato("Copa de Vino de la Casa", "Tinto o blanco", new BigDecimal("6.00"), CategoriaPlato.BEBIDA));
        }
    }
}
