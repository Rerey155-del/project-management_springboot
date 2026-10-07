package com.nodewave.portal.feature.pm;

import com.nodewave.portal.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "2. Feature Slice: Product Manager (PM)", description = "Manajemen Proyek, Pengaturan Dependensi, dan Ringkasan Daily Standup (Khusus PM)")
@RestController
@RequestMapping("/api")
public class PmController {

    private final PmService pmService;

    public PmController(PmService pmService) {
        this.pmService = pmService;
    }

    @Operation(summary = "Daftar Semua Proyek", description = "Mengambil seluruh proyek aktif beserta total jumlah task.")
    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> getProjects() {
        List<ProjectDto> projects = pmService.getAllProjects();
        return ResponseEntity.ok(Map.of(
                "message", "Projects retrieved successfully",
                "data", projects
        ));
    }

    @Operation(summary = "Tambah Dependensi Tugas (Khusus PM)", description = "Menetapkan task prasyarat untuk suatu task. Hanya PM yang memiliki akses.")
    @PostMapping("/tasks/{id}/dependencies")
    public ResponseEntity<Map<String, Object>> addDependency(
            @PathVariable("id") String taskId,
            @Valid @RequestBody CreateDependencyRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<String, Object> result = pmService.addDependency(taskId, request, principal);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Daily Standup Auto-Summary", description = "Ringkasan standup harian dari AuditLog (diselesaikan kemarin dan terblokir hari ini) dikelompokkan per departemen.")
    @GetMapping({"/tasks/standup/summary", "/tasks/summary/standup"})
    public ResponseEntity<Map<String, Object>> getStandupSummary(
            @RequestParam(value = "projectId", required = false) String projectId
    ) {
        Map<String, Object> result = pmService.getStandupSummary(projectId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Daily Standup Summary by Project ID", description = "Ringkasan standup harian berdasarkan Project ID.")
    @GetMapping("/projects/{projectId}/standup-summary")
    public ResponseEntity<Map<String, Object>> getStandupSummaryByProject(
            @PathVariable("projectId") String projectId
    ) {
        Map<String, Object> result = pmService.getStandupSummary(projectId);
        return ResponseEntity.ok(result);
    }
}
