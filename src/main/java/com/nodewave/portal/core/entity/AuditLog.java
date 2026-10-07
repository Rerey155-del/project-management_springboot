package com.nodewave.portal.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String taskId;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false)
    private String changedColumn;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(String taskId, String userId, String changedColumn, String oldValue, String newValue) {
        this();
        this.taskId = taskId;
        this.userId = userId;
        this.changedColumn = changedColumn;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChangedColumn() {
        return changedColumn;
    }

    public void setChangedColumn(String changedColumn) {
        this.changedColumn = changedColumn;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
