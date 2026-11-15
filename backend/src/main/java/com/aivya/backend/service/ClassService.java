package com.aivya.backend.service;

import com.aivya.backend.dto.classes.ClassResponse;
import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.SchoolClassRepository;
import com.aivya.backend.repository.StudentRepository;
import com.aivya.backend.repository.TeacherRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public ClassService(
            SchoolClassRepository schoolClassRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository) {

        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClasses(String email) {

        Teacher teacher = teacherRepository.findByEmail(email)
                .filter(Teacher::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Teacher account not found or inactive"
                ));

        return schoolClassRepository
                .findByTeacher_Id(teacher.getId())
                .stream()
                .map(schoolClass -> new ClassResponse(
                        schoolClass.getId(),
                        schoolClass.getGrade(),
                        schoolClass.getSection(),
                        schoolClass.getSubject(),
                        schoolClass.getRoom(),
                        studentRepository.countBySchoolClass_IdAndActiveTrue(
                                schoolClass.getId()
                        )
                ))
                .toList();
    }
}
