package com.aivya.backend.service;

import com.aivya.backend.dto.attendance.AttendanceHistoryItem;
import com.aivya.backend.dto.attendance.AttendanceRecordInput;
import com.aivya.backend.dto.attendance.AttendanceSubmitRequest;
import com.aivya.backend.dto.attendance.AttendanceSummaryResponse;
import com.aivya.backend.dto.attendance.QrCheckInRequest;
import com.aivya.backend.dto.attendance.QrCheckInResponse;
import com.aivya.backend.dto.attendance.StudentAttendanceResponse;
import com.aivya.backend.entity.Attendance;
import com.aivya.backend.entity.AttendanceStatus;
import com.aivya.backend.entity.SchoolClass;
import com.aivya.backend.entity.Student;
import com.aivya.backend.entity.Teacher;
import com.aivya.backend.repository.AttendanceRepository;
import com.aivya.backend.repository.SchoolClassRepository;
import com.aivya.backend.repository.StudentRepository;
import com.aivya.backend.repository.TeacherRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(
            SchoolClassRepository schoolClassRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            AttendanceRepository attendanceRepository) {

        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.attendanceRepository = attendanceRepository;
    }

    private Teacher requireTeacher(String email) {
        return teacherRepository.findByEmail(email)
                .filter(Teacher::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Teacher account not found or inactive"
                ));
    }

    // Only the teacher a class is assigned to may ever see or edit its
    // attendance -- this is the privacy boundary the assessment requires.
    private SchoolClass requireOwnedClass(UUID classId, Teacher teacher) {
        return schoolClassRepository.findByIdAndTeacher_Id(classId, teacher.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Class not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<StudentAttendanceResponse> getRoster(
            String teacherEmail, UUID classId, LocalDate date) {

        Teacher teacher = requireTeacher(teacherEmail);
        requireOwnedClass(classId, teacher);

        LocalDate target = date != null ? date : LocalDate.now();

        List<Student> students = studentRepository
                .findBySchoolClass_IdAndActiveTrueOrderByRollNumberAsc(classId);

        Map<UUID, Attendance> byStudent = attendanceRepository
                .findByStudent_SchoolClass_IdAndAttendanceDate(classId, target)
                .stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

        return students.stream()
                .map(student -> toResponse(student, byStudent.get(student.getId())))
                .toList();
    }

    @Transactional
    public AttendanceSummaryResponse submitAttendance(
            String teacherEmail, UUID classId, AttendanceSubmitRequest request) {

        Teacher teacher = requireTeacher(teacherEmail);
        SchoolClass schoolClass = requireOwnedClass(classId, teacher);
        LocalDate date = request.dateOrToday();

        for (AttendanceRecordInput record : request.records()) {
            Student student = studentRepository.findById(record.studentId())
                    .filter(s -> s.getSchoolClass().getId().equals(classId))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Student does not belong to this class"
                    ));

            upsertAttendance(student, date, record.status(), teacher);
        }

        return buildSummary(schoolClass.getId(), date);
    }

    @Transactional
    public QrCheckInResponse qrCheckIn(
            String teacherEmail, UUID classId, QrCheckInRequest request) {

        Teacher teacher = requireTeacher(teacherEmail);
        SchoolClass schoolClass = requireOwnedClass(classId, teacher);
        LocalDate today = LocalDate.now();

        Student student = studentRepository
                .findByQrCodeAndSchoolClass_IdAndActiveTrue(
                        request.qrCode().trim(), classId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This QR code does not match any student in this class"
                ));

        boolean alreadyMarked = attendanceRepository
                .findByStudent_IdAndAttendanceDate(student.getId(), today)
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .isPresent();

        Attendance attendance = upsertAttendance(
                student, today, AttendanceStatus.PRESENT, teacher);

        return new QrCheckInResponse(
                toResponse(student, attendance),
                buildSummary(schoolClass.getId(), today),
                alreadyMarked
        );
    }

    @Transactional(readOnly = true)
    public List<AttendanceHistoryItem> getHistory(
            String teacherEmail, UUID classId, int days) {

        Teacher teacher = requireTeacher(teacherEmail);
        requireOwnedClass(classId, teacher);

        int safeDays = Math.max(1, Math.min(days, 60));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(safeDays - 1L);

        List<Attendance> records = attendanceRepository
                .findByStudent_SchoolClass_IdAndAttendanceDateBetween(
                        classId, start, end);

        long totalStudents = studentRepository
                .countBySchoolClass_IdAndActiveTrue(classId);

        Map<LocalDate, List<Attendance>> byDate = records.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate));

        return start.datesUntil(end.plusDays(1))
                .map(day -> {
                    List<Attendance> dayRecords = byDate.getOrDefault(day, List.of());
                    int present = countStatus(dayRecords, AttendanceStatus.PRESENT);
                    int absent = countStatus(dayRecords, AttendanceStatus.ABSENT);
                    int late = countStatus(dayRecords, AttendanceStatus.LATE);
                    int unmarked = (int) totalStudents - dayRecords.size();

                    return new AttendanceHistoryItem(
                            day,
                            (int) totalStudents,
                            present,
                            absent,
                            late,
                            Math.max(unmarked, 0)
                    );
                })
                .toList();
    }

    private Attendance upsertAttendance(
            Student student,
            LocalDate date,
            AttendanceStatus status,
            Teacher teacher) {

        Attendance attendance = attendanceRepository
                .findByStudent_IdAndAttendanceDate(student.getId(), date)
                .orElseGet(Attendance::new);

        attendance.setStudent(student);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setRecordedBy(teacher);

        return attendanceRepository.save(attendance);
    }

    private AttendanceSummaryResponse buildSummary(UUID classId, LocalDate date) {
        List<Attendance> dayRecords = attendanceRepository
                .findByStudent_SchoolClass_IdAndAttendanceDate(classId, date);

        long total = studentRepository.countBySchoolClass_IdAndActiveTrue(classId);
        int present = countStatus(dayRecords, AttendanceStatus.PRESENT);
        int absent = countStatus(dayRecords, AttendanceStatus.ABSENT);
        int late = countStatus(dayRecords, AttendanceStatus.LATE);
        int unmarked = (int) total - dayRecords.size();

        return new AttendanceSummaryResponse(
                classId, date, (int) total, present, absent, late, Math.max(unmarked, 0));
    }

    private int countStatus(List<Attendance> records, AttendanceStatus status) {
        return (int) records.stream().filter(a -> a.getStatus() == status).count();
    }

    private StudentAttendanceResponse toResponse(Student student, Attendance attendance) {
        return new StudentAttendanceResponse(
                student.getId(),
                student.getName(),
                student.getRollNumber(),
                student.getQrCode() != null && !student.getQrCode().isBlank(),
                attendance != null ? attendance.getStatus() : null
        );
    }
}
