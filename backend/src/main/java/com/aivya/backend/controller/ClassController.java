package com.aivya.backend.controller;

import com.aivya.backend.dto.classes.ClassResponse;
import com.aivya.backend.service.ClassService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public ResponseEntity<List<ClassResponse>> getMyClasses(
            Authentication authentication) {

        List<ClassResponse> classes =
                classService.getTeacherClasses(authentication.getName());

        return ResponseEntity.ok(classes);
    }
}
