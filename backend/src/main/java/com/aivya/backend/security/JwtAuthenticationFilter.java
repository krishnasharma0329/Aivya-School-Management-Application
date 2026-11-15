package com.aivya.backend.security;

import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.TeacherRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TeacherRepository teacherRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            TeacherRepository teacherRepository) {
        this.jwtService = jwtService;
        this.teacherRepository = teacherRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtService.isTokenValid(token)) {
                    String email = jwtService.extractEmail(token);

                    Teacher teacher = teacherRepository
                            .findByEmail(email)
                            .filter(Teacher::isActive)
                            .orElse(null);

                    if (teacher != null &&
                            SecurityContextHolder.getContext()
                                    .getAuthentication() == null) {

                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        teacher.getEmail(),
                                        null,
                                        List.of(new SimpleGrantedAuthority(
                                                "ROLE_" + teacher.getRole()
                                        ))
                                );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
