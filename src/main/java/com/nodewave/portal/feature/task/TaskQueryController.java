package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.TaskStatus;
import com.nodewave.portal.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "5. Feature Slice: Task Query", description = "Query Daftar Tugas, Pencarian Multi-keyword, Filter Status, Pagination, dan Dynamic Role Masking")
@RestController
@RequestMapping("/api/tasks")
public class TaskQueryController {

    private final TaskQueryService taskQueryService;

    public TaskQueryController(TaskQueryService taskQueryService) {
        this.taskQueryService = taskQueryService;
    }

    @Operation(
            summary = "Ambil Daftar Tugas (Dengan Pencarian & Metrik)",
            description = "Menampilkan list tugas berdasarkan projectId, filter kata kunci pencarian (multi-keyword terhadap title, description, assignee), filter status, dan pagination. Otomatis memask data jika role adalah CLIENT."
    )
    @GetMapping
    public ResponseEntity<TasksResponse> getTasks(
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) TaskStatus status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TasksResponse response = taskQueryService.getTasks(projectId, search, status, page, limit, principal);
        return ResponseEntity.ok(response);
    }
}
