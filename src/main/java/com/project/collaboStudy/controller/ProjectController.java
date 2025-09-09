package com.project.collaboStudy.controller;

import com.project.collaboStudy.dto.ProjectUpdateDTO;
import com.project.collaboStudy.model.Project;
import com.project.collaboStudy.repository.ProjectRepository;
import com.project.collaboStudy.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    @GetMapping("/api/projects/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getAllProjects(@RequestParam(required = false) String search) {
        List<Project> projects;
        if (search != null && !search.isEmpty()) {
            projects = projectRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            projects = projectRepository.findAll();
        }
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project createdProject = projectService.createProject(project);
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.findAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findProjectById(id));
    }

    // New endpoint to add a member to a project
    @PostMapping("/{projectId}/addMember/{userId}")
    @PreAuthorize("isAuthenticated()") // You can add more specific rules here like `hasRole('ADMIN')`
    public ResponseEntity<Project> addMemberToProject(@PathVariable Long projectId, @PathVariable Long userId) {
        Project updatedProject = projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody ProjectUpdateDTO projectDTO) {
        Project updatedProject = projectService.updateProject(id, projectDTO);
        return ResponseEntity.ok(updatedProject);
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> archiveProject(@PathVariable Long id) {
        projectService.archiveProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestToJoinProject(@PathVariable Long id) {
        projectService.requestToJoinProject(id);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/join/requests/{requestId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> approveJoinRequest(@PathVariable Long requestId) {
        projectService.approveJoinRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/join/requests/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> rejectJoinRequest(@PathVariable Long requestId) {
        projectService.rejectJoinRequest(requestId);
        return ResponseEntity.ok().build();
    }
}