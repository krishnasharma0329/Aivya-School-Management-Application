package com.aivya.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "students",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_roll_class",
            columnNames = {"school_class_id", "roll_number"}
        ),
        @UniqueConstraint(
            name = "uk_student_qr_code",
            columnNames = {"qr_code"}
        )
    })
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "roll_number", nullable = false, length = 30)
    private String rollNumber;

    // Unique token printed on the student's ID card as a QR code.
    // Scanning it resolves straight to this student for attendance check-in.
    @Column(name = "qr_code", length = 64)
    private String qrCode;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_class_id", nullable = false)
    private SchoolClass schoolClass;

    public Student() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getRollNumber() { return rollNumber; }
    public String getQrCode() { return qrCode; }
    public boolean isActive() { return active; }
    public SchoolClass getSchoolClass() { return schoolClass; }

    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public void setActive(boolean active) { this.active = active; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
}
