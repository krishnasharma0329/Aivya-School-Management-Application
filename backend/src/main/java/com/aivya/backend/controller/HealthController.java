
package com.aivya.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public Map<String, String> healthCheck() {

        return Map.of(
                "status", "UP",
                "application", "Aivya School Management Backend",
                "version", "1.0.0"
        );
    }
}

