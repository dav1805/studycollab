package com.project.collaboStudy.dto;

import com.project.collaboStudy.model.Task;
import com.project.collaboStudy.model.TaskPriority;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskPriority priority;
    private boolean completed;
    private String assignedToUsername;
    private Long projectId;

    public static TaskDTO fromEntity(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setPriority(task.getPriority());
        dto.setCompleted(task.isCompleted());
        if (task.getAssignedTo() != null) {
            dto.setAssignedToUsername(task.getAssignedTo().getUsername());
        }
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
        }
        return dto;
    }
}