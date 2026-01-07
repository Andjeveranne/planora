package com.isi4.authentification.service;

import com.isi4.authentification.dto.LoginRequest;
import com.isi4.authentification.dto.LoginResponse;
import com.isi4.authentification.dto.RegisterRequest;
import com.isi4.authentification.entity.User;
import com.isi4.authentification.repository.UserRepository;
import com.isi4.authentification.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name());

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole().name());
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));

        return userRepository.save(user);
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec cet email"));

        String token = java.util.UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(java.time.LocalDateTime.now().plusMinutes(30)); // 30 min validité
        userRepository.save(user);

        // Dans un cas réel, c'est l'URL du frontend
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        // Log the reset link for development
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
        } catch (Exception e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
            // On continue même si l'email échoue (pour le développement)
        }
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (user.getResetPasswordTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }
}
