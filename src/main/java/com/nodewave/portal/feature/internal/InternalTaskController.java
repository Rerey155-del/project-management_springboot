package com.nodewave.portal.feature.internal;

import com.nodewave.portal.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "3. Feature Slice: Internal Engineer", description = "Update Status Task (ABAC, Blocker, Optimistic Locking) & Upload Bukti Attachment")
@RestController
@RequestMapping("/api")
public class InternalTaskController {

    private final InternalTaskService internalTaskService;

    public InternalTaskController(InternalTaskService internalTaskService) {
        this.internalTaskService = internalTaskService;
    }

    @Operation(
            summary = "Update Status / Deskripsi Task",
            description = "Memperbarui status atau deskripsi task. Menerapkan ABAC (hanya assignee), Blocker check (prasyarat harus DONE), State-based rule (PM dilarang set DONE), Optimistic Locking (version), dan Audit Trail otomatis."
    )
    @PatchMapping("/tasks/{id}")
    public ResponseEntity<Map<String, Object>> updateTask(
            @PathVariable("id") String taskId,
            @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<String, Object> result = internalTaskService.updateTask(taskId, request, principal);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Upload Attachment Bukti Pengerjaan", description = "Mengunggah link attachment pada task. Hanya dapat dilakukan oleh role INTERNAL pada tugas miliknya.")
    @PatchMapping("/tasks/{id}/attachment")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @PathVariable("id") String taskId,
            @Valid @RequestBody UploadAttachmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Map<String, Object> result = internalTaskService.uploadAttachment(taskId, request, principal);
        return ResponseEntity.ok(result);
    }
}
