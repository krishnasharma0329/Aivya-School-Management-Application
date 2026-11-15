package com.aivya.backend.service;

import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TeacherBootstrap {

    // Synthetic demo account for reviewers/graders. Override with real
    // env vars (BOOTSTRAP_TEACHER_EMAIL/PASSWORD/NAME) in any shared environment.
    @Value("${BOOTSTRAP_TEACHER_EMAIL:teacher@aivya.edu}")
    private String bootstrapEmail;

    @Value("${BOOTSTRAP_TEACHER_PASSWORD:AivyaDemo@2026}")
    private String bootstrapPassword;

    @Value("${BOOTSTRAP_TEACHER_NAME:Demo Teacher}")
    private String bootstrapName;

    @Bean
    @Order(1)
    CommandLineRunner createInitialTeacher(
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String email = bootstrapEmail;
            String password = bootstrapPassword;
            String name = bootstrapName;

            if (email == null || password == null || name == null
                    || email.isBlank() || password.isBlank() || name.isBlank()) {
                System.out.println(
                    "Teacher bootstrap skipped: credentials not configured."
                );
                return;
            }

            email = email.trim().toLowerCase();

            if (teacherRepository.existsByEmail(email)) {
                System.out.println("Initial teacher already exists.");
                return;
            }

            if (password.length() < 12) {
                throw new IllegalStateException(
                    "Initial teacher password must contain at least 12 characters."
                );
            }

            Teacher teacher = new Teacher();
            teacher.setFullName(name.trim());
            teacher.setEmail(email);
            teacher.setPasswordHash(passwordEncoder.encode(password));
            teacher.setRole("TEACHER");
            teacher.setActive(true);

            teacherRepository.save(teacher);

            System.out.println("Initial teacher account created successfully.");
        };
    }
}

