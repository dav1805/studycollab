package com.project.collaboStudy.controller;

import com.project.collaboStudy.dto.TaskDTO;
import com.project.collaboStudy.dto.TaskUpdateDTO;
import com.project.collaboStudy.model.Task;
import com.project.collaboStudy.model.TaskPriority;
import com.project.collaboStudy.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/users/{assignedToId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Task> createTask(
            @PathVariable Long projectId,
            @PathVariable Long assignedToId,
            @RequestBody Task taskDetails) {
        Task createdTask = taskService.createTask(projectId, assignedToId, taskDetails);
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody TaskUpdateDTO updatedTaskDTO) {
        Task task = taskService.updateTask(taskId, updatedTaskDTO);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksByProjectId(@PathVariable Long projectId) {
        List<TaskDTO> tasks = taskService.findTasksByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/filter-by-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(@RequestParam boolean completed) {
        List<TaskDTO> tasks = taskService.findTasksByCompletedStatus(completed);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/filter-by-priority")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksByPriority(@RequestParam TaskPriority priority) {
        List<TaskDTO> tasks = taskService.findTasksByPriority(priority);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/filter-by-assignee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(@RequestParam String username) {
        List<TaskDTO> tasks = taskService.findTasksByAssignedUser(username);
        return ResponseEntity.ok(tasks);
    }
}