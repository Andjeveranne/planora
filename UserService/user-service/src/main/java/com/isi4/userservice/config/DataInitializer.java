package com.isi4.userservice.config;

import com.isi4.userservice.model.Administrateur;
import com.isi4.userservice.repository.AdministrateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            if (administrateurRepository.findById("admin001").isEmpty()) {
                Administrateur admin = new Administrateur();
                admin.setIdUser("admin001");
                admin.setMdp(passwordEncoder.encode("password123"));
                admin.setEmail("admin@plannora.com");
                admin.setNomUser("Admin");
                admin.setPrenomUser("Système");
                admin.setTelephone("0000000000");
                admin.setRole("ADMIN");
                
                administrateurRepository.save(admin);
                System.out.println("✅ Administrateur créé : admin@plannora.com / password123");
            } else {
                System.out.println("ℹ️ Administrateur existe déjà");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Administrateur existe déjà (erreur ignorée)");
        }
    }
}
