package com.project.collaboStudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Course cannot be blank")
    private String course;
    
    @NotBlank(message = "Goals cannot be blank")
    private String goals;
}