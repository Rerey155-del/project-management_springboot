package com.nodewave.portal.feature.client;

import com.nodewave.portal.core.entity.Project;
import com.nodewave.portal.core.entity.Task;
import com.nodewave.portal.core.repository.ProjectRepository;
import com.nodewave.portal.core.repository.TaskRepository;
import com.nodewave.portal.exception.ApiException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ClientTaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public ClientTaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public List<ClientTaskDto> getClientTasks(String requestedProjectId) {
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
        Specification<Task> spec = (root, query, cb) -> cb.and(
                cb.isNull(root.get("deletedAt")),
                cb.equal(root.get("project").get("id"), finalProjectId),
                cb.isTrue(root.get("isClientVisible"))
        );

        return taskRepository.findAll(spec).stream()
                .map(ClientTaskDto::fromEntity)
                .collect(Collectors.toList());
    }
}
