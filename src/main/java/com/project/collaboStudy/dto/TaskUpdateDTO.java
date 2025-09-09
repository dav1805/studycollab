package com.project.collaboStudy.dto;

import com.project.collaboStudy.model.TaskPriority;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskUpdateDTO {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskPriority priority;
    private boolean completed;
}