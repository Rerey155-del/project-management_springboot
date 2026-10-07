package com.nodewave.portal.core.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "task_dependencies",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"task_id", "depends_on_task_id"})
    }
)
public class TaskDependency {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOnTask;

    public TaskDependency() {
        this.id = UUID.randomUUID().toString();
    }

    public TaskDependency(Task task, Task dependsOnTask) {
        this();
        this.task = task;
        this.dependsOnTask = dependsOnTask;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getDependsOnTask() {
        return dependsOnTask;
    }

    public void setDependsOnTask(Task dependsOnTask) {
        this.dependsOnTask = dependsOnTask;
    }
}
