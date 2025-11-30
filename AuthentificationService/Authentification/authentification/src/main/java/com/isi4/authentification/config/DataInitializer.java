package com.isi4.authentification.config;

import com.isi4.authentification.entity.User;
import com.isi4.authentification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Toujours recréer les utilisateurs de test avec les bons mots de passe
        
        // Supprimer les anciens utilisateurs de test s'ils existent
        userRepository.findByEmail("admin@plannora.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("enseignant@plannora.com").ifPresent(userRepository::delete);
        
        // Créer l'admin
        User admin = new User();
        admin.setId("admin001");
        admin.setEmail("admin@plannora.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setNom("Admin");
        admin.setPrenom("Système");
        admin.setTelephone("0000000000");
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        
        // Créer l'enseignant
        User enseignant = new User();
        enseignant.setId("ens001");
        enseignant.setEmail("enseignant@plannora.com");
        enseignant.setPassword(passwordEncoder.encode("password123"));
        enseignant.setNom("Dupont");
        enseignant.setPrenom("Jean");
        enseignant.setTelephone("0612345678");
        enseignant.setRole(User.Role.ENSEIGNANT);
        userRepository.save(enseignant);
        
        System.out.println("✅ Utilisateurs de test créés avec succès!");
        System.out.println("📧 admin@plannora.com / password123");
        System.out.println("📧 enseignant@plannora.com / password123");
    }
}
