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
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        
        String token = jwtUtil.generateToken(
                user.getEmail(), 
                user.getId(), 
                user.getRole().name()
        );
        
        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole().name()
        );
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
}
