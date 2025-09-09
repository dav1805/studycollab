package com.project.collaboStudy.service;

import com.project.collaboStudy.dto.TaskDTO;
import com.project.collaboStudy.dto.TaskUpdateDTO;
import com.project.collaboStudy.model.*;
import com.project.collaboStudy.repository.ProjectRepository;
import com.project.collaboStudy.repository.TaskRepository;
import com.project.collaboStudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    @Transactional
    public Task createTask(Long projectId, Long assignedToId, Task taskDetails) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot add tasks to a completed project.");
        }

        taskDetails.setProject(project);

        User assignedTo = userRepository.findById(assignedToId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!project.getMembers().contains(assignedTo)) {
            throw new AccessDeniedException("The assigned user is not a member of this project.");
        }

        taskDetails.setAssignedTo(assignedTo);
        Task savedTask = taskRepository.save(taskDetails);

        projectService.updateProjectStatus(projectId);

        return savedTask;
    }

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public Task updateTask(Long taskId, TaskUpdateDTO updatedTaskDTO) {
        return taskRepository.findById(taskId).map(task -> {
            // **Authorization check:** Only the project owner or assigned user can update a task.
            String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            if (!task.getProject().getOwner().getUsername().equals(currentUsername) &&
                    !task.getAssignedTo().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("You do not have permission to update this task.");
            }

            // **New Dependency Logic:** Prevent completing a task if its prerequisite is not completed
            if (updatedTaskDTO.isCompleted() && task.getPrerequisiteTask() != null) {
                Task prerequisite = taskRepository.findById(task.getPrerequisiteTask().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Prerequisite task not found."));

                if (!prerequisite.isCompleted()) {
                    throw new IllegalStateException("Cannot complete this task because its prerequisite '" + prerequisite.getTitle() + "' is not yet completed.");
                }
            }

            // Map the DTO fields to the entity
            if (updatedTaskDTO.getTitle() != null) {
                task.setTitle(updatedTaskDTO.getTitle());
            }
            if (updatedTaskDTO.getDescription() != null) {
                task.setDescription(updatedTaskDTO.getDescription());
            }
            if (updatedTaskDTO.getDueDate() != null) {
                task.setDueDate(updatedTaskDTO.getDueDate());
            }
            if (updatedTaskDTO.getPriority() != null) {
                task.setPriority(updatedTaskDTO.getPriority());
            }
            task.setCompleted(updatedTaskDTO.isCompleted());

            Task savedTask = taskRepository.save(task);
            projectService.updateProjectStatus(savedTask.getProject().getId());

            return savedTask;
        }).orElseThrow(() -> new IllegalArgumentException("Task not found."));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task taskToDelete = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found."));

        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!taskToDelete.getProject().getOwner().getUsername().equals(currentUsername) &&
                !taskToDelete.getAssignedTo().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to delete this task.");
        }

        Long projectId = taskToDelete.getProject().getId();
        taskRepository.deleteById(id);

        projectService.updateProjectStatus(projectId);
    }

    public List<TaskDTO> findTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> findTasksByCompletedStatus(boolean completed) {
        return taskRepository.findByCompleted(completed).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> findTasksByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> findTasksByAssignedUser(String username) {
        return taskRepository.findByAssignedToUsername(username).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }
}