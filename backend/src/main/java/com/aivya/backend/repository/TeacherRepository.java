
package com.aivya.backend.repository;

import com.aivya.backend.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeacherRepository
        extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);
}
