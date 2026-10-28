package com.isi4.authentification.config;

import com.isi4.authentification.entity.User;
import com.isi4.authentification.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        ensureUser("admin001", "admin.systeme@ictuniversity.org", "Systeme", "Admin", "0000000000", User.Role.ADMIN);
        ensureUser("ens001", "nkiamboh.tanwi@ictuniversity.org", "Tanwi", "Nkiamboh", "650000001", User.Role.ENSEIGNANT);
        ensureUser("ens002", "marie.mba@ictuniversity.org", "Mba", "Marie", "650000002", User.Role.ENSEIGNANT);
        ensureUser("ens003", "alain.dibongo@ictuniversity.org", "Dibongo", "Alain", "650000003", User.Role.ENSEIGNANT);
        ensureUser("ens004", "grace.ekeme@ictuniversity.org", "Ekeme", "Grace", "650000004", User.Role.ENSEIGNANT);
        ensureUser("ens005", "pierre.ndjock@ictuniversity.org", "Ndjock", "Pierre", "650000005", User.Role.ENSEIGNANT);
        ensureUser("ens006", "sophie.mokam@ictuniversity.org", "Mokam", "Sophie", "650000006", User.Role.ENSEIGNANT);
        ensureUser("ens007", "jean.mbem@ictuniversity.org", "Mbem", "Jean", "650000007", User.Role.ENSEIGNANT);
        ensureUser("ens008", "louise.tchinda@ictuniversity.org", "Tchinda", "Louise", "650000008", User.Role.ENSEIGNANT);
        ensureUser("ens009", "daniel.nguefack@ictuniversity.org", "Nguefack", "Daniel", "650000009", User.Role.ENSEIGNANT);
        ensureUser("ens010", "charles.ekoume@ictuniversity.org", "Ekoume", "Charles", "650000010", User.Role.ENSEIGNANT);
        ensureUser("ens011", "john.martin@ictuniversity.org", "Martin", "John", "650000011", User.Role.ENSEIGNANT);
        ensureUser("etu001", "etudiant.test@ictuniversity.org", "Test", "Etudiant", "690000001", User.Role.ETUDIANT);
        ensureUser("etu002", "etudiant.deux@ictuniversity.org", "Deux", "Etudiant", "690000002", User.Role.ETUDIANT);

        System.out.println(" DataInitializer completed successfully!");
        System.out.println(" Available test accounts:");
        System.out.println("   admin.systeme@ictuniversity.org / password123");
        System.out.println("   nkiamboh.tanwi@ictuniversity.org / password123");
        System.out.println("   marie.mba@ictuniversity.org / password123");
        System.out.println("   alain.dibongo@ictuniversity.org / password123");
        System.out.println("   etudiant.test@ictuniversity.org / password123");
        System.out.println("   etudiant.deux@ictuniversity.org / password123");
    }

    private void ensureUser(String id, String email, String nom, String prenom, String telephone, User.Role role) {
        if (userRepository.findById(id).isPresent() || userRepository.existsByEmail(email)) {
            System.out.println("ℹ User already exists: " + email + " (skipping)");
            return;
        }

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setTelephone(telephone);
        user.setRole(role);
        userRepository.save(user);
        System.out.println("✅ Created user: " + email + " / password123");
    }
}
