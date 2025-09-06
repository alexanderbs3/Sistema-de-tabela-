// AuthService.java - CORRIGIDO
package com.leaderboard.service;

import com.leaderboard.dto.AuthResponse;
import com.leaderboard.dto.LoginRequest;
import com.leaderboard.dto.RegisterRequest;
import com.leaderboard.entity.User;
import com.leaderboard.exception.ResourceNotFoundException;
import com.leaderboard.repository.UserRepository;
import com.leaderboard.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "username", loginRequest.getUsername()));

        // ✅ CORREÇÃO: Usar o construtor com 3 parâmetros
        return new AuthResponse(jwt, user.getUsername(), user.getEmail());
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username já está sendo usado!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email já está sendo usado!");
        }

        // Criar novo usuário
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword())
        );

        User result = userRepository.save(user);

        String jwt = tokenProvider.generateTokenFromUsername(result.getUsername());

        // ✅ CORREÇÃO: Usar o construtor com 3 parâmetros
        return new AuthResponse(jwt, result.getUsername(), result.getEmail());
    }
}