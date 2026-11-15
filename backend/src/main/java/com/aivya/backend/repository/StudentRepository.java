package com.aivya.backend.repository;

import com.aivya.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findBySchoolClass_IdAndActiveTrue(UUID classId);

    List<Student> findBySchoolClass_IdAndActiveTrueOrderByRollNumberAsc(UUID classId);

    long countBySchoolClass_IdAndActiveTrue(UUID classId);

    boolean existsBySchoolClass_IdAndRollNumber(
            UUID classId,
            String rollNumber
    );

    Optional<Student> findByQrCodeAndSchoolClass_IdAndActiveTrue(
            String qrCode,
            UUID classId
    );
}
