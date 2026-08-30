package com.multigenesys.booking.service.impl;

import com.multigenesys.booking.dto.request.LoginRequest;
import com.multigenesys.booking.dto.request.RegisterRequest;
import com.multigenesys.booking.dto.response.AuthResponse;
import com.multigenesys.booking.entity.Role;
import com.multigenesys.booking.entity.User;
import com.multigenesys.booking.exception.BadRequestException;
import com.multigenesys.booking.exception.ConflictException;
import com.multigenesys.booking.repository.UserRepository;
import com.multigenesys.booking.security.JwtService;
import com.multigenesys.booking.security.UserPrincipal;
import com.multigenesys.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${application.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getRole().name())
                .expiresIn(jwtExpirationMs / 1000)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Attempting registration for username: {}, email: {}", 
                registerRequest.getUsername(), registerRequest.getEmail());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("Username is already taken: " + registerRequest.getUsername());
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email is already in use: " + registerRequest.getEmail());
        }

        User user = User.builder()
                .username(registerRequest.getUsername().trim())
                .email(registerRequest.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName().trim())
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);

        String jwt = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .expiresIn(jwtExpirationMs / 1000)
                .build();
    }
}
