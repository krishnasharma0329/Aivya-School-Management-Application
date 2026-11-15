package com.aivya.backend.service;

import com.aivya.backend.entity.ClassSchedule;
import com.aivya.backend.entity.SchoolClass;
import com.aivya.backend.entity.Student;
import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.ClassScheduleRepository;
import com.aivya.backend.repository.SchoolClassRepository;
import com.aivya.backend.repository.StudentRepository;
import com.aivya.backend.repository.TeacherRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Seeds synthetic classes, students and a weekly timetable for the bootstrap
 * teacher so the mobile app has something to show on a clean install. No
 * real student data is ever used here -- names and roll numbers are entirely
 * made up, and each student gets a random QR check-in token (as if printed
 * on an ID card). Runs once: it skips a class that already has students.
 */
@Configuration
public class DemoDataSeeder {

    private record ScheduleSeed(DayOfWeek day, LocalTime start, LocalTime end) {}

    private record ClassSeed(
            String grade, String section, String subject, String room,
            int studentCount, List<ScheduleSeed> schedules) {}

    @Value("${BOOTSTRAP_TEACHER_EMAIL:teacher@aivya.edu}")
    private String bootstrapEmail;

    private static final String[] FIRST_NAMES = {
            "Aarav", "Vivaan", "Aditi", "Diya", "Kabir", "Myra", "Rohan", "Isha",
            "Arjun", "Sara", "Vihaan", "Anika", "Reyansh", "Tara", "Kiaan", "Navya",
            "Dev", "Priya", "Yash", "Zara"
    };

    @Bean
    @Order(2)
    CommandLineRunner seedDemoClasses(
            TeacherRepository teacherRepository,
            SchoolClassRepository schoolClassRepository,
            StudentRepository studentRepository,
            ClassScheduleRepository classScheduleRepository) {

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

            // Fixed Monday-Saturday timetable -- schools are closed Sunday, so
            // no class ever gets a Sunday slot. Real, believable school hours
            // so Ongoing/Upcoming/Missed reflect whatever time it actually is.
            List<ClassSeed> classSeeds = List.of(
                    new ClassSeed("Grade 10", "Section A", "Advanced Mathematics", "Room 204", 18, List.of(
                            new ScheduleSeed(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                            new ScheduleSeed(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                            new ScheduleSeed(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0))
                    )),
                    new ClassSeed("Grade 9", "Section B", "General Science", "Lab 2", 16, List.of(
                            new ScheduleSeed(DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 0)),
                            new ScheduleSeed(DayOfWeek.THURSDAY, LocalTime.of(11, 0), LocalTime.of(12, 0)),
                            new ScheduleSeed(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(11, 0))
                    )),
                    new ClassSeed("Grade 8", "Section C", "English", "Room 105", 15, List.of(
                            new ScheduleSeed(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
                            new ScheduleSeed(DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)),
                            new ScheduleSeed(DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30))
                    ))
            );

            int nameIndex = 0;

            for (ClassSeed seed : classSeeds) {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setGrade(seed.grade());
                schoolClass.setSection(seed.section());
                schoolClass.setSubject(seed.subject());
                schoolClass.setRoom(seed.room());
                schoolClass.setTeacher(teacher);
                schoolClass = schoolClassRepository.save(schoolClass);

                for (ScheduleSeed slot : seed.schedules()) {
                    ClassSchedule schedule = new ClassSchedule();
                    schedule.setSchoolClass(schoolClass);
                    schedule.setDayOfWeek(slot.day());
                    schedule.setStartTime(slot.start());
                    schedule.setEndTime(slot.end());
                    classScheduleRepository.save(schedule);
                }

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

            System.out.println("Demo classes, schedules and students seeded successfully.");
        };
    }

    private String rollSuffix(int rollNumber) {
        char letter = (char) ('A' + (rollNumber % 26));
        return letter + ".";
    }
}
