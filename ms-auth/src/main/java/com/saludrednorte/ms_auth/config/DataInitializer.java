package com.saludrednorte.ms_auth.config;

import com.saludrednorte.ms_auth.entity.User;
import com.saludrednorte.ms_auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos para poblar los usuarios por defecto al arrancar el servicio.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(new User("paciente", passwordEncoder.encode("paciente123"), "ROLE_PACIENTE"));
            userRepository.save(new User("funcionario", passwordEncoder.encode("funcionario123"), "ROLE_FUNCIONARIO"));
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN"));
            System.out.println("Usuarios por defecto inicializados correctamente en la base de datos.");
        }
    }
}
