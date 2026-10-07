package com.nodewave.portal.feature.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "6. Feature Slice: Common & Healthcheck", description = "Endpoint Root & Pengecekan Kesehatan API")
@RestController
public class RootController {

    @Operation(summary = "Root API Info", description = "Informasi root bahwa API sedang berjalan.")
    @GetMapping({"/", "/api"})
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "message", "Nodewave Assessment API is running!",
                "status", "OK"
        ));
    }

    @Operation(summary = "Healthcheck API", description = "Mengecek status kesehatan server dan timestamp.")
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", Instant.now().toString()
        ));
    }
}
