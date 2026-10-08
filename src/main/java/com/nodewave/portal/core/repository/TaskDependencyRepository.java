package com.nodewave.portal.core.repository;

import com.nodewave.portal.core.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, String> {

    List<TaskDependency> findByTaskId(String taskId);
    List<TaskDependency> findByDependsOnTaskId(String dependsOnTaskId);

    boolean existsByTaskIdAndDependsOnTaskId(String taskId, String dependsOnTaskId);
    Optional<TaskDependency> findByTaskIdAndDependsOnTaskId(String taskId, String dependsOnTaskId);
}
