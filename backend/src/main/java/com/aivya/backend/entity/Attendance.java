package com.aivya.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_student_date",
        columnNames = {"student_id", "attendance_date"}
    ))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by", nullable = false)
    private Teacher recordedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Attendance() {}

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Student getStudent() { return student; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public AttendanceStatus getStatus() { return status; }
    public Teacher getRecordedBy() { return recordedBy; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStudent(Student student) { this.student = student; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public void setRecordedBy(Teacher recordedBy) { this.recordedBy = recordedBy; }
}
