package com.nodewave.portal.feature.task;

import com.nodewave.portal.core.entity.Project;
import com.nodewave.portal.core.entity.Role;
import com.nodewave.portal.core.entity.Task;
import com.nodewave.portal.core.entity.TaskDependency;
import com.nodewave.portal.core.entity.TaskStatus;
import com.nodewave.portal.core.entity.User;
import com.nodewave.portal.core.repository.ProjectRepository;
import com.nodewave.portal.core.repository.TaskDependencyRepository;
import com.nodewave.portal.core.repository.TaskRepository;
import com.nodewave.portal.core.security.UserPrincipal;
import com.nodewave.portal.exception.ApiException;
import com.nodewave.portal.feature.client.ClientTaskDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskDependencyRepository dependencyRepository;

    public TaskQueryService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            TaskDependencyRepository dependencyRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.dependencyRepository = dependencyRepository;
    }

    public TasksResponse getTasks(
            String requestedProjectId,
            String search,
            TaskStatus status,
            Integer page,
            Integer limit,
            UserPrincipal principal
    ) {
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

        final String finalProjectId = projectId;
        int currentPage = (page != null && page > 0) ? page : 1;
        int currentLimit = (limit != null && limit > 0) ? limit : 50;

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("project").get("id"), finalProjectId));

            if (principal.getRole() == Role.CLIENT) {
                predicates.add(cb.isTrue(root.get("isClientVisible")));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String[] keywords = search.trim().split("\\s+");
                Join<Task, User> assigneeJoin = root.join("assignee", JoinType.LEFT);

                for (String word : keywords) {
                    if (!word.isEmpty()) {
                        String pattern = "%" + word.toLowerCase() + "%";
                        Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                        Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                        Predicate assigneeMatch = cb.like(cb.lower(assigneeJoin.get("name")), pattern);
                        predicates.add(cb.or(titleMatch, descMatch, assigneeMatch));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(currentPage - 1, currentLimit, Sort.by(Sort.Direction.ASC, "id"));
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        long totalTasksFilter = taskPage.getTotalElements();
        long allTasksCount;
        long doneTasksCount;

        if (principal.getRole() == Role.CLIENT) {
            allTasksCount = taskRepository.countByProjectIdAndIsClientVisibleTrueAndDeletedAtIsNull(finalProjectId);
            doneTasksCount = taskRepository.countByProjectIdAndIsClientVisibleTrueAndStatusAndDeletedAtIsNull(finalProjectId, TaskStatus.DONE);
        } else {
            allTasksCount = taskRepository.countByProjectIdAndDeletedAtIsNull(finalProjectId);
            doneTasksCount = taskRepository.countByProjectIdAndStatusAndDeletedAtIsNull(finalProjectId, TaskStatus.DONE);
        }

        long progressPercentage = allTasksCount > 0 ? Math.round(((double) doneTasksCount / allTasksCount) * 100) : 0;

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("total", totalTasksFilter);
        meta.put("page", currentPage);
        meta.put("limit", currentLimit);
        meta.put("totalPages", (int) Math.ceil((double) totalTasksFilter / currentLimit));

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalTasks", allTasksCount);
        metrics.put("completedTasks", doneTasksCount);
        metrics.put("progress", progressPercentage + "%");

        List<?> tasksData;
        if (principal.getRole() == Role.CLIENT) {
            tasksData = taskPage.getContent().stream()
                    .map(ClientTaskDto::fromEntity)
                    .collect(Collectors.toList());
        } else {
            tasksData = taskPage.getContent().stream()
                    .map(t -> {
                        List<TaskDependency> deps = dependencyRepository.findByTaskId(t.getId());
                        List<TaskDependencyItemDto> depDtos = deps.stream()
                                .map(TaskDependencyItemDto::fromEntity)
                                .collect(Collectors.toList());
                        return TaskDetailDto.fromEntity(t, depDtos);
                    })
                    .collect(Collectors.toList());
        }

        return new TasksResponse(principal.getRole(), meta, metrics, tasksData);
    }
}
