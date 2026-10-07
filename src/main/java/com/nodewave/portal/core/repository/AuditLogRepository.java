package com.nodewave.portal.core.repository;

import com.nodewave.portal.core.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByTaskIdOrderByCreatedAtDesc(String taskId);

    List<AuditLog> findByChangedColumnAndNewValueAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            String changedColumn,
            String newValue,
            LocalDateTime createdAt
    );
}
