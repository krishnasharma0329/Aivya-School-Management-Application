package com.aivya.backend.service;

import com.aivya.backend.dto.auth.LoginRequest;
import com.aivya.backend.dto.auth.LoginResponse;
import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.TeacherRepository;
import com.aivya.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public AuthService(
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Teacher teacher = teacherRepository.findByEmail(email)
                .filter(Teacher::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.password(),
                teacher.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(
                teacher.getEmail(),
                teacher.getRole()
        );

        return new LoginResponse(
                token,
                "Bearer",
                expiration / 1000,
                teacher.getId(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getRole()
        );
    }
}
