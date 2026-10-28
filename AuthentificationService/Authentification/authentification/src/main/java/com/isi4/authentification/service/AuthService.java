package com.isi4.authentification.service;

import com.isi4.authentification.dto.LoginRequest;
import com.isi4.authentification.dto.LoginResponse;
import com.isi4.authentification.dto.RegisterRequest;
import com.isi4.authentification.entity.User;
import com.isi4.authentification.repository.UserRepository;
import com.isi4.authentification.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    private static final String ALLOWED_EMAIL_DOMAIN = "@ictuniversity.org";
    private static final String DOMAIN_ERROR_MESSAGE = "Seules les adresses email du domaine @ictuniversity.org sont autorisées";

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim();
        log.info("Tentative de connexion pour l'email: {}", email);

        if (!isValidEmailDomain(email)) {
            log.warn("Tentative de connexion avec un domaine email non autorisé: {}", email);
            throw new RuntimeException(DOMAIN_ERROR_MESSAGE);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec un email inexistant: {}", request.getEmail());
                    return new RuntimeException("Email ou mot de passe incorrect");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Tentative de connexion avec un mot de passe incorrect pour: {}", request.getEmail());
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        if (!isValidEmailDomain(user.getEmail())) {
            log.error("Utilisateur en base avec domaine email non autorisé: {}", user.getEmail());
            throw new RuntimeException("Compte non autorisé - contactez l'administrateur");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name());

        log.info("Connexion réussie pour: {} ({})", user.getEmail(), user.getRole());

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole().name());
    }

    public User register(RegisterRequest request) {
        String email = request.getEmail().trim();
        log.info("Tentative d'inscription pour l'email: {}", email);

        if (!isValidEmailDomain(email)) {
            log.warn("Tentative d'inscription avec un domaine email non autorisé: {}", email);
            throw new RuntimeException(DOMAIN_ERROR_MESSAGE);
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Tentative d'inscription avec un email déjà existant: {}", email);
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        if (!isValidInstitutionalEmailFormat(email)) {
            log.warn("Format d'email institutionnel invalide: {}", email);
            throw new RuntimeException("Format d'email invalide. Utilisez le format: prenom.nom@ictuniversity.org");
        }

        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));

        User savedUser = userRepository.save(user);
        log.info("Inscription réussie pour: {} ({})", savedUser.getEmail(), savedUser.getRole());

        return savedUser;
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public void forgotPassword(String email) {
        String normalizedEmail = email == null ? null : email.trim();
        log.info("Demande de réinitialisation de mot de passe pour: {}", normalizedEmail);

        if (!isValidEmailDomain(normalizedEmail)) {
            log.warn("Demande de réinitialisation avec un domaine email non autorisé: {}", normalizedEmail);
            throw new RuntimeException(DOMAIN_ERROR_MESSAGE);
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Demande de réinitialisation pour un email inexistant: {}", normalizedEmail);
                    return new RuntimeException("Utilisateur non trouvé avec cet email");
                });

        String token = java.util.UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(java.time.LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        System.out.println("=== LIEN DE REINITIALISATION ===");
        System.out.println(resetLink);
        System.out.println("================================");

        try {
            emailService.sendEmail(
                    user.getEmail(),
                    "Réinitialisation de mot de passe - Plannora",
                    "Bonjour " + user.getPrenom() + ",\n\n" +
                            "Vous avez demandé la réinitialisation de votre mot de passe.\n" +
                            "Cliquez sur le lien suivant pour définir un nouveau mot de passe :\n\n" +
                            resetLink + "\n\n" +
                            "Ce lien est valide pour 30 minutes.\n\n" +
                            "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.");
            log.info("Email de réinitialisation envoyé à: {}", normalizedEmail);
        } catch (Exception e) {
            log.error("Erreur envoi email de réinitialisation pour {}: {}", normalizedEmail, e.getMessage());
        }
    }

    public void resetPassword(String token, String newPassword) {
        log.info("Tentative de réinitialisation de mot de passe avec token");

        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> {
                    log.warn("Tentative de réinitialisation avec token invalide");
                    return new RuntimeException("Token invalide");
                });

        if (user.getResetPasswordTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            log.warn("Tentative de réinitialisation avec token expiré pour: {}", user.getEmail());
            throw new RuntimeException("Token expiré");
        }

        if (!isValidEmailDomain(user.getEmail())) {
            log.error("Tentative de réinitialisation pour utilisateur avec domaine non autorisé: {}", user.getEmail());
            throw new RuntimeException("Compte non autorisé - contactez l'administrateur");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        log.info("Mot de passe réinitialisé avec succès pour: {}", user.getEmail());
    }

    private boolean isValidEmailDomain(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.toLowerCase().endsWith(ALLOWED_EMAIL_DOMAIN.toLowerCase());
    }

    private boolean isValidInstitutionalEmailFormat(String email) {
        if (!isValidEmailDomain(email)) {
            return false;
        }

        String localPart = email.substring(0, email.lastIndexOf('@')).toLowerCase();

        if (localPart.startsWith("admin.") || localPart.startsWith("system.")) {
            return true;
        }

        return localPart.matches("^[a-z]+\\.[a-z]+$");
    }

    public DomainValidationInfo getDomainValidationInfo() {
        return new DomainValidationInfo(
                ALLOWED_EMAIL_DOMAIN,
                "prenom.nom@ictuniversity.org",
                "Seules les adresses email institutionnelles sont autorisées"
        );
    }

    public static class DomainValidationInfo {
        public final String allowedDomain;
        public final String exampleFormat;
        public final String message;

        public DomainValidationInfo(String allowedDomain, String exampleFormat, String message) {
            this.allowedDomain = allowedDomain;
            this.exampleFormat = exampleFormat;
            this.message = message;
        }
    }
}
