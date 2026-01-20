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
        
        // Supprimer les anciens utilisateurs de test s'ils existent (sauf ceux avec des contraintes FK)
        userRepository.findByEmail("admin.systeme@saintjeaningenieur.org").ifPresent(user -> {
            try {
                userRepository.delete(user);
            } catch (Exception e) {
                System.out.println("⚠️ Cannot delete admin user (foreign key constraint): " + e.getMessage());
            }
        });
        
        // Ne pas supprimer jean.dupont car il a des relations dans la table enseignants
        // userRepository.findByEmail("jean.dupont@saintjeaningenieur.org").ifPresent(userRepository::delete);
        
        // Créer l'admin seulement s'il n'existe pas déjà
        if (!userRepository.existsByEmail("admin.systeme@saintjeaningenieur.org")) {
            User admin = new User();
            admin.setId("admin001");
            admin.setEmail("admin.systeme@saintjeaningenieur.org");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setNom("Systeme");
            admin.setPrenom("Admin");
            admin.setTelephone("0000000000");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ Admin user created!");
        } else {
            System.out.println("ℹ️ Admin user already exists, skipping creation.");
        }
        
        // Créer l'enseignant seulement s'il n'existe pas déjà
        if (!userRepository.existsByEmail("jean.dupont@saintjeaningenieur.org")) {
            User enseignant = new User();
            enseignant.setId("ens001");
            enseignant.setEmail("jean.dupont@saintjeaningenieur.org");
            enseignant.setPassword(passwordEncoder.encode("password123"));
            enseignant.setNom("Dupont");
            enseignant.setPrenom("Jean");
            enseignant.setTelephone("0612345678");
            enseignant.setRole(User.Role.ENSEIGNANT);
            userRepository.save(enseignant);
            System.out.println("✅ Teacher user created!");
        } else {
            System.out.println("ℹ️ Teacher user already exists, skipping creation.");
        }
        
        System.out.println("✅ DataInitializer completed successfully!");
        System.out.println("📧 Available test accounts:");
        System.out.println("   admin.systeme@saintjeaningenieur.org / password123");
        System.out.println("   jean.dupont@saintjeaningenieur.org / password123");
    }
}
