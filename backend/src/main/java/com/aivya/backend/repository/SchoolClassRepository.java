
package com.aivya.backend.repository;

import com.aivya.backend.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolClassRepository
        extends JpaRepository<SchoolClass, UUID> {

    List<SchoolClass> findByTeacher_Id(UUID teacherId);

    Optional<SchoolClass> findByIdAndTeacher_Id(
        UUID classId,
        UUID teacherId
    );

    boolean existsByGradeAndSectionAndTeacher_Id(
        String grade,
        String section,
        UUID teacherId
    );
}
