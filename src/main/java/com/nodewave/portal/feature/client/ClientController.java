package com.nodewave.portal.feature.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "4. Feature Slice: Client", description = "Proyeksi Data Tugas Khusus Klien (Data Masking)")
@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final ClientTaskService clientTaskService;

    public ClientController(ClientTaskService clientTaskService) {
        this.clientTaskService = clientTaskService;
    }

    @Operation(summary = "Daftar Tugas Klien (Masked)", description = "Mengambil tugas yang diizinkan untuk klien (isClientVisible = true) tanpa menampilkan assignee atau attachment internal.")
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getClientTasks(
            @RequestParam(value = "projectId", required = false) String projectId
    ) {
        List<ClientTaskDto> tasks = clientTaskService.getClientTasks(projectId);
        return ResponseEntity.ok(Map.of(
                "message", "Client tasks retrieved successfully",
                "data", tasks
        ));
    }
}
