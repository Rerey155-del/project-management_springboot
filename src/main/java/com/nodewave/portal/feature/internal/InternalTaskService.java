package com.nodewave.portal.feature.internal;

import com.nodewave.portal.core.entity.*;
import com.nodewave.portal.core.repository.AuditLogRepository;
import com.nodewave.portal.core.repository.TaskDependencyRepository;
import com.nodewave.portal.core.repository.TaskRepository;
import com.nodewave.portal.core.repository.UserRepository;
import com.nodewave.portal.core.security.UserPrincipal;
import com.nodewave.portal.exception.AccessDeniedException;
import com.nodewave.portal.exception.OptimisticLockConflictException;
import com.nodewave.portal.exception.ResourceNotFoundException;
import com.nodewave.portal.exception.TaskBlockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class InternalTaskService {

    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public InternalTaskService(
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> updateTask(String taskId, UpdateTaskRequest request, UserPrincipal principal) {
        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID '" + taskId + "'"));

        // ABAC & State-based validations
        if (principal.getRole() == Role.CLIENT) {
            throw new AccessDeniedException("Akses Ditolak: Klien tidak memiliki izin mengubah tugas.");
        }

        if (principal.getRole() == Role.PM && request.status() == TaskStatus.DONE && currentTask.getStatus() != TaskStatus.DONE) {
            throw new AccessDeniedException("Product Manager tidak dapat menandai task sebagai DONE");
        }

        if (principal.getRole() == Role.INTERNAL) {
            String assigneeId = currentTask.getAssignee() != null ? currentTask.getAssignee().getId() : null;
            if (assigneeId == null || !assigneeId.equals(principal.getId())) {
                throw new AccessDeniedException("Akses Ditolak: Kamu hanya bisa mengubah status tugas milikmu sendiri.");
            }
            if (request.description() != null && !request.description().equals(currentTask.getDescription())) {
                throw new AccessDeniedException("Akses Ditolak: Tim internal tidak dapat mengubah deskripsi utama task; hanya dapat mengunggah attachment dan mengubah status.");
            }
        }

        // Inter-task blocking: cannot transition to IN_PROGRESS or DONE if dependencies are incomplete
        if (request.status() == TaskStatus.IN_PROGRESS || request.status() == TaskStatus.DONE) {
            List<TaskDependency> dependencies = dependencyRepository.findByTaskId(taskId);
            boolean isBlocked = dependencies.stream()
                    .anyMatch(dep -> dep.getDependsOnTask().getStatus() != TaskStatus.DONE);
            if (isBlocked) {
                throw new TaskBlockedException("Task is BLOCKED. Prerequisites must be DONE first.");
            }
        }

        // Optimistic locking check
        if (request.version() != null && !request.version().equals(currentTask.getVersion())) {
            throw new OptimisticLockConflictException("Conflict 409: Data has been modified by another user. Please refresh.");
        }

        User actingUser = userRepository.findById(principal.getId()).orElse(null);

        // Audit Logging
        List<AuditLog> auditLogs = new ArrayList<>();
        if (request.status() != null && request.status() != currentTask.getStatus()) {
            auditLogs.add(new AuditLog(
                    currentTask.getId(),
                    principal.getId(),
                    "status",
                    currentTask.getStatus().name(),
                    request.status().name()
            ));
            currentTask.setStatus(request.status());
        }

        if (request.description() != null && !request.description().equals(currentTask.getDescription())) {
            auditLogs.add(new AuditLog(
                    currentTask.getId(),
                    principal.getId(),
                    "description",
                    currentTask.getDescription(),
                    request.description()
            ));
            currentTask.setDescription(request.description());
        }

        if (!auditLogs.isEmpty()) {
            auditLogRepository.saveAll(auditLogs);
        }

        Task savedTask = taskRepository.save(currentTask);

        return Map.of(
                "message", "Task updated successfully",
                "data", mapTaskToDto(savedTask)
        );
    }

    public Map<String, Object> uploadAttachment(String taskId, UploadAttachmentRequest request, UserPrincipal principal) {
        if (principal.getRole() != Role.INTERNAL) {
            throw new AccessDeniedException("Hanya tim internal yang dapat mengunggah attachment");
        }

        Task currentTask = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID '" + taskId + "'"));

        String assigneeId = currentTask.getAssignee() != null ? currentTask.getAssignee().getId() : null;
        if (assigneeId == null || !assigneeId.equals(principal.getId())) {
            throw new AccessDeniedException("Akses Ditolak: Kamu hanya bisa mengunggah attachment pada tugas milikmu sendiri.");
        }

        currentTask.setAttachmentUrl(request.attachmentUrl());
        Task savedTask = taskRepository.save(currentTask);

        return Map.of(
                "message", "Attachment berhasil diunggah",
                "data", mapTaskToDto(savedTask)
        );
    }

    private Map<String, Object> mapTaskToDto(Task task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", task.getId());
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("status", task.getStatus());
        map.put("isClientVisible", task.getIsClientVisible());
        map.put("version", task.getVersion());
        map.put("attachmentUrl", task.getAttachmentUrl());
        map.put("projectId", task.getProject() != null ? task.getProject().getId() : null);
        map.put("assigneeId", task.getAssignee() != null ? task.getAssignee().getId() : null);
        map.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        map.put("updatedAt", task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);
        return map;
    }
}
