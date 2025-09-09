package com.project.collaboStudy.repository;

import com.project.collaboStudy.model.Task;
import com.project.collaboStudy.model.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);

    // Find tasks by their completed status
    List<Task> findByCompleted(boolean completed);

    // Find tasks by their priority
    List<Task> findByPriority(TaskPriority priority);

    // Find tasks by their assigned user
    List<Task> findByAssignedToUsername(String username);
}