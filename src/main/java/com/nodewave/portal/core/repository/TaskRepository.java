package com.nodewave.portal.core.repository;

import com.nodewave.portal.core.entity.Task;
import com.nodewave.portal.core.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedAtIsNull(String id);

    List<Task> findByProjectIdAndDeletedAtIsNull(String projectId);

    List<Task> findByProjectIdAndStatusNotAndDeletedAtIsNull(String projectId, TaskStatus status);

    List<Task> findByIdInAndProjectIdAndDeletedAtIsNull(List<String> ids, String projectId);

    long countByProjectIdAndDeletedAtIsNull(String projectId);

    long countByProjectIdAndStatusAndDeletedAtIsNull(String projectId, TaskStatus status);

    long countByProjectIdAndIsClientVisibleTrueAndDeletedAtIsNull(String projectId);

    long countByProjectIdAndIsClientVisibleTrueAndStatusAndDeletedAtIsNull(String projectId, TaskStatus status);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.project WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Task> findByIdWithDetails(@Param("id") String id);
}
