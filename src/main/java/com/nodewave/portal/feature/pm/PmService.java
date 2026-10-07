package com.nodewave.portal.feature.pm;

import com.nodewave.portal.core.entity.*;
import com.nodewave.portal.core.repository.*;
import com.nodewave.portal.core.security.UserPrincipal;
import com.nodewave.portal.exception.AccessDeniedException;
import com.nodewave.portal.exception.ApiException;
import com.nodewave.portal.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PmService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final AuditLogRepository auditLogRepository;

    public PmService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<ProjectDto> getAllProjects() {
        List<Project> projects = projectRepository.findAllByDeletedAtIsNullOrderByCreatedAtAsc();
        return projects.stream()
                .map(p -> {
                    long count = taskRepository.countByProjectIdAndDeletedAtIsNull(p.getId());
                    return ProjectDto.fromEntity(p, count);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> addDependency(String taskId, CreateDependencyRequest request, UserPrincipal principal) {
        if (principal.getRole() != Role.PM) {
            throw new AccessDeniedException("Hanya Product Manager yang dapat mengatur Task Dependencies");
        }

        if (taskId.equals(request.dependsOnTaskId())) {
            throw new ApiException("Task cannot depend on itself", HttpStatus.BAD_REQUEST);
        }

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        Task dependsOnTask = taskRepository.findByIdAndDeletedAtIsNull(request.dependsOnTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite task not found with ID: " + request.dependsOnTaskId()));

        if (dependencyRepository.existsByTaskIdAndDependsOnTaskId(taskId, request.dependsOnTaskId())) {
            throw new ApiException("Dependency relationship already exists", HttpStatus.CONFLICT);
        }

        TaskDependency dependency = new TaskDependency(task, dependsOnTask);
        dependencyRepository.save(dependency);

        return Map.of(
                "message", "Dependency berhasil ditambahkan",
                "data", Map.of(
                        "id", dependency.getId(),
                        "taskId", task.getId(),
                        "dependsOnTaskId", dependsOnTask.getId()
                )
        );
    }

    public Map<String, Object> getStandupSummary(String requestedProjectId) {
        String projectId = requestedProjectId;
        if (projectId == null || projectId.trim().isEmpty()) {
            Project defaultProject = projectRepository.findFirstByDeletedAtIsNullOrderByCreatedAtAsc()
                    .orElse(null);
            if (defaultProject != null) {
                projectId = defaultProject.getId();
            }
        }

        if (projectId == null) {
            throw new ApiException("projectId query parameter is required", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);

        List<AuditLog> completedLogs = auditLogRepository
                .findByChangedColumnAndNewValueAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
                        "status", "DONE", oneDayAgo);

        Set<String> completedTaskIds = completedLogs.stream()
                .map(AuditLog::getTaskId)
                .collect(Collectors.toSet());

        List<Task> completedTasks = completedTaskIds.isEmpty() ? Collections.emptyList() :
                taskRepository.findByIdInAndProjectIdAndDeletedAtIsNull(new ArrayList<>(completedTaskIds), projectId);

        List<Task> allIncompleteTasks = taskRepository
                .findByProjectIdAndStatusNotAndDeletedAtIsNull(projectId, TaskStatus.DONE);

        List<Task> blockedTasks = new ArrayList<>();
        Map<String, List<String>> blockedReasons = new HashMap<>();

        for (Task t : allIncompleteTasks) {
            List<TaskDependency> deps = dependencyRepository.findByTaskId(t.getId());
            List<String> uncompletedPrereqs = deps.stream()
                    .map(TaskDependency::getDependsOnTask)
                    .filter(prereq -> prereq.getStatus() != TaskStatus.DONE)
                    .map(Task::getTitle)
                    .toList();

            if (t.getStatus() == TaskStatus.BLOCKED || !uncompletedPrereqs.isEmpty()) {
                blockedTasks.add(t);
                blockedReasons.put(t.getId(), uncompletedPrereqs);
            }
        }

        Map<String, Map<String, List<Map<String, Object>>>> summary = new LinkedHashMap<>();
        summary.put("UIUX", createEmptyDeptSummary());
        summary.put("FRONTEND", createEmptyDeptSummary());
        summary.put("BACKEND", createEmptyDeptSummary());

        for (Task t : completedTasks) {
            String dept = (t.getAssignee() != null && t.getAssignee().getDepartment() != null && t.getAssignee().getDepartment() != Department.NONE)
                    ? t.getAssignee().getDepartment().name() : "UNASSIGNED";

            summary.computeIfAbsent(dept, k -> createEmptyDeptSummary());
            Map<String, Object> item = new HashMap<>();
            item.put("id", t.getId());
            item.put("title", t.getTitle());
            item.put("engineer", t.getAssignee() != null ? t.getAssignee().getName() : "Unassigned");
            summary.get(dept).get("diselesaikan_kemarin").add(item);
        }

        for (Task t : blockedTasks) {
            String dept = (t.getAssignee() != null && t.getAssignee().getDepartment() != null && t.getAssignee().getDepartment() != Department.NONE)
                    ? t.getAssignee().getDepartment().name() : "UNASSIGNED";

            summary.computeIfAbsent(dept, k -> createEmptyDeptSummary());
            Map<String, Object> item = new HashMap<>();
            item.put("id", t.getId());
            item.put("title", t.getTitle());
            item.put("engineer", t.getAssignee() != null ? t.getAssignee().getName() : "Unassigned");
            item.put("waitingFor", blockedReasons.getOrDefault(t.getId(), Collections.emptyList()));
            summary.get(dept).get("terblokir_hari_ini").add(item);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Daily Standup Summary generated successfully");
        response.put("date_referenced", oneDayAgo.toLocalDate().toString());
        response.put("data", summary);
        return response;
    }

    private Map<String, List<Map<String, Object>>> createEmptyDeptSummary() {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        map.put("diselesaikan_kemarin", new ArrayList<>());
        map.put("terblokir_hari_ini", new ArrayList<>());
        return map;
    }
}
