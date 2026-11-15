package com.aivya.backend.repository;

import com.aivya.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByStudent_SchoolClass_IdAndAttendanceDate(
        UUID classId, LocalDate date
    );

    List<Attendance> findByStudent_SchoolClass_IdAndAttendanceDateBetween(
        UUID classId, LocalDate start, LocalDate end
    );

    Optional<Attendance> findByStudent_IdAndAttendanceDate(
        UUID studentId, LocalDate date
    );
}
