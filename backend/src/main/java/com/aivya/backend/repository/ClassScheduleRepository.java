package com.aivya.backend.repository;

import com.aivya.backend.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID> {

    List<ClassSchedule> findBySchoolClass_IdOrderByDayOfWeekAscStartTimeAsc(UUID classId);
}
