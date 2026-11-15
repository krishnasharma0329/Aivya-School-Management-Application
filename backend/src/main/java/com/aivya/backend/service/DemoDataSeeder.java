package com.aivya.backend.service;

import com.aivya.backend.entity.SchoolClass;
import com.aivya.backend.entity.Student;
import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.SchoolClassRepository;
import com.aivya.backend.repository.StudentRepository;
import com.aivya.backend.repository.TeacherRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.UUID;

/**
 * Seeds synthetic classes and students for the bootstrap teacher so the
 * mobile app has something to show on a clean install. No real student data
 * is ever used here -- names and roll numbers are entirely made up, and each
 * student gets a random QR check-in token (as if printed on an ID card).
 * Runs once: it skips a class that already has students.
 */
@Configuration
public class DemoDataSeeder {

    private record ClassSeed(String grade, String section, String subject, String room, int studentCount) {}

    private static final List<ClassSeed> CLASS_SEEDS = List.of(
            new ClassSeed("Grade 10", "Section A", "Advanced Mathematics", "Room 204", 18),
            new ClassSeed("Grade 9", "Section B", "General Science", "Lab 2", 16),
            new ClassSeed("Grade 8", "Section C", "English", "Room 105", 15)
    );

    private static final String[] FIRST_NAMES = {
            "Aarav", "Vivaan", "Aditi", "Diya", "Kabir", "Myra", "Rohan", "Isha",
            "Arjun", "Sara", "Vihaan", "Anika", "Reyansh", "Tara", "Kiaan", "Navya",
            "Dev", "Priya", "Yash", "Zara"
    };

    @Value("${BOOTSTRAP_TEACHER_EMAIL:teacher@aivya.edu}")
    private String bootstrapEmail;

    @Bean
    @Order(2)
    CommandLineRunner seedDemoClasses(
            TeacherRepository teacherRepository,
            SchoolClassRepository schoolClassRepository,
            StudentRepository studentRepository) {

        return args -> {
            if (bootstrapEmail == null || bootstrapEmail.isBlank()) {
                System.out.println("Demo data seeding skipped: no bootstrap teacher configured.");
                return;
            }

            Teacher teacher = teacherRepository.findByEmail(bootstrapEmail.trim().toLowerCase())
                    .orElse(null);

            if (teacher == null) {
                return;
            }

            if (!schoolClassRepository.findByTeacher_Id(teacher.getId()).isEmpty()) {
                System.out.println("Demo classes already exist, skipping seed.");
                return;
            }

            int nameIndex = 0;

            for (ClassSeed seed : CLASS_SEEDS) {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setGrade(seed.grade());
                schoolClass.setSection(seed.section());
                schoolClass.setSubject(seed.subject());
                schoolClass.setRoom(seed.room());
                schoolClass.setTeacher(teacher);
                schoolClass = schoolClassRepository.save(schoolClass);

                for (int i = 1; i <= seed.studentCount(); i++) {
                    Student student = new Student();
                    student.setSchoolClass(schoolClass);
                    student.setRollNumber(String.format("%02d", i));
                    student.setName(FIRST_NAMES[nameIndex % FIRST_NAMES.length] + " " + rollSuffix(i));
                    student.setQrCode("AIVYA-STU-" + UUID.randomUUID());
                    student.setActive(true);
                    studentRepository.save(student);
                    nameIndex++;
                }
            }

            System.out.println("Demo classes and students seeded successfully.");
        };
    }

    private String rollSuffix(int rollNumber) {
        char letter = (char) ('A' + (rollNumber % 26));
        return letter + ".";
    }
}
