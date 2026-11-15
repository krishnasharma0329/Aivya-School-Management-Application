
package com.aivya.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "school_classes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_class_section_teacher",
            columnNames = {"grade", "section", "teacher_id"}
        )
    },
    indexes = {
        @Index(name = "idx_class_teacher", columnList = "teacher_id")
    }
)
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String grade;

    @Column(nullable = false, length = 30)
    private String section;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(length = 30)
    private String room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "teacher_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_class_teacher")
    )
    private Teacher teacher;

    public SchoolClass() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
